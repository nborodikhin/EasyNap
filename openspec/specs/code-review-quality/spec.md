# Code Review Quality

## Purpose

Defines testing coverage and implementation quality requirements that protect review findings from regressing.

## Requirements

### Requirement: Three-tier testing policy
The project SHALL maintain a three-tier testing policy governing which test type applies to which code:
- **Tier 1 — Pure JVM unit tests** (`src/test/`): any logic with no Android framework dependency (pure functions, data transformations, parsing, state machines expressible in plain Kotlin).
- **Tier 2 — Robolectric tests** (`src/test/` with Robolectric runner): Android-coupled components that require `Context` or framework classes but do not require a real device (service lifecycle, broadcast receivers, DataStore-backed stores).
- **Tier 3 — Instrumentation tests** (`src/androidTest/`): full UI flows or system-service interactions that require a real Android framework and DI wiring via `HiltAndroidRule`.

#### Scenario: Pure logic is tested at Tier 1
- **WHEN** a function has no Android import and only manipulates Kotlin primitives or standard library types
- **THEN** its test lives in `src/test/` and runs on the JVM without any Android runner

#### Scenario: Android-coupled UI-free code is tested at Tier 2
- **WHEN** a component requires `Context` or Android framework classes but does not display UI
- **THEN** its test lives in `src/test/` and uses the Robolectric runner

#### Scenario: Full UI or system-service flows are tested at Tier 3
- **WHEN** a test requires rendering Compose UI or interacting with real system services
- **THEN** its test lives in `src/androidTest/` and uses `HiltAndroidRule` for DI

### Requirement: TimerController lifecycle is Robolectric-tested
`TimerController` state transitions (idle -> running, running -> cancelled, running -> snoozed) SHALL be covered by Robolectric tests that inject a fake `TimerStore`.

#### Scenario: Start transitions state to Running
- **WHEN** `timerController.start(minutes)` is called with a valid duration
- **THEN** `timerController.state.value` becomes `TimerState.Running`

#### Scenario: Cancel transitions state to Idle
- **WHEN** a timer is running and `timerController.cancel()` is called
- **THEN** `timerController.state.value` becomes `TimerState.Idle`

#### Scenario: SnoozeStart transitions state to Running with isSnooze=true
- **WHEN** `timerController.startSnooze(minutes)` is called
- **THEN** `timerController.state.value` is `TimerState.Running` with `isSnooze = true`

### Requirement: AlarmReceiver and NapTimerService are Robolectric-tested
`AlarmReceiver` broadcast handling and `NapTimerService` foreground lifecycle SHALL be covered by Robolectric tests.

#### Scenario: AlarmReceiver stops timer service and starts alarm service
- **WHEN** `AlarmReceiver.onReceive` is called
- **THEN** `NapTimerService` is stopped and `AlarmService` is started

#### Scenario: NapTimerService starts foreground with correct notification
- **WHEN** `NapTimerService.onStartCommand` is called with an active timer in the store
- **THEN** the service enters foreground state with a notification on channel `timer_channel`

### Requirement: Core e2e flows are instrumentation-tested
The following user flows SHALL be covered by instrumentation tests in `androidTest/` using `HiltAndroidRule`:
- Setup -> Start nap
- Running -> Cancel
- Running -> Snooze
- Alarm firing -> Dismiss

#### Scenario: Setup screen starts a nap
- **WHEN** the user taps a duration tile on SetupScreen
- **THEN** the UI transitions to RunningScreen showing a countdown

#### Scenario: Running screen cancel returns to Setup
- **WHEN** RunningScreen is displayed and the user taps Cancel
- **THEN** the UI transitions back to SetupScreen

#### Scenario: Snooze extends the timer
- **WHEN** the alarm is firing and the user selects a snooze option
- **THEN** a new running timer starts with `isSnooze = true` and the alarm activity finishes

#### Scenario: Alarm dismiss stops the alarm
- **WHEN** the alarm is firing and the user taps Stop
- **THEN** `AlarmService` stops and the app returns to SetupScreen

### Requirement: appendToBuffer pure function is unit-tested
The `appendToBuffer` function (keypad input buffer logic) SHALL be covered by JVM unit tests for all edge cases: digit append, colon insertion, backspace, and buffer length caps.

#### Scenario: Digit appended within limit
- **WHEN** a digit key is pressed and the buffer has fewer than 3 digits (no colon)
- **THEN** the digit is appended

#### Scenario: Digit rejected at digit limit
- **WHEN** a digit key is pressed and the buffer already has 3 digits and no colon
- **THEN** the buffer is unchanged

#### Scenario: Colon inserts when not present
- **WHEN** the colon key is pressed and the buffer has at least one digit and no colon
- **THEN** the colon is appended

#### Scenario: Colon rejected when already present
- **WHEN** the colon key is pressed and the buffer already contains a colon
- **THEN** the buffer is unchanged

#### Scenario: Backspace removes last character
- **WHEN** the backspace key is pressed and the buffer is non-empty
- **THEN** the last character is removed

#### Scenario: Backspace on empty buffer is no-op
- **WHEN** the backspace key is pressed and the buffer is empty
- **THEN** the buffer remains empty

#### Scenario: Seconds field capped at two digits
- **WHEN** a digit key is pressed and the buffer has a colon with two digits after it
- **THEN** the buffer is unchanged

### Requirement: Coroutine cancellation is not swallowed
Coroutine-backed data and service code SHALL NOT handle all exceptions in a way that consumes `CancellationException`. Any broad coroutine exception handling SHALL rethrow `CancellationException` before applying fallback behavior.

#### Scenario: DataStore cancellation propagates
- **WHEN** the timer preference DataStore flow is cancelled while being collected
- **THEN** `CancellationException` is rethrown instead of being converted to default preferences
