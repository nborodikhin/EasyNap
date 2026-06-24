## 1. Dependencies And Store

- [x] 1.1 Add the Jetpack DataStore Preferences dependency.
- [x] 1.2 Create a timer persistence abstraction for end time, nap duration, and duration history.
- [x] 1.3 Define DataStore preference keys for existing timer values.
- [x] 1.4 Add SharedPreferences-to-DataStore migration for `end_at_millis`, `nap_duration_minutes`, and `duration_history`.

## 2. Controller And Call Sites

- [x] 2.1 Refactor `TimerController` to use DataStore-backed suspend/Flow APIs instead of direct SharedPreferences reads.
- [x] 2.2 Replace `loadHistory()` usage in setup UI with lifecycle-aware collection of async history state.
- [x] 2.3 Replace `getNapDurationMinutes()` usage in alarm UI with async/state-backed duration access.
- [x] 2.4 Replace `NapTimerService` direct SharedPreferences read with DataStore-backed active timer loading.

## 3. Behavior Preservation

- [x] 3.1 Preserve current timer start, cancel, completion, and padding behavior.
- [x] 3.2 Preserve current recent duration seed, dedupe, ordering, and cap behavior.
- [x] 3.3 Preserve current alarm and foreground notification behavior.

## 4. Validation

- [x] 4.1 Add or update unit tests for DataStore-backed history and timer state persistence.
- [x] 4.2 Add migration coverage for existing SharedPreferences values.
- [x] 4.3 Build the debug Android app.
- [x] 4.4 Manually verify active timer restoration, setup duration history, countdown notification, and alarm duration display.
