# Фаза 6: iOS-таргет — план реализации

Статус: **план, код не писался**. Требует macOS + Xcode для выполнения и
проверки — на Windows Kotlin/Native под iOS не компилируется, это
ограничение тулчейна, не среды.

Этот документ написан по итогам аудита текущего `:shared` (KMP-модуль) —
все пути и блокеры ниже взяты из реального грепа по коду, а не общих мест.

---

## 0. Точка отсчёта

Модульная структура на момент написания:

```
ReceipeDelivery/
├── androidApp/     — тонкий com.android.application (MainActivity, Application, манифест, res)
└── shared/         — KMP-модуль (com.android.library), единственный target сейчас: androidTarget
    ├── commonMain  — AuthState, CookingState, SettingsState, весь data/ (модели, Settings-хранилища)
    └── androidMain — AppState, RecipeApp, CartState, LocationState, OrderState,
                       все экраны (ui/screens), компоненты (ui/components), тема (ui/theme)
```

`shared/build.gradle.kts` подключает `org.jetbrains.compose` (Compose Multiplatform)
и `compose.runtime` в `commonMain` — то есть план "общий UI" был заложен с самого начала
миграции, это не отход от него.

## 1. Жёсткие предпосылки

- **macOS** (свой Mac или CI-раннер, например GitHub Actions `macos-latest`)
- **Xcode** (актуальная стабильная версия; какая именно — смотреть на момент
  реализации, KMP обычно требует последнюю-минус-одну)
- Kotlin Multiplatform Mobile плагин для Android Studio/Xcode не обязателен,
  но плагин **Kotlin Multiplatform** в Android Studio упростит отладку iOS-таргета
- CocoaPods **не обязателен** — будем собирать нативный `.xcframework` через
  штатный Gradle-таск `org.jetbrains.kotlin.multiplatform`, без Pods-обёртки
  (проще для старта; если позже понадобится нативная iOS-библиотека с
  собственным CocoaPod — тогда подключим `kotlin("native.cocoapods")` отдельно)

## 2. Что реально блокирует iOS-компиляцию — по файлам

Грепом по `shared/src/androidMain` найдены Android-only API в этих файлах:

| Файл | Что использует | Что делать |
|---|---|---|
| `ui/AppState.kt` | `com.yandex.mapkit.geometry.Point` | см. §3.1 — заменить на общий `GeoPoint` |
| `ui/LocationState.kt` | `com.yandex.mapkit.geometry.Point` | то же |
| `ui/OrderState.kt` | `com.yandex.mapkit.geometry.Point` | то же |
| `ui/RecipeApp.kt` | `com.yandex.mapkit.geometry.Point`, `android.content.Context` (`getSharedPreferences`) | Point → GeoPoint; Context-код и так уже локализован в `RecipeApp.kt` — станет androidMain actual для `expect fun rememberAppSettings(): Settings` |
| `ui/components/YandexMap.kt` | весь Yandex MapKit SDK | expect/actual карта, см. §4 |
| `ui/components/ConnectivityStatus.kt` | `android.net.ConnectivityManager` | expect `connectivityState()`, actual на Android (as-is) + actual на iOS (`NWPathMonitor`) |
| `ui/screens/DistrictScreens.kt` | использует `YandexMapView`/`Point` через компоненты выше | не трогать напрямую — почистится, когда почистятся зависимости |
| `ui/screens/SettingsScreen.kt` | `LocalContext.current` (скорее всего для `Intent`/share или похожего) | проверить точное использование при переносе, обернуть в expect-функцию если нужно системное действие |
| `ui/theme/Theme.kt` | `Build.VERSION.SDK_INT`, `dynamicLightColorScheme`/`dynamicDarkColorScheme` (Android 12+ Material You) | ветка уже за флагом `dynamicColor: Boolean = false` (выключена по умолчанию) — тривиально завернуть в `expect fun supportsDynamicColor(): Boolean` (Android actual: `Build.VERSION.SDK_INT >= S`; iOS actual: `false`) |
| `data/RecipeImages.kt` | `uz.nodirbek.receiptdelivery.shared.R.drawable.*` (Android resource id) | expect `fun imageResFor(key: String): ???` — на Android возвращает `Int` (resId), на iOS нужен другой механизм показа картинки (см. §3.4) |

Всё остальное (`commonMain/data/*`, `AuthState`, `CookingState`, `SettingsState`,
семь файлов экранов помимо перечисленных, `ui/components/Common.kt`,
`ui/components/NoInternetScreen.kt`) уже не содержит Android-only API и либо
уже в `commonMain`, либо переедет туда без правок.

## 3. Пошаговый план

### 3.1 Абстрагировать координаты — сделать ДО разбивки на iosMain

Сейчас `AppState`/`LocationState`/`OrderState` тянут `com.yandex.mapkit.geometry.Point`
только как пару `(latitude, longitude)` — сами эти классы не вызывают методов
Yandex SDK, только конструируют `Point(lat, lon)` и читают `.latitude`/`.longitude`.
Это значит: убрав Point, все три класса становятся чистым Kotlin и **переезжают
в `commonMain` целиком** — не нужно писать три раза одну и ту же бизнес-логику
под каждую платформу.

```kotlin
// commonMain/uz/nodirbek/receiptdelivery/geo/GeoPoint.kt
data class GeoPoint(val latitude: Double, val longitude: Double)
```

Работа:
- [ ] Завести `GeoPoint` в `commonMain`
- [ ] В `AppState.kt`/`LocationState.kt`/`OrderState.kt` заменить все
      `com.yandex.mapkit.geometry.Point` на `GeoPoint`
- [ ] Перенести `AppState.kt`, `LocationState.kt`, `OrderState.kt` из
      `androidMain` в `commonMain` (после замены типа они больше не
      содержат Android-only кода — единственное, что их туда держало,
      это Point)
- [ ] На границе с UI (там, где сейчас `YandexMapView`/`YandexMap.kt`
      реально просит `com.yandex.mapkit.geometry.Point`) — конвертировать
      `GeoPoint → Point` только в androidMain-actual карты (см. §4), не
      в бизнес-логике

Это единственный шаг, который стоит сделать **уже сейчас, на Android**,
без Mac — чистый рефакторинг, проверяется обычной Android-сборкой. Реальная
экономия: 3 больших файла состояния перестают быть Android-only.

### 3.2 Добавить iOS-таргеты в `shared/build.gradle.kts`

```kotlin
kotlin {
    androidTarget { /* как сейчас */ }

    listOf(
        iosX64(),          // симулятор на Intel Mac
        iosArm64(),         // реальные устройства
        iosSimulatorArm64() // симулятор на Apple Silicon
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true   // статический фреймворк — проще подключить в Xcode без доп. шагов подписи
        }
    }

    sourceSets {
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        // аналогично iosTest, если понадобятся iOS-специфичные тесты
    }
}
```

- [ ] `compose.runtime` в `commonMain` уже объявлена — Compose Multiplatform
      runtime доступен iOS без доп. действий
- [ ] `multiplatform-settings` (уже в `commonMain.dependencies`) — библиотека
      **уже мультиплатформенная**, на iOS её `Settings`-имплементация — это
      `NSUserDefaultsSettings`, ничего дописывать не нужно, кроме
      androidMain/iosMain фабрики (см. `RecipeApp.kt` — там сейчас
      `SharedPreferencesSettings(context.getSharedPreferences(...))`
      напрямую; обернуть в `expect fun platformSettings(): Settings`)
- [ ] `androidx.navigation:navigation-compose` — с версии 2.8 публикует
      common-артефакт с поддержкой iOS/desktop; проверить, что версия в
      `libs.versions.toml` (сейчас 2.8.4) действительно тянет iOS-таргет
      при первой синхронизации на Mac — если нет, поднять до последней
      стабильной 2.8.x/2.9.x на момент реализации

### 3.3 Развести platform-specific код по `expect`/`actual`

Для каждого блокера из таблицы §2 (кроме координат, см. §3.1):

- [ ] **Карта** — `expect` composable-обёртка + Android/iOS `actual`, детали в §4
- [ ] **Connectivity** — `expect fun connectivityState(): State<Boolean>`,
      Android-actual = текущий код 1-в-1, iOS-actual через `NWPathMonitor`
      (Network framework, доступен из Kotlin/Native через cinterop или
      готовую обёртку типа `ktor-client` engine-detection — решить на месте)
- [ ] **Dynamic color в теме** — `expect fun supportsDynamicColor(): Boolean`,
      iOS-actual просто `false` (на iOS нет Material You)
- [ ] **`Settings`-фабрика** — `expect fun platformSettings(): Settings`,
      Android-actual оборачивает `SharedPreferencesSettings`, iOS-actual — `NSUserDefaultsSettings`
- [ ] **Картинки блюд (`RecipeImages.kt`)** — тут развилка, см. §3.4

### 3.4 Картинки: переход на `Compose Resources` вместо `R.drawable`

Сейчас `imageResFor(key: String): Int` возвращает Android resource id — это
принципиально Android-only механизм, на iOS такого `Int`-идентификатора не
существует. Правильное решение — не писать expect/actual с разными типами
возврата (Int на Android, что-то другое на iOS), а перейти на **Compose
Multiplatform Resources** (`org.jetbrains.compose` уже подключён, ресурсы
из коробки):

- [ ] Переместить 4 файла `dish_*.xml` (сейчас в `shared/src/androidMain/res/drawable/`)
      в `shared/src/commonMain/composeResources/drawable/`
- [ ] `imageResFor(key: String)` начинает возвращать `DrawableResource`
      (тип из `org.jetbrains.compose.resources`, общий для всех платформ) —
      функция и её вызовы (`painterResource(...)`) переезжают в `commonMain`
      без всякого expect/actual
- [ ] Это же автоматически решает вопрос иконки приложения на будущее —
      компоуз-ресурсы работают одинаково на Android/iOS/desktop

Эта замена — не косметика: без неё каждая новая картинка в приложении
требует отдельно android-версии и отдельно iOS-версии кода её показа.

### 3.5 Xcode-проект `iosApp`

- [ ] Создать `iosApp/` рядом с `androidApp/` и `shared/` — это **не**
      Gradle-модуль, это отдельный Xcode-проект (`iosApp.xcodeproj` или
      `iosApp.xcodeproj` через `.xcworkspace`, если подключать CocoaPods
      позже)
- [ ] `settings.gradle.kts` менять не нужно — Xcode собирает `shared` как
      внешнюю зависимость через build-фазу (`Run Script` с
      `./gradlew :shared:embedAndSignAppleFrameworkForXcode`), Gradle не
      должен "знать" про Xcode-проект
- [ ] Минимальный `ContentView.swift`:

```swift
import SwiftUI
import shared // модуль из собранного .xcframework

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

- [ ] В `commonMain` (или `iosMain`) добавить точку входа для Xcode:

```kotlin
// iosMain/uz/nodirbek/receiptdelivery/MainViewController.kt
fun MainViewController() = ComposeUIViewController { RecipeApp() }
```

- [ ] `Info.plist` в `iosApp`:
  - `NSLocationWhenInUseUsageDescription` — приложению нужна геолокация
    для определения района доставки (используется в `DistrictScreens.kt`
    / `onUserLocationFound`)
  - Название/бандл-айди — согласовать с `applicationId` Android
    (`uz.nodirbek.receiptdelivery`), например `uz.nodirbek.receiptdelivery.ios`
    или общий, если планируется единая запись в App Store Connect

### 3.6 Build Phase — как Xcode получает `shared`

Стандартный подход без CocoaPods (Kotlin Multiplatform Mobile "прямой" режим):

1. В Xcode target → Build Phases → New Run Script Phase, **до** Compile Sources:
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
2. Framework Search Paths указывают на
   `shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
   (Gradle-плагин генерирует этот путь автоматически при наличии
   `iosX64()`/`iosArm64()`/`iosSimulatorArm64()` таргетов — ничего вручную
   прописывать не должно понадобиться, если использовать стандартный
   `binaries.framework {}` блок из §3.2)

## 4. Карта на iOS — отдельный вопрос, не блокер запуска

У Yandex MapKit **нет** официального Kotlin/Native SDK — это уже обсуждалось
раньше в этой сессии. Для Фазы 6 это **не блокирует компиляцию и первый
запуск** приложения на iOS (можно временно показывать заглушку вместо карты
на экранах доставки), но реальная функциональность карты потребует решения:

| Вариант | Плюсы | Минусы |
|---|---|---|
| **MapLibre Compose Multiplatform** (`dev.sargunv:maplibre-compose` или аналог) | Один Compose-код для Android+iOS, открытые тайлы | Нужно проверить актуальное состояние библиотеки на момент реализации — экосистема молодая, могла измениться; нужен провайдер тайлов (можно взять публичный демо-стиль `demotiles.maplibre.org` на старте, без ключа) |
| **Apple MapKit нативно** (только для iOS) + Yandex MapKit на Android | Максимальная нативность на каждой платформе | UI карты пишется дважды — экран приходится делать через `expect fun MapView(...)` с абсолютно разной реализацией; больше кода |
| **Google Maps Compose Multiplatform** | Хорошая документация, стабильно | Платный после квоты, требует API-ключ на обеих платформах |

Рекомендация (совпадает с ранее согласованной в этой сессии стратегией
"общий UI"): начинать с **MapLibre Compose Multiplatform** — единственный
вариант, не ломающий принцип "один экран — один код". План:

- [ ] Завести `expect @Composable fun DeliveryMap(center: GeoPoint, onUserLocationFound: (GeoPoint) -> Unit, ...)`
      в `commonMain` — интерфейс карты, без привязки к конкретному SDK
- [ ] `androidMain actual` — обёртка над текущим `YandexMap.kt` (оставить
      Yandex как есть на Android, ничего не менять в поведении)
- [ ] Написать полноценный `commonMain actual` на MapLibre **или**
      `iosMain actual` на Apple MapKit — решить в момент реализации,
      когда будет видно состояние MapLibre-библиотеки
- [ ] Это отдельная задача, может идти параллельно/после первого успешного
      запуска "пустого" iOS-приложения — не блокирует Definition of Done
      из §6 пункт 1

## 5. Порядок выполнения

1. **Сейчас, на Windows** (реально можно делать уже сегодня, проверяется
   Android-сборкой): §3.1 — GeoPoint, перенос AppState/LocationState/OrderState
   в commonMain. Единственный пункт плана, не требующий Mac.
2. **На Mac**: §3.2 (iOS-таргеты в Gradle) → §3.3 (expect/actual заглушки,
   карта временно как no-op/заглушка) → первая сборка `:shared` под iOS
   (`./gradlew :shared:assembleXCFramework` или эквивалент) — должна пройти
   без ошибок
3. §3.5–3.6 (Xcode-проект, Run Script, точка входа) → первый запуск в
   симуляторе, видим экран онбординга
4. §3.4 (Compose Resources для картинок) — без этого RecipeApp() не
   скомпилируется на iOS вообще, поэтому фактически это часть шага 2, а не
   отдельный поздний шаг — переставлено в списке для читаемости, не для
   порядка исполнения
5. §4 (реальная карта) — уже после того, как приложение стабильно
   собирается и открывается

## 6. Definition of Done для Фазы 6 (минимальный)

- [ ] `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` (или аналог)
      проходит без ошибок
- [ ] Xcode-проект `iosApp` открывается, собирается, запускается в
      симуляторе iPhone
- [ ] Видно экран онбординга (`Onboarding1Screen`), навигация между
      онбордингом → авторизацией работает (проверяет, что
      `navigation-compose` реально тянется на iOS)
- [ ] `PhoneOTP`-подобный флоу авторизации (submitPhone/verifyOtp) работает
      на симуляторе так же, как на Android — подтверждает, что вынесенный
      в commonMain `AuthState` действительно платформонезависим
- [ ] Настройки (`SettingsState`) сохраняются между перезапусками через
      `NSUserDefaultsSettings` — подтверждает работу `multiplatform-settings`
      на iOS
- [ ] Карта на экране выбора района — либо реальная (если §4 сделан), либо
      явная заглушка "скоро будет" (не крашится, не блокирует остальной флоу)

## 7. Известные риски

- **Версии Compose Multiplatform / Kotlin / navigation-compose** в
  `libs.versions.toml` зафиксированы на момент Android-миграции (Kotlin
  2.0.21, Compose Multiplatform 1.7.1, navigation-compose 2.8.4) — к моменту
  выполнения Фазы 6 стоит свериться с актуальной матрицей совместимости на
  https://kmp.jetbrains.com и на countyhub Compose Multiplatform release notes,
  версии могли уйти вперёд
- **MapLibre Compose Multiplatform** — экосистема молодая относительно
  androidx-артефактов, состояние библиотеки нужно перепроверить
  непосредственно перед реализацией §4, не полагаться на актуальность
  информации в этом документе
- **AGP/Kotlin Multiplatform совместимость** — при первой синхронизации
  на Mac уже возникало предупреждение "AGP 8.13.2 выше максимальной
  протестированной версии KGP" (видно в логах текущих Android-сборок) —
  на iOS-таргете это предупреждение может обернуться реальной ошибкой,
  учитывать при диагностике первого падения синхронизации
