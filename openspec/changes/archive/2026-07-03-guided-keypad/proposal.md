## Why

The current keypad offers all buttons at all times, letting users construct invalid intermediate states mid-entry (e.g., typing `:` before a digit, or building durations that exceed 120 minutes). Making only the contextually valid buttons active at each step eliminates dead-end states and makes the correct path self-evident without requiring error messages.

## What Changes

- **Remove the `:` button.** Colon is auto-inserted into the buffer when the user enters sub-minute mode (i.e., when any digit is pressed while the buffer is `"0"`).
- **Replace `colonEnabled: Boolean` with a 7-state machine** that drives per-key `enabled` on every keypad button and the Start button.
- **Sub-minute seconds are always 2 digits** (tens digit then units digit). Single-digit seconds entries (e.g. `"0:6"`) are no longer reachable.
- **Merge `PAD_MS_SHORT` / `PAD_MS_LONG` into a single `START_DELAY = 990L`** constant, removing the branch on duration length.

### Keypad state machine

| # | State | Buffer | Active digits | ⌫ | Start |
|---|-------|--------|--------------|---|-------|
| 1 | `EMPTY` | `""` | 0–9 | ✗ | ✗ |
| 2 | `SUBMINUTE_START` | `"0"` | 0–5 | ✓ | ✗ |
| 3 | `SUBMINUTE_TENS` | `"0:0"`…`"0:5"` | 5–9 if tens=0, else 0–9 | ✓ | ✗ |
| 4 | `SUBMINUTE_FULL` | `"0:05"`…`"0:59"` | — | ✓ | ✓ |
| 5 | `MINUTE_ONES` | `"1"`…`"9"` | 0–9 | ✓ | ✓ |
| 6 | `MINUTE_TENS` | `"10"`…`"99"` | d where `(buf+d) ≤ 120` | ✓ | ✓ |
| 7 | `MINUTE_HUNDREDS` | `"100"`…`"120"` | — | ✓ | ✓ |

`SUBMINUTE_FULL` Start is unconditionally on: state 3's digit constraints guarantee ≥ 5 sec.

### Layout

Layout A: the `:` slot (bottom-left of the 4×3 grid) is left visually empty. The Start button remains prominent below the keypad, unchanged.

## Capabilities

### New Capabilities

_(none — this is a behavioral refinement to existing input)_

### Modified Capabilities

- `timer-setup`: keypad loses the colon key; button availability is driven by the state machine above; sub-minute seconds require exactly 2 digits

## Impact

- `TimerHelpers.kt`: `canAppendColon` deleted; `appendToBuffer` updated for auto-colon insertion
- `SetupScreen.kt`: `NumericKeypad` receives per-key enabled state instead of `colonEnabled`; `:` button slot left empty
- `TimerController.kt`: `PAD_MS_SHORT` / `PAD_MS_LONG` → single `START_DELAY = 990L`
- Hardware keyboard: colon/semicolon key no longer inserts a separator (colon is auto-inserted)
