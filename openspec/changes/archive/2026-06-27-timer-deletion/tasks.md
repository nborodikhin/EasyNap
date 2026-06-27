## 1. TimerStore interface

- [x] 1.1 Add `suspend fun addToHistory(minutes: Float, position: Int)` to `TimerStore`
- [x] 1.2 Add `suspend fun removeFromHistory(minutes: Float)` to `TimerStore`
- [x] 1.3 Remove `updateHistory: Boolean` parameter from `TimerStore.startTimer`

## 2. TimerPreferenceStore implementation

- [x] 2.1 Implement `addToHistory`: deduplicate, insert at `min(position, size)`, cap at 5, write to DataStore
- [x] 2.2 Implement `removeFromHistory`: filter out value, write to DataStore; no-op if absent
- [x] 2.3 Remove `updateHistory` logic from `startTimer`; simplify to persist end time and duration only

## 3. FakeTimerStore (unit tests)

- [x] 3.1 Add `addToHistory` and `removeFromHistory` to `app/src/test/.../FakeTimerStore`
- [x] 3.2 Remove `updateHistory` parameter from `startTimer` in the same fake

## 4. FakeTimerStore (instrumented tests)

- [x] 4.1 Add `addToHistory` and `removeFromHistory` to `app/src/androidTest/.../FakeTimerStore`
- [x] 4.2 Remove `updateHistory` parameter from `startTimer` in the same fake

## 5. TimerController

- [x] 5.1 Add `fun addTimer(durationMinutes: Float, position: Int = 0)` — launches `store.addToHistory`
- [x] 5.2 Add `fun removeFromHistory(durationMinutes: Float)` — captures current position, launches `store.removeFromHistory`, stores internal pending-undo, starts 5-second auto-expire coroutine
- [x] 5.3 Add `fun undo()` — applies pending-undo if non-null (calls `store.addToHistory` with captured position), clears pending-undo; no-op if null
- [x] 5.4 Remove `updateHistory` from `startInternal`; `_napDurationMinutes` is now always updated in `start()` path (not in `startSnooze`)
- [x] 5.5 Update existing `start()` call sites in `TimerController` itself if any reference `updateHistory`

## 6. SetupScreen — history call sites

- [x] 6.1 Update duration tile tap: call `timerController.addTimer(minutes, 0)` then `timerController.start(minutes)`
- [x] 6.2 Update Custom sheet confirm: call `timerController.addTimer(minutes, 0)` then `timerController.start(minutes)`

## 7. DurationGrid — delete mode UI

- [x] 7.1 Define `sealed interface DurationGridMode` with `Normal` and `PendingDelete(minutes: Float)` in `SetupScreen.kt`
- [x] 7.2 Add `mode: DurationGridMode` state (`remember { mutableStateOf(Normal) }`) inside `DurationGrid`
- [x] 7.3 When in `PendingDelete`, pass `onClick = { mode = Normal }` to non-selected tiles (no timer started) and dim them via `alpha`
- [x] 7.4 Wire parent `Column` with `Modifier.clickable { if (mode is PendingDelete) mode = Normal }` for empty-space tap cancellation
- [x] 7.5 Add `onDurationDeleted: (Float) -> Unit` callback parameter to `DurationGrid`

## 8. DurationTile — pending-delete visual state

- [x] 8.1 Accept optional `isPendingDelete: Boolean` parameter in `DurationTile`
- [x] 8.2 When `isPendingDelete`: apply red border (`MaterialTheme.colorScheme.error` at 70% alpha) and subtle red background tint
- [x] 8.3 Add × badge composable (red filled circle, white ×, positioned top-right corner via `Box` + `align(Alignment.TopEnd)`) visible only when `isPendingDelete`
- [x] 8.4 Wire `Modifier.combinedClickable` on `DurationTile`: short tap = normal action, long press = `onLongClick` callback

## 9. SetupScreen — delete/undo wiring

- [x] 9.1 Add `var pendingUndoMinutes by rememberSaveable { mutableStateOf<Float?>(null) }` in `SetupScreen`
- [x] 9.2 In `onDurationDeleted` handler: call `timerController.removeFromHistory(minutes)`, set `pendingUndoMinutes = minutes`
- [x] 9.3 Show `Snackbar` (via `SnackbarHostState` on the `Scaffold`) when `pendingUndoMinutes != null`; message = formatted duration + " timer deleted"; action = "Undo"
- [x] 9.4 On snackbar "Undo" action: call `timerController.undo()`, set `pendingUndoMinutes = null`
- [x] 9.5 On snackbar dismiss (timeout): set `pendingUndoMinutes = null`

## 10. Tests

- [x] 10.1 Unit test `TimerPreferenceStore.addToHistory`: deduplication, position clamping, 5-item cap
- [x] 10.2 Unit test `TimerPreferenceStore.removeFromHistory`: present value removed, absent value is no-op
- [x] 10.3 Unit test `TimerController.removeFromHistory` + `undo()`: position captured correctly, undo restores, undo after auto-expire is no-op
- [x] 10.4 Unit test `TimerController.start()` no longer updates history; `addTimer` does
- [x] 10.5 Update any existing `startTimer` call sites in tests that pass `updateHistory`
