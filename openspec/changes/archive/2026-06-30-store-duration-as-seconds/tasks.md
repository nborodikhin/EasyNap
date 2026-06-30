## 1. Core Data Model

- [x] 1.1 Update `PersistedTimer` data class: rename `durationMinutes: Float` to `durationSeconds: Int`
- [x] 1.2 Update `TimerState.Running`: rename `durationMinutes: Float` to `durationSeconds: Int`
- [x] 1.3 Update `TimerStore` interface: change all `Float` duration parameters and return types to `Int` seconds (`napDurationSeconds`, `startTimer`, `addToHistory`, `removeFromHistory`, `getNapDurationSeconds`)

## 2. Persistence Layer

- [x] 2.1 Add `intPreferencesKey("nap_duration_seconds")` and `stringPreferencesKey("duration_history_seconds")` constants to `TimerPreferenceStore`; remove `KEY_DURATION` (float) and `KEY_HISTORY` (old string) from active use
- [x] 2.2 Update `DEFAULT_HISTORY` to `listOf(300, 600, 1800)` (`List<Int>`)
- [x] 2.3 Update `napDurationSeconds: Flow<Int>` (was `napDurationMinutes: Flow<Float>`) in `TimerPreferenceStore`
- [x] 2.4 Update `startTimer` in `TimerPreferenceStore` to write `durationSeconds: Int` to the new key
- [x] 2.5 Update `addToHistory` and `removeFromHistory` in `TimerPreferenceStore` to use `Int` seconds and the new history key
- [x] 2.6 Update `parseHistory` to parse comma-separated integers; update `parseStoredHistory` and `toActiveTimer` accordingly
- [x] 2.7 Update `getNapDurationSeconds(): Int` (was `getNapDurationMinutes(): Float`)

## 3. Controller

- [x] 3.1 Update `TimerController` internal `MutableStateFlow` for duration and history to `Int`; rename `_napDurationMinutes`/`napDurationMinutes` to `_napDurationSeconds`/`napDurationSeconds`
- [x] 3.2 Update `TimerController.start`, `startSnooze`, `startInternal` to accept `durationSeconds: Int`; update padding threshold to `durationSeconds >= 60`; compute `endAt` as `durationSeconds * 1000L + pad`
- [x] 3.3 Update `TimerController.addTimer`, `removeFromHistory`, `undo` to use `Int` seconds

## 4. Helpers

- [x] 4.1 Remove `durationTotalSeconds(minutes: Float): Int` from `TimerHelpers.kt`
- [x] 4.2 Update `durationDisplayMinutesOrNull` signature to `(seconds: Int): Int?`; body becomes `if (seconds >= 60) seconds / 60 else null`
- [x] 4.3 Update `formatDurationLabel` signature to `(seconds: Int): String`; body uses `seconds / 60` and `seconds % 60`
- [x] 4.4 Update `SNOOZE_OPTIONS` to `List<Int>` with values `listOf(60, 300, 600)`

## 5. Setup UI

- [x] 5.1 Update `DurationGridMode.PendingDelete` to hold `seconds: Int` (was `minutes: Float`)
- [x] 5.2 Update `DurationGrid` to accept `durations: List<Int>` and pass `Int` to callbacks
- [x] 5.3 Update `DurationTile` to accept `seconds: Int` (was `minutes: Float`); remove `durationTotalSeconds` call; use `seconds` directly for display and `durationDisplayMinutesOrNull(seconds)`
- [x] 5.4 Update `SetupScreen`: remove float-minutes conversion in `CustomDurationSheet.onStart` callback (pass `seconds` directly to `timerController.start`)
- [x] 5.5 Update `SetupScreen` snackbar label logic: replace `durationTotalSeconds(minutes)` with direct `seconds` value; update `pendingUndoMinutes` state to `pendingUndoSeconds: Int?`

## 6. Running Screen

- [x] 6.1 Update `RunningScreen`: replace `state.durationMinutes` with `state.durationSeconds`; compute `totalMs = state.durationSeconds * 1000L`
- [x] 6.2 Update `napCaption` helper to accept `seconds: Int` (was `minutes: Float`); replace `durationTotalSeconds` call with direct `seconds`; update `durationDisplayMinutesOrNull` call
- [x] 6.3 Update `timerController.napDurationSeconds` reference (was `napDurationMinutes`) in `RunningScreen`

## 7. Alarm and Service

- [x] 7.1 Update `AlarmActivity`: replace `timerController.napDurationMinutes` with `napDurationSeconds`; update body-copy derivation to use `Int` seconds directly; update snooze calls to pass `Int` seconds from `SNOOZE_OPTIONS`; update volume-key snooze to pass `60`
- [x] 7.2 Update `NapTimerService`: change `durationMinutes: Float` field to `durationSeconds: Int`; update `durationMinutes = activeTimer.durationMinutes` assignment; update notification title derivation to use `Int` seconds

## 8. Fake Stores

- [x] 8.1 Update `FakeTimerStore` in `src/test` to implement the updated `TimerStore` interface with `Int` seconds throughout
- [x] 8.2 Update `FakeTimerStore` in `src/androidTest` identically

## 9. Tests

- [x] 9.1 Update `TimerPreferenceStoreTest`: change all `Float` duration values to `Int` seconds; update `parseHistory` test to use integer string format; update `DEFAULT_HISTORY` references
- [x] 9.2 Update `TimerControllerTest`: change all `Float` duration values to `Int` seconds in assertions and `FakeTimerStore` setup
- [x] 9.3 Update `TimerHelpersTest`: update `durationDisplayMinutesOrNull` and `formatDurationLabel` tests to use `Int` seconds; remove any `durationTotalSeconds` tests; update `SNOOZE_OPTIONS` tests if present
- [x] 9.4 Update `DurationGridTest` and `DurationGridDeleteTest`: change all `Float` history values to `Int` seconds
- [x] 9.5 Update `EasyNapE2ETest`: update any duration assertions or setup values from `Float` minutes to `Int` seconds
