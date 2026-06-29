# Android Compliance

## Purpose

Defines Android platform compliance, adaptive layout, system UI, notification, and service requirements.

## Requirements

### Requirement: App uses own notification icon
The app SHALL use a dedicated vector drawable (`@drawable/ic_notification`) for all notification small icons instead of system-provided drawables.

#### Scenario: Timer running notification shows app icon
- **WHEN** a nap timer is started and the foreground notification appears
- **THEN** the notification icon is the app's own icon, not a system stock icon

#### Scenario: Alarm notification shows app icon
- **WHEN** the alarm fires and the alarm notification appears
- **THEN** the notification icon is the app's own icon, not a system stock icon

### Requirement: Notification and alarm UI strings are localized
All user-visible strings displayed in notifications and system UI (alarm activity, services) SHALL be defined in `res/values/strings.xml` and referenced via `R.string.*`.

#### Scenario: Timer notification content is localized
- **WHEN** the timer foreground notification is built
- **THEN** the notification title and text are sourced from string resources, not hardcoded literals

#### Scenario: Alarm notification content is localized
- **WHEN** the alarm foreground notification is built
- **THEN** the notification title and text are sourced from string resources

#### Scenario: AlarmActivity strings are localized
- **WHEN** AlarmActivity is displayed
- **THEN** headline, body, stop button label, and snooze section label are all string resources

### Requirement: Composables have @Preview annotations
Every public and private `@Composable` function in the UI layer SHALL have at least one `@Preview` annotation. Composables with meaningful state variants SHALL have additional previews covering those variants.

#### Scenario: SetupScreen has preview
- **WHEN** Android Studio loads the SetupScreen file
- **THEN** at least one `@Preview` function renders SetupScreen

#### Scenario: RunningScreen has normal and snooze previews
- **WHEN** Android Studio loads the RunningScreen file
- **THEN** one preview renders a normal running state and one renders a snooze state

#### Scenario: CustomDurationSheet has previews for empty and filled buffer
- **WHEN** Android Studio loads the SetupScreen file
- **THEN** one preview shows an empty input buffer and one shows a filled buffer with a valid duration

#### Scenario: DurationGrid, DurationTile, CustomTile have previews
- **WHEN** Android Studio loads the composable files
- **THEN** each component has at least one standalone preview

#### Scenario: ValueDisplay, NumericKeypad, KeypadButton have previews
- **WHEN** Android Studio loads the composable files
- **THEN** each component has at least one standalone preview

### Requirement: Edge-to-edge insets applied correctly
All screens that call `enableEdgeToEdge()` SHALL consume window insets via `Scaffold` padding or explicit `WindowInsets` modifiers to avoid content being obscured by system bars.

#### Scenario: SetupScreen content avoids status bar overlap
- **WHEN** SetupScreen is displayed on a device with edge-to-edge enabled
- **THEN** the top content is not obscured by the status bar

#### Scenario: RunningScreen content avoids navigation bar overlap
- **WHEN** RunningScreen is displayed on a device with edge-to-edge enabled
- **THEN** the Cancel button is not obscured by the navigation bar

#### Scenario: AlarmActivity content avoids system bar overlap
- **WHEN** AlarmActivity is shown over the lock screen
- **THEN** no interactive element is obscured by status or navigation bars

### Requirement: Custom duration input adapts to compact landscape
The custom duration input sheet SHALL fit short landscape tablet windows without placing the Start button below the visible screen area.

#### Scenario: Compact landscape sheet keeps Start visible
- **WHEN** the custom duration sheet is opened in a short landscape window
- **THEN** the input, keypad, and Start button are all visible without requiring the Start button to be off-screen

#### Scenario: Orientation change preserves custom input
- **WHEN** the device orientation changes while the custom duration sheet is open
- **THEN** the sheet remains open and preserves the entered duration buffer

### Requirement: Countdown screen adapts to compact landscape
The running countdown screen SHALL avoid overlapping its progress ring and Cancel button in short landscape windows.

#### Scenario: Compact landscape countdown separates timer and action
- **WHEN** RunningScreen is displayed in a short landscape window
- **THEN** the countdown ring and Cancel button are arranged side-by-side without overlap

### Requirement: Alarm screen adapts to compact landscape
The alarm screen SHALL keep Stop and Snooze controls fully visible in short landscape windows.

#### Scenario: Compact landscape alarm separates message and actions
- **WHEN** AlarmActivity is displayed in a short landscape window
- **THEN** the alarm message and alarm actions are arranged side-by-side without clipping the Snooze buttons

### Requirement: Alarm APIs remain compatible with minSdk
Alarm UI code SHALL guard platform APIs introduced after `minSdk` and use backwards-compatible alternatives on older supported Android versions.

#### Scenario: Lock screen alarm flags support API 26
- **WHEN** `AlarmActivity` starts on API 26
- **THEN** it uses window flags instead of calling API 27-only lock-screen methods

### Requirement: Internal alarm broadcasts are explicit and non-exported
Internal alarm-dismiss broadcasts SHALL be restricted to this app and registered as non-exported receivers.

#### Scenario: Alarm stop broadcast is package-scoped
- **WHEN** `AlarmService` sends the alarm-finish broadcast
- **THEN** the intent is scoped to the app package before being sent

#### Scenario: Alarm finish receiver is non-exported
- **WHEN** `AlarmActivity` registers its finish receiver
- **THEN** it uses the AndroidX receiver API with `RECEIVER_NOT_EXPORTED`

### Requirement: Foreground timer service starts foreground synchronously
`NapTimerService` SHALL call `startForeground` synchronously from `onStartCommand` before any asynchronous timer-store lookup can delay foreground promotion.

#### Scenario: Foreground service contract is satisfied before store lookup
- **WHEN** `NapTimerService` is started via `startForegroundService`
- **THEN** it calls `startForeground` before loading the active timer from `TimerStore`

### Requirement: All user-facing UI text is localized and translation-ready
Every user-facing string rendered anywhere in the app — Compose screens, the alarm activity, notifications, services, and snackbars — SHALL be defined in `res/values/strings.xml` or `res/values/plurals.xml` and referenced via `R.string.*` / `R.plurals.*`. No user-facing literal SHALL be hardcoded in Kotlin. Grammar that depends on a quantity (nap captions, nap descriptions, alarm body, notification duration prefix, snooze labels) SHALL use plural resources rather than Kotlin string concatenation. Developer-facing text in `@Preview` and `*Stateless` preview helpers is exempt.

#### Scenario: Compose screen copy comes from resources
- **WHEN** `SetupScreen` renders the snackbar and the custom tile
- **THEN** the "… timer deleted" message, the "Undo" action, and the "Custom" label are sourced from string resources, not hardcoded literals

#### Scenario: Duration grammar uses plural resources
- **WHEN** a nap caption, custom-duration description, alarm body, or notification title prefix is built
- **THEN** the quantity-dependent text is produced from a plural resource, not by concatenating words in Kotlin

#### Scenario: Alarm body is grammatically correct for all durations
- **WHEN** the alarm screen body is shown for a 20-minute nap or a 30-second nap
- **THEN** each reads as correct English: "Your 20-minute nap is done." / "Your 30-second nap is done." (no "Your 0:05-minute nap is done")

#### Scenario: Pure formatters stay context-free
- **WHEN** the duration formatting helpers in `TimerHelpers.kt` are unit-tested on the JVM
- **THEN** they require no Android `Context` or `Resources`, returning numeric/structured results that call sites localize

### Requirement: UI accommodates translated text expansion
Layouts SHALL tolerate translated strings that are longer than the English source (≈30%+) without truncating critical labels — duration tiles and button labels SHALL wrap or ellipsize gracefully rather than clip. This SHALL be verifiable with the `en-XA` pseudolocale.

#### Scenario: Longer labels do not clip under pseudolocale
- **WHEN** the app is run under the `en-XA` pseudolocale (expanded text)
- **THEN** tile and button labels remain readable (wrapped or ellipsized), with no critical text cut off

### Requirement: App brand name is spelled consistently
The app brand SHALL be spelled "Easy Nap" (with a space) in every user-facing string, including the launcher label, home title, and notification fallback title.

#### Scenario: Notification fallback title uses the canonical brand
- **WHEN** a notification is built with no active nap duration and falls back to the brand name
- **THEN** the title reads "Easy Nap", matching `app_name` and `home_title`

### Requirement: Layouts support right-to-left (RTL) locales
The app SHALL declare `android:supportsRtl="true"` and render correctly under RTL locales (Arabic, Hebrew, Persian): spacing and alignment SHALL use direction-aware APIs (`start`/`end`, not `left`/`right`). Elements whose meaning depends on a fixed left-to-right order — the numeric duration keypad and the duration/time value display it feeds — SHALL be pinned to `LayoutDirection.Ltr` via `LocalLayoutDirection` so they do not mirror. Numeric `mm:ss` time strings SHALL render in the correct visual order (not reversed) in both LTR and RTL.

#### Scenario: Custom-duration colon key is restricted to sub-minute entries
- **WHEN** the user types a non-zero minute value (e.g. "1") into the custom-duration keypad
- **THEN** the ":" key is disabled, so `1:30` cannot be entered; only whole-minute values (e.g. "90") or sub-minute second values via `0:SS` (e.g. "0:30") are accepted

#### Scenario: Numeric keypad keeps digit order in RTL
- **WHEN** the custom-duration keypad is shown under an RTL locale
- **THEN** the keys remain in 1-2-3 / 4-5-6 order (not mirrored), with the value display and cursor laid out left-to-right

#### Scenario: Screens mirror correctly in RTL
- **WHEN** the home, countdown, and alarm screens are shown under an RTL locale
- **THEN** padding and alignment mirror with no clipped or overlapping content

#### Scenario: Countdown time is not reversed in RTL
- **WHEN** the countdown shows "05:30" under an RTL locale
- **THEN** it reads "05:30", not "30:05" or otherwise reordered

Note: `mm:ss` format no longer appears in duration tiles or nap captions; only the countdown ring and the custom-sheet value display use it.

### Requirement: App code uses no deprecated edge-to-edge APIs directly
The app's own source SHALL NOT call deprecated edge-to-edge window APIs (`Window.setStatusBarColor`, `Window.setNavigationBarColor`, `SYSTEM_UI_FLAG_*`, `FLAG_TRANSLUCENT_STATUS`) and SHALL configure edge-to-edge via supported APIs (`enableEdgeToEdge()`). Any residual Play "deprecated edge-to-edge APIs" warning that originates inside bundled libraries (e.g. `androidx.activity`'s `EdgeToEdge*` classes invoked by `enableEdgeToEdge()`) is outside the app's control and is accepted as no-user-impact.

#### Scenario: No deprecated edge-to-edge calls in app sources
- **WHEN** the app's Kotlin sources are scanned for edge-to-edge configuration
- **THEN** they contain no direct calls to `setStatusBarColor`, `setNavigationBarColor`, or `SYSTEM_UI_FLAG_*`, only `enableEdgeToEdge()`

#### Scenario: Edge-to-edge displays correctly on Android 15
- **WHEN** the app runs on an Android 15 (SDK 35) device
- **THEN** all screens draw edge-to-edge with no content obscured by the status or navigation bars
