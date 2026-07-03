## Context

The custom duration keypad (`NumericKeypad` in `SetupScreen.kt`) currently enables every button except `:`, whose enablement is derived post-hoc from the buffer via `canAppendColon`. `appendToBuffer` (`TimerHelpers.kt`) accepts almost any digit up to a length cap and relies on downstream parsing (`parseCustomDurationSeconds`, `isCustomDurationInRange`) to reject out-of-range results after the fact. This lets users type sequences that can never resolve to a valid duration (e.g. `13` then `0` → `130` minutes), discoverable only via the disabled Start button and error text.

The proposal replaces this with a 7-state machine (`EMPTY` → `SUBMINUTE_START`/`MINUTE_ONES` → … ) that derives, from the current buffer alone, which digits/backspace/Start are legal next inputs. `TimerController.startInternal` also has an unrelated but bundled cleanup: collapsing the two-tier `PAD_MS_SHORT`/`PAD_MS_LONG` scheduling pad into one constant.

## Goals / Non-Goals

**Goals:**
- A single, buffer-derived function computes keypad state and drives per-key enablement so onscreen buttons and hardware-keyboard input can't diverge.
- `appendToBuffer` itself rejects digits that are invalid for the current state (defense in depth — not just onscreen button disablement), so no caller path can produce dead-end buffers.
- Colon is never a distinct keystroke: it is inserted automatically by `appendToBuffer` when the buffer is `"0"` and a digit is pressed.
- Sub-minute seconds are always exactly 2 digits before the value is considered complete.
- Consolidate `PAD_MS_SHORT`/`PAD_MS_LONG` into one `START_DELAY` constant.

**Non-Goals:**
- No change to persisted state, DataStore schema, or `TimerStore`.
- No change to the 5 second–120 minute overall valid range, or to `parseCustomDurationSeconds`'s parsing rules beyond what the state machine already prevents from being typed.
- No visual redesign beyond leaving the `:` key's grid slot empty.

## Decisions

### 1. `KeypadState` enum + pure derivation function

Add to `TimerHelpers.kt`:

```kotlin
enum class KeypadState { EMPTY, SUBMINUTE_START, SUBMINUTE_TENS, SUBMINUTE_FULL, MINUTE_ONES, MINUTE_TENS, MINUTE_HUNDREDS }

fun keypadState(buffer: String): KeypadState
```

`keypadState` pattern-matches on buffer shape only (length, presence/position of `:`), mirroring the table in the proposal. This is the single source of truth for "what state am I in," used by both digit-acceptance and button-enablement logic.

**Alternative considered:** thread an explicit state alongside the buffer (e.g. in a small state-holder class) instead of re-deriving it from the string each time. Rejected — the buffer is already the persisted/`rememberSaveable` source of truth (`CustomDurationSheet`), and every existing call site only has the buffer, not a parallel state object. Re-deriving is O(1) string inspection and keeps `appendToBuffer(buffer, key): String` as the only stateful-looking function, unchanged in shape.

### 2. `appendToBuffer` becomes the enforcement point, not just a length-capper

`appendToBuffer(buffer, key)` computes `keypadState(buffer)`, and for digit keys checks the digit against that state's allowed set before appending (auto-inserting `:` first when leaving `SUBMINUTE_START`). Backspace behavior is unchanged (always allowed when buffer is non-empty). The `:` branch and `canAppendColon` are deleted.

**Why enforce here instead of only in the UI layer:** the same function backs both the onscreen keypad and the hardware-keyboard path (`mapCustomDurationKey` → `appendToBuffer`, `SetupScreen.kt:604,620`). If invalid-digit rejection lived only in "is this button enabled," hardware input would still be able to produce dead-end buffers. Centralizing in `appendToBuffer` means one implementation to test and no duplicated range logic between the two input paths.

### 3. Per-key enabled state for `NumericKeypad`

`NumericKeypad` no longer takes `colonEnabled: Boolean`. Instead it computes, per digit key, `enabled = appendToBuffer(buffer, key) != buffer` (i.e. "does pressing this key actually change the buffer") — reusing decision 2's logic rather than re-implementing the allowed-digit ranges a second time in the Compose layer. Backspace enablement: `buffer.isNotEmpty()`. Start enablement is unchanged (`isStartEnabled` computed in `CustomDurationSheet` from `parseCustomDurationSeconds` + `isCustomDurationInRange`), since that already evaluates to true/false correctly across all 7 states without modification.

**Alternative considered:** expose `allowedDigits(state): Set<Char>` from `TimerHelpers.kt` and have `NumericKeypad` call that directly. Rejected — it would duplicate the exact same range logic that `appendToBuffer` needs internally, and the two could drift. Deriving "enabled" from "would appending this key change anything" guarantees the button state and the actual accept/reject behavior can never disagree.

### 4. Hardware keyboard: drop colon/period mapping

`mapCustomDurationKey` (`SetupScreen.kt:511`) removes the `Key.Semicolon, Key.Period, Key.NumPadDot -> ":"` branch entirely. Those keys fall through to `null` (ignored), consistent with "unsupported keys are ignored" — colon is never a directly-typeable character anymore, matching the onscreen keypad.

### 5. `TimerController`: single `START_DELAY`

Replace `PAD_MS_SHORT` / `PAD_MS_LONG` with `START_DELAY = 990L` and remove the `if (durationSeconds >= 60)` branch in `startInternal`.

## Risks / Trade-offs

- **[Risk]** Long-duration naps (≥60s) get a shorter scheduling pad (990ms vs. previous 1990ms). If device-specific latency (coroutine dispatch, DataStore write, foreground service startup, `AlarmManager.setAlarmClock`) exceeds 990ms on a slow/throttled device, the alarm could fire up to ~1s before the displayed countdown reaches zero. → **Mitigation:** this is an explicit, accepted simplification per the proposal (not a bug fix); 990ms already covers the short-duration case today with no reported issues, and the two-tier split had no documented justification tying it to duration length specifically. Flag for observation post-release; revert to a duration-based pad only if undershoot is reported.
- **[Risk]** Moving digit-rejection into `appendToBuffer` changes its contract from "always appends within a length cap" to "may silently no-op for a rejected digit." Any other caller of `appendToBuffer` besides the keypad/hardware-keyboard paths would inherit this stricter behavior. → **Mitigation:** confirmed via grep that `appendToBuffer` is only called from `SetupScreen.kt`'s two sites (onscreen key press, hardware key handler); no other call sites exist.

## Migration Plan

Not applicable — UI-only behavioral change with no persisted-state migration. Ship in the next release; no rollback data concerns since no schema changes.
