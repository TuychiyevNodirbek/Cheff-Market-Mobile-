# Фаза 6: iOS-таргет — план реализации

Статус: **код написан «вслепую», НИ РАЗУ не скомпилирован под iOS**. На
Windows Kotlin/Native под iOS не компилируется — это ограничение тулчейна,
не среды, и обойти его нельзя никаким конфигурированием. Всё, что ниже
помечено как «сделано», проверено **только** тем, что `:androidApp`
по-прежнему собирается и работает на реальном Android-устройстве — это
подтверждает, что рефакторинг не сломал Android, но **ничего не говорит**
о том, компилируется ли новый iOS-таргет и iosMain-код вообще.

Первое, что нужно сделать на Mac — не писать новый код, а прогнать
`./gradlew :shared:compileKotlinIosSimulatorArm64` и чинить то, что
всплывёт. Ожидайте ошибок — особенно в местах, помеченных ниже как
«неверифицированный best-effort».

---

## 0. Точка отсчёта (актуально)

```
ReceipeDelivery/
├── androidApp/     — тонкий com.android.application (MainActivity, Application, манифест, res)
└── shared/         — KMP-модуль (com.android.library), таргеты: androidTarget + iosX64/iosArm64/iosSimulatorArm64
    ├── commonMain  — данные, ВСЕ state-классы (Auth/Cart/Cooking/Location/Order/Settings/AppState),
    │                 ВСЕ 7 экранов, RecipeApp.kt/NavHost, тема, компоненты — кроме перечисленного ниже
    ├── androidMain — только то, что реально платформо-специфично (8 файлов, см. §2)
    └── iosMain     — actual-реализации тех же 8 контрактов, ни разу не скомпилированы
```

`iosApp/` (сам Xcode-проект) **ещё не создан** — это следующий большой шаг, см. §3.5.

## 1. Жёсткие предпосылки

- **macOS** (свой Mac или CI-раннер, например GitHub Actions `macos-latest`)
- **Xcode** (актуальная стабильная версия; какая именно — смотреть на момент
  реализации, KMP обычно требует последнюю-минус-одну)
- Kotlin Multiplatform Mobile плагин для Android Studio/Xcode не обязателен,
  но плагин **Kotlin Multiplatform** в Android Studio упростит отладку iOS-таргета
- CocoaPods **не обязателен** — собираем нативный `.framework` через штатный
  Gradle-таск (`binaries.framework {}` уже настроен в `shared/build.gradle.kts`)

## 2. Что было Android-only — статус по каждому файлу

| Файл (contract → android actual → ios actual) | Что делает | Статус |
|---|---|---|
| `ui/components/DeliveryMap.kt` → `.android.kt` → `.ios.kt` | Карта доставки/пикер адреса | Android: оборачивает `YandexMap.kt` как раньше, без изменений. **iOS: честная заглушка** — серый `Box` с текстом «Карта (iOS): пока не подключена», реальной карты нет. См. §4. |
| `ui/components/ConnectivityStatus.kt` → `.android.kt` → `.ios.kt` | Индикатор интернета | Android: без изменений (`ConnectivityManager`). **iOS: заглушка** — всегда возвращает «онлайн». Нужен `NWPathMonitor` через cinterop — не писал вслепую, слишком легко ошибиться в биндингах C-колбэков без компилятора под рукой. |
| `ui/components/LocationPermission.kt` → `.android.kt` → `.ios.kt` | Разрешение на геолокацию + reverse-геокодинг | Android: без изменений. **iOS: заглушка** — permission всегда «не выдано», геокодинг всегда `null`. Нужны `CLLocationManager`/`CLGeocoder`. |
| `ui/components/PhoneDialer.kt` → `.android.kt` → `.ios.kt` | Кнопка «Позвонить в поддержку» | Android: `Intent.ACTION_DIAL`, без изменений. iOS: **best-effort**, не заглушка — `UIApplication.sharedApplication.openURL("tel:...")`, стандартный для KMP паттерн, но синтаксис не проверен компилятором. |
| `ui/PlatformSettings.kt` → `.android.kt` → `.ios.kt` | Фабрика `Settings` для авторизации/корзины/настроек | Android: оборачивает `SharedPreferences`, без изменений. **iOS: не заглушка** — `NSUserDefaultsSettings` из самой библиотеки `multiplatform-settings`, готовый код библиотеки, не самописный cinterop. Вероятность, что заработает как есть, высокая. |
| `ui/theme/DynamicColor.kt` (`expect` в Theme.kt) | Material You (Android 12+) | Android: без изменений. iOS: просто `null` (на iOS нет Material You) — тривиально, риска нет. |
| `data/RecipeImages.kt` | Картинки блюд | **Полностью в commonMain**, expect/actual не нужен — переведено на Compose Multiplatform Resources (`Res.drawable.*`), работает одинаково на всех таргетах. |
| `AndroidAppContext.kt` | `Context` вне `@Composable` (нужен `LocationPermission.android.kt` для `Geocoder`) | Android-only по своей природе, iOS-эквивалента не требует. |

Все остальные файлы (`AppState.kt`, `CartState.kt`, `RecipeApp.kt`, все 7
экранов, `theme/{Color,Type,Theme}.kt`, `components/{Common,NoInternetScreen}.kt`)
— **в `commonMain`, без единого expect/actual**, компилируются как обычный
Kotlin. `YandexMap.kt` осознанно остался в `androidMain` целиком — Yandex
MapKit не публикует iOS SDK под Kotlin/Native.

## 3. Пошаговый план — статус

### 3.1 Координаты (`GeoPoint`) — ✅ сделано, проверено Android-сборкой

`GeoPoint(latitude, longitude)` в `commonMain` заменил `com.yandex.mapkit.geometry.Point`
везде в бизнес-логике. `LocationState`/`OrderState`/`AppState` переехали в
`commonMain` целиком.

### 3.2 iOS-таргеты в Gradle — ✅ добавлено, ⚠️ не проверено компиляцией

`shared/build.gradle.kts` объявляет `iosX64()`/`iosArm64()`/`iosSimulatorArm64()`
и `binaries.framework { baseName = "shared"; isStatic = true }`. Локально
Gradle сам подтверждает: *"The following Kotlin/Native targets cannot be
built on this machine and are disabled"* — то есть конфигурация валидна
синтаксически, но **ни разу не проходила реальную компиляцию**.

Зависимости, которые теперь тянутся через `compose.*` (Compose Multiplatform
accessors) в `commonMain` вместо `androidx.compose.*` в `androidMain`:
`compose.runtime`, `compose.foundation`, `compose.material3`,
`compose.materialIconsExtended`, `compose.ui`, `compose.components.resources`,
`compose.components.uiToolingPreview`. Плюс `androidx.navigation:navigation-compose`
переехала в `commonMain.dependencies` — предполагается, что 2.8.4 публикует
iOS-артефакт; **это первое, что стоит перепроверить**, если синк на Mac упадёт.

### 3.3 expect/actual разводка — ✅ написано, ⚠️ iOS-часть не проверена

См. таблицу в §2 — сделано всё, кроме реальной карты (§4).

### 3.4 Compose Resources вместо `R.drawable` — ✅ сделано, проверено на устройстве

4 файла `dish_*.xml` переехали в `shared/src/commonMain/composeResources/drawable/`.
`imageResFor()` возвращает `DrawableResource`. **Реально проверено на
Android-устройстве** — карточки рецептов отрисовались с картинками через
новый механизм, скриншот подтверждён визуально в этой сессии.

### 3.5 Xcode-проект `iosApp` — ❌ не начато

Ничего из этого пункта не создано:

- [ ] `iosApp/` — отдельный Xcode-проект рядом с `androidApp/`/`shared/`
- [ ] `ContentView.swift` + `UIViewControllerRepresentable`-обёртка
- [ ] Точка входа `fun MainViewController() = ComposeUIViewController { RecipeApp() }`
      в `iosMain` — **тоже ещё не написана**, добавить вместе с проектом
- [ ] `Info.plist`: `NSLocationWhenInUseUsageDescription`, bundle id

### 3.6 Build Phase (Run Script) — ❌ не начато, зависит от §3.5

```bash
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

## 4. Карта на iOS — по-прежнему открытый вопрос

Заглушка (`DeliveryMap.ios.kt`) стоит на месте и не крашит приложение — это
осознанное решение, не забытая недоделка: писать cinterop-код под MapLibre
или Apple MapKit без возможности его скомпилировать — заведомо потратить
время впустую на код, который почти гарантированно не соберётся с первого
раза. Варианты остаются те же (MapLibre Compose Multiplatform рекомендован
ранее), но **начинать их писать нужно уже на Mac**, с компилятором под рукой.

## 5. Порядок выполнения — обновлено

1. ~~GeoPoint, перенос в commonMain~~ — ✅ сделано
2. ~~iOS-таргеты в Gradle, expect/actual заглушки, Compose Resources~~ —
   ✅ написано, но **следующий шаг на Mac должен начаться именно с проверки
   этого**, не с новых фич: `./gradlew :shared:compileKotlinIosSimulatorArm64`
   и правка того, что не скомпилируется
3. §3.5–3.6 (Xcode-проект, Run Script, точка входа) → первый запуск в
   симуляторе, видим экран онбординга
4. Замена заглушек на реальный код, по одной: сначала `PlatformSettings`
   и `DynamicColor` (наименьший риск), потом `PhoneDialer` (best-effort,
   вероятно почти рабочий), затем `ConnectivityStatus`/`LocationPermission`
   (нужен реальный cinterop/CoreLocation), карта — последней
5. §4 (реальная карта) — после того как приложение стабильно собирается
   и открывается без неё

## 6. Definition of Done для Фазы 6 (минимальный)

- [ ] `./gradlew :shared:compileKotlinIosSimulatorArm64` (или аналог)
      проходит без ошибок — **первая реальная проверка всего, что описано
      в §2–3.4**, ещё не пройдена
- [ ] Xcode-проект `iosApp` открывается, собирается, запускается в
      симуляторе iPhone
- [ ] Видно экран онбординга, навигация между онбордингом → авторизацией
      работает (проверяет, что `navigation-compose` реально тянется на iOS)
- [ ] Флоу авторизации (submitPhone/verifyOtp) работает на симуляторе так
      же, как на Android — подтверждает, что `AuthState` в `commonMain`
      действительно платформонезависим
- [ ] Настройки сохраняются между перезапусками через `NSUserDefaultsSettings`
- [ ] Карта на экране выбора района — либо реальная (если §4 сделан), либо
      заглушка не крашится и не блокирует остальной флоу (сейчас заглушка
      уже написана именно так — просто ещё не проверена компиляцией)

## 7. Известные риски

- **Ни одна строчка iosMain-кода не проходила компилятор.** Это не
  теоретический риск «может быть ошибки» — почти наверняка будут синтаксические
  и API-несоответствия в `PhoneDialer.ios.kt` (UIApplication API),
  `PlatformSettings.ios.kt` (конструктор `NSUserDefaultsSettings`) и в
  Gradle-конфигурации iOS-таргетов. Первая сессия на Mac должна быть
  "починить компиляцию", не "добавить фичи".
- **Версии Compose Multiplatform / Kotlin / navigation-compose** в
  `libs.versions.toml` (Kotlin 2.0.21, Compose Multiplatform 1.7.1,
  navigation-compose 2.8.4) зафиксированы на момент Android-миграции —
  свериться с https://kmp.jetbrains.com на актуальность перед первой iOS-сборкой.
- **MapLibre Compose Multiplatform** — состояние библиотеки нужно
  перепроверить непосредственно перед реализацией §4.
- **AGP/Kotlin Multiplatform совместимость** — предупреждение "AGP 8.13.2
  выше максимальной протестированной версии KGP" уже видно в логах
  Android-сборок; на iOS-таргете оно может обернуться реальной ошибкой.
