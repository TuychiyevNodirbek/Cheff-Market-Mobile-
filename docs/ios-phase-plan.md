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

## 4. Гео-подсистема на iOS — приоритизированный план починки

Три заглушки (`DeliveryMap.ios.kt`, `LocationPermission.ios.kt`) вместе
ломают не косметику, а саму возможность выбрать район на iOS — см. разбор
ниже. План рассчитан на первую Mac-сессию: цель не "красивая карта", а
восстановить рабочий флоу выбора адреса минимальными, независимо
проверяемыми шагами.

### Почему карта — не первый шаг

Ключевая деталь в `DistrictScreens.kt`: `cameraTarget` (точка, по которой
определяется район) обновляется **только** через колбэк `onCameraTargetChanged`.
`onUserLocationFound` сам по себе камеру не двигает — на Android это скрыто,
потому что `YandexLocationPickerMap` при "найти меня" **программно** двигает
камеру (`mapView.map.move(...)`), а это само по себе стреляет тем же
`CameraListener`, который вызывает `onCameraTargetChanged`. У плейсхолдера
на iOS такого моста нет вообще — оба колбэка после подключения CLLocationManager
нужно будет дёргать вручную из `LocationPickerMap`-actual, независимо от того,
есть визуальная карта или нет.

Вывод: **рабочий "найти меня" не обязан ждать выбора и интеграции реальной
карты** — это два независимых по коду, но связанных по эффекту шага.

### Порядок (по возрастанию риска и по зависимостям)

**1. `reverseGeocodeAddress` через `CLGeocoder`** — делать первым.
   - Независим от разрешений на геолокацию (геокодинг по явным координатам
     не требует `NSLocationWhenInUseUsageDescription` и не показывает
     системный диалог)
   - Самый простой cinterop-паттерн: один completion-callback → обернуть в
     `suspendCancellableCoroutine`. Хорошая "разминка" перед delegate-based
     API у `CLLocationManager`
   - Проверяется даже без `iosApp`-проекта — работает в любом хосте, который
     линкует `CoreLocation` (например, отдельный iOS-таргет юнит-теста)
   - Видимого эффекта в UI не даст, пока `cameraTarget` не начнёт двигаться
     (см. шаг 2) — это ожидаемо, не повод откладывать

**2. `rememberLocationPermissionState` через `CLLocationManager`** — делать
   вторым, переиспользуя интероп-паттерны из шага 1.
   - **Жёсткая зависимость**: нужен `Info.plist` с
     `NSLocationWhenInUseUsageDescription` — а этот файл живёт в таргете
     `iosApp`, которого пока не существует (§3.5). То есть до этого шага
     реально придётся хотя бы минимально поднять Xcode-проект, даже если
     карта в нём ещё не готова
   - При успешном разовом location fix ("найти меня") **actual должен
     вызвать оба колбэка** — и `onUserLocationFound`, и `onCameraTargetChanged`
     с одной и той же точкой — иначе UI не сдвинется с места (см. разбор выше)
   - После этого шага "найти меня" реально работает, а вместе с шагом 1 —
     подтягивается и адрес по геокодингу. Всё это — **всё ещё на сером
     плейсхолдере вместо карты**, но выбор района через GPS уже полностью
     рабочий

**3. Реальная карта (`DeliveryMap`/`LocationPickerMap`)** — делать последним.
   - Самый большой и неопределённый объём: выбор библиотеки (MapLibre Compose
     Multiplatform рекомендован ранее в сессии), провайдер тайлов, рендер,
     жесты (drag/zoom)
   - К этому моменту интероп с Apple-фреймворками уже обкатан на шагах 1–2 —
     остаётся один явно самый рискованный кусок, а не три одновременно
   - Даёт возможность **вручную** двигать пин (не только через GPS) —
     это единственное, чего не хватает после шага 2

### Что осознанно вне этого плана

`ConnectivityStatus.ios.kt` (индикатор интернета) — не часть гео-подсистемы,
ниже приоритетом: последствие заглушки чисто косметическое (экран "нет
интернета" не появится), не блокирует ни один флоу.

## 5. Порядок выполнения — обновлено

1. ~~GeoPoint, перенос в commonMain~~ — ✅ сделано
2. ~~iOS-таргеты в Gradle, expect/actual заглушки, Compose Resources~~ —
   ✅ написано, но **следующий шаг на Mac должен начаться именно с проверки
   этого**, не с новых фич: `./gradlew :shared:compileKotlinIosSimulatorArm64`
   и правка того, что не скомпилируется
3. §3.5–3.6 (Xcode-проект, Run Script, точка входа) → первый запуск в
   симуляторе, видим экран онбординга
4. Замена заглушек на реальный код: сначала `PlatformSettings` и
   `DynamicColor` (наименьший риск, уже написаны как настоящий код, не
   заглушки), потом `PhoneDialer` (best-effort, вероятно почти рабочий)
5. Гео-подсистема — по детальному приоритету в §4: `reverseGeocodeAddress`
   (CLGeocoder) → `rememberLocationPermissionState` (CLLocationManager,
   требует `Info.plist` из §3.5) → реальная карта (MapLibre/Apple MapKit).
   Уже после шага 2 выбор района через GPS работает полностью, до карты
6. `ConnectivityStatus` (NWPathMonitor) — низкий приоритет, чисто
   косметический эффект заглушки

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
