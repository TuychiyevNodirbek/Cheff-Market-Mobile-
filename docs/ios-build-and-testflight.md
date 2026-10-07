# ReceipeDelivery — сборка iOS на Mac и релиз в TestFlight (для разработчика)

> ⚠️ **Важно прочитать первым.** iOS-таргет написан на Windows и **ни разу
> не компилировался** (Kotlin/Native под iOS собирается только на macOS).
> Xcode-проекта `iosApp/` в репозитории **ещё нет**. Подробности и список
> заглушек — в [ios-phase-plan.md](ios-phase-plan.md). Будьте готовы чинить
> ошибки компиляции на шаге 3.

## 0. Что есть в репозитории

```
ReceipeDelivery/
├── androidApp/   — Android-приложение
├── shared/       — KMP-модуль: весь UI (Compose Multiplatform) и логика
│   ├── commonMain/
│   ├── androidMain/
│   └── iosMain/  — actual-реализации для iOS (часть — заглушки)
└── docs/
```

`shared/build.gradle.kts` уже объявляет таргеты `iosX64`, `iosArm64`,
`iosSimulatorArm64` и статический framework `shared`.
Бэкенд: `https://web-production-887d1.up.railway.app` (`data/ApiConfig.kt`).

## 1. Требования

| Что | Зачем |
|---|---|
| Mac на Apple Silicon (или Intel) с актуальной macOS | Kotlin/Native + Xcode работают только тут |
| **Xcode** (последняя стабильная) + `xcode-select --install` | сборка, подпись, загрузка |
| **JDK 17+** (например, `brew install --cask zulu@17`) | Gradle |
| **Android Studio** + плагин *Kotlin Multiplatform* (опционально) | удобная отладка |
| Аккаунт **Apple Developer Program** (99 $/год) | без него TestFlight недоступен |
| Доступ к команде в **App Store Connect** с ролью *Admin* или *App Manager* | создание приложения, загрузка сборок |

Проверка окружения:

```bash
xcodebuild -version
```

```bash
java -version
```

## 2. Клонирование

```bash
git clone <repo-url> ReceipeDelivery && cd ReceipeDelivery
```

```bash
chmod +x gradlew
```

Создайте `local.properties` (он не в git) с путём к Android SDK, если
собираете и Android: `sdk.dir=/Users/<you>/Library/Android/sdk`.
Для чисто iOS-сборки SDK может понадобиться всё равно, т.к. `shared` —
`com.android.library`; проще поставить Android Studio.

Если в `local.properties` хранится ключ Yandex MapKit — он нужен только Android.

## 3. Первая компиляция shared под iOS

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```

Ожидайте ошибок в `shared/src/iosMain` (особенно `PhoneDialer.ios.kt`,
см. §2 плана). Чините до зелёного, затем:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Убедитесь, что Android не сломался:

```bash
./gradlew :androidApp:assembleDebug
```

## 4. Создание Xcode-проекта `iosApp/` (один раз)

Самый надёжный путь — взять шаблон из **KMP Wizard** (kmp.jetbrains.com,
Android + iOS, «Share UI»), скопировать оттуда папку `iosApp/` в корень
репозитория и адаптировать:

1. **Bundle Identifier**: например `uz.nodirbek.receiptdelivery`
   (должен совпасть с App ID в Apple Developer, см. §6).
2. **Build Phases → Run Script** (перед *Compile Sources*):
   ```sh
   cd "$SRCROOT/.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
   В *Build Settings* отключите `ENABLE_USER_SCRIPT_SANDBOXING` (= No).
3. **Build Settings**:
   - `FRAMEWORK_SEARCH_PATHS` += `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
   - `OTHER_LDFLAGS` += `-framework shared`
4. **Swift-точка входа** — показать Compose UI:
   ```swift
   import SwiftUI
   import shared

   struct ComposeView: UIViewControllerRepresentable {
       func makeUIViewController(context: Context) -> UIViewController {
           MainViewControllerKt.MainViewController()
       }
       func updateUIViewController(_ vc: UIViewController, context: Context) {}
   }

   @main
   struct iOSApp: App {
       var body: some Scene {
           WindowGroup { ComposeView().ignoresSafeArea(.keyboard) }
       }
   }
   ```
   Если в `iosMain` ещё нет `MainViewController.kt` — добавьте:
   ```kotlin
   fun MainViewController() = ComposeUIViewController { RecipeApp() }
   ```
   (имя корневого composable сверьте с `RecipeApp.kt`).
5. **Info.plist**:
   - `CADisableMinimumFrameDurationOnPhone` = `YES` (плавная анимация Compose);
   - `NSLocationWhenInUseUsageDescription` — текст, зачем нужна геолокация
     (обязательно, если код запрашивает геолокацию, иначе ревью отклонит);
   - `ITSAppUsesNonExemptEncryption` = `NO` (только HTTPS — избавит от
     вопроса об экспортном шифровании при каждой загрузке);
   - `CFBundleDisplayName` — отображаемое имя.
6. **App Icon**: `Assets.xcassets/AppIcon` — одна картинка 1024×1024 PNG
   **без прозрачности** (иначе загрузка будет отклонена).

Закоммитьте `iosApp/` (кроме `xcuserdata/`, `*.xcuserstate`, `build/`).

## 5. Запуск на симуляторе / устройстве

```bash
open iosApp/iosApp.xcodeproj
```

В Xcode: выберите схему `iosApp` и симулятор → **⌘R**.
Для реального iPhone: *Signing & Capabilities* → выберите Team, включите
*Automatically manage signing*; на телефоне включите *Developer Mode*.

Пройдите основные сценарии: вход, рецепты, корзина, оформление заказа,
статус заказа, настройки. Известные заглушки: карта, геолокация, индикатор сети.

## 6. Подготовка в Apple Developer / App Store Connect (один раз)

1. **developer.apple.com → Identifiers → +** → App IDs → App → Bundle ID
   ровно как в Xcode (`uz.nodirbek.receiptdelivery`).
2. **appstoreconnect.apple.com → Apps → + → New App**:
   платформа iOS, имя, язык, Bundle ID из списка, SKU (любая строка, напр. `receiptdelivery-ios`).
3. Сертификаты и профили при *Automatically manage signing* Xcode создаст сам.

## 7. Сборка релиза и загрузка

1. Увеличьте версии в таргете `iosApp` (*General*):
   - **Version** (`MARKETING_VERSION`) — например `1.0.0`;
   - **Build** (`CURRENT_PROJECT_VERSION`) — **каждая загрузка должна иметь
     новый номер** (1, 2, 3…), иначе App Store Connect её отклонит.
2. Схема → *Any iOS Device (arm64)*.
3. **Product → Archive** (собирает Release; Gradle-скрипт сам соберёт
   `iosArm64` framework).
4. Откроется **Organizer** → выберите архив → **Distribute App** →
   **TestFlight & App Store** (или *TestFlight Internal Only*) → **Distribute**.
5. Через 5–30 минут сборка пройдёт обработку и появится в App Store Connect
   → *TestFlight*. Придёт письмо.

### Альтернатива из терминала (для CI)

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release -archivePath build/iosApp.xcarchive -destination 'generic/platform=iOS' archive
```

```bash
xcodebuild -exportArchive -archivePath build/iosApp.xcarchive -exportOptionsPlist iosApp/ExportOptions.plist -exportPath build/export
```

```bash
xcrun altool --upload-app -f build/export/iosApp.ipa -t ios --apiKey <KEY_ID> --apiIssuer <ISSUER_ID>
```

`ExportOptions.plist` — с `method` = `app-store-connect`. API-ключ создаётся
в App Store Connect → *Users and Access → Integrations*; файл `.p8` кладётся
в `~/.appstoreconnect/private_keys/`. **Не коммитьте ключ.**

## 8. Раздача тестировщикам

App Store Connect → приложение → **TestFlight**:

- **Internal Testing** (до 100 человек из команды App Store Connect):
  создайте группу, добавьте людей — сборка доступна сразу, без ревью.
- **External Testing** (до 10 000 человек по email или публичной ссылке):
  создайте группу → добавьте сборку → заполните *Test Information*
  (что тестировать, email для обратной связи, **демо-аккаунт для входа**,
  если нужен логин) → отправьте на **Beta App Review** (обычно < 1–2 суток;
  повторные сборки той же версии часто проходят автоматически).
- Включите **Public Link**, если нужна ссылка вместо приглашений.

Отправьте тестировщикам [testflight-testers.md](testflight-testers.md).

## 9. Частые проблемы

| Симптом | Решение |
|---|---|
| `embedAndSignAppleFrameworkForXcode` не найден / Gradle не стартует из Xcode | Запустите `./gradlew` хотя бы раз из терминала; проверьте `JAVA_HOME` (Xcode не видит переменные из `.zshrc` — пропишите путь к JDK в `gradle.properties`: `org.gradle.java.home=...`) |
| `Sandbox: rsync deny` / ошибки скрипта | `ENABLE_USER_SCRIPT_SANDBOXING = No` |
| `No such module 'shared'` | Сначала соберите проект (⌘B), проверьте `FRAMEWORK_SEARCH_PATHS` |
| Сборка отклонена: invalid icon | Иконка 1024×1024 без альфа-канала |
| `Redundant Binary Upload` | Увеличьте Build number |
| Missing purpose string | Добавьте `NS…UsageDescription` в Info.plist |
| Сборка висит в *Processing* | Подождите до часа; проверьте почту — там причина отказа |

## 10. Чек-лист релиза

- [ ] `./gradlew :shared:allTests` и `:androidApp:assembleDebug` зелёные
- [ ] Приложение проверено на реальном iPhone
- [ ] Build number увеличен
- [ ] Archive → Distribute → загружено
- [ ] Сборка прошла Processing
- [ ] Добавлены «What to Test» заметки
- [ ] Сборка назначена группам тестировщиков
- [ ] Изменения (`iosApp/`, правки `iosMain`) закоммичены
