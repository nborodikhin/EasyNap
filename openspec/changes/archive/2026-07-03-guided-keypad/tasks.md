## 1. Keypad state machine (`TimerHelpers.kt`)

- [x] 1.1 Add `KeypadState` enum (`EMPTY`, `SUBMINUTE_START`, `SUBMINUTE_TENS`, `SUBMINUTE_FULL`, `MINUTE_ONES`, `MINUTE_TENS`, `MINUTE_HUNDREDS`) and a `keypadState(buffer: String): KeypadState` function that classifies a buffer per the state table in `proposal.md`.
- [x] 1.2 Rewrite `appendToBuffer` so digit keys are checked against `keypadState(buffer)`'s allowed-digit set before appending (no-op if the digit is invalid for the current state), and so pressing a digit while `buffer == "0"` auto-inserts `:` before the digit.
- [x] 1.3 Delete the `":"` branch from `appendToBuffer` and delete `canAppendColon` entirely.

## 2. Update `TimerHelpersTest.kt`

- [x] 2.1 Remove the four colon-key tests (`inserts colon after zero minute field`, `rejects colon after non-zero minute field`, `rejects colon when already present`) and any `canAppendColon` tests — the `:` key no longer exists.
- [x] 2.2 Add tests for `keypadState` covering all 7 states from the proposal's table.
- [x] 2.3 Add `appendToBuffer` tests for: auto-colon insertion on digit-while-`"0"`; rejection of a sub-minute tens digit outside 0–5; rejection of a sub-minute units digit outside 5–9 when tens is `0`; acceptance of any units digit 0–9 when tens is 1–5; rejection of a third minute digit that would push the value over 120 (e.g. `"13"` + any digit); acceptance of a third minute digit that keeps the value ≤ 120 (e.g. `"12"` + `"0"`).

## 3. Keypad UI (`SetupScreen.kt`)

- [x] 3.1 Remove `colonEnabled: Boolean` from `NumericKeypad`; compute each digit key's `enabled` as `appendToBuffer(buffer, key) != buffer`, and backspace `enabled` as `buffer.isNotEmpty()`. `NumericKeypad` takes the current `buffer: String` instead of `colonEnabled`.
- [x] 3.2 Remove `":"` from the keypad's row layout (leave that grid slot empty per Layout A), and drop the now-unused `canAppendColon` import.
- [x] 3.3 Update both `NumericKeypad(...)` call sites (compact-landscape and portrait branches in `CustomDurationSheetContent`) to pass `buffer = inputBuffer` instead of `colonEnabled = canAppendColon(inputBuffer)`.
- [x] 3.4 Remove the `Key.Semicolon, Key.Period, Key.NumPadDot -> ":"` branch from `mapCustomDurationKey` so those keys fall through to `null` (ignored).
- [x] 3.5 Update the two `@Preview` composables (`NumericKeypadColonEnabledPreview`, `NumericKeypadColonDisabledPreview`) to pass representative buffers instead of `colonEnabled`, renaming as appropriate.

## 4. Timer scheduling constant (`TimerController.kt`)

- [x] 4.1 Replace `PAD_MS_SHORT` and `PAD_MS_LONG` with a single `START_DELAY = 990L` constant.
- [x] 4.2 Remove the `if (durationSeconds >= 60) PAD_MS_LONG else PAD_MS_SHORT` branch in `startInternal`; use `START_DELAY` unconditionally.

## 5. Validation

- [x] 5.1 Run `./gradlew testDebugUnitTest` (or project equivalent) and fix any failures.
- [x] 5.2 Build and manually exercise the custom duration sheet (onscreen keypad and, if testable, hardware keyboard) to confirm only valid keys are tappable at each step and no dead-end states remain.
- [x] 5.3 Run `/simplify` on the changed files per project convention.
