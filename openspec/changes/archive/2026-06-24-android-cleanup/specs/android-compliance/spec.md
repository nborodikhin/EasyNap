## ADDED Requirements

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
