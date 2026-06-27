## Why

Users accumulate duration tiles over time with no way to remove ones they no longer use. The grid supports up to 5 recent durations, so stale entries crowd out useful ones and can only be displaced by running enough new naps.

## What Changes

- Long-pressing a duration tile enters a **pending-delete mode**: the tile gains a red outline and an × badge; all other tiles dim and become non-interactive.
- Tapping the × badge removes the duration from history **immediately** and exits the mode.
- Tapping anywhere outside the selected tile (another tile, empty space) cancels and exits the mode with no change.
- A snackbar appears after deletion: `"X min timer deleted"` with an **Undo** button.
- Tapping **Undo** restores the duration at its **original position** in the history list.
- The undo window is ~5 seconds (snackbar lifetime); after dismissal the deletion is permanent.
- `TimerController` gains an explicit `addTimer(duration, position)` method. History addition is **decoupled from `start()`** — both regular tile taps and the Custom sheet now call `addTimer` separately before `start()`.
- `TimerController` gains `removeFromHistory(duration)` and `undo()`. Undo state (position captured at deletion time) is held **internally** in the controller — nothing leaks to the UI.

## Capabilities

### New Capabilities

- `duration-deletion`: Long-press delete flow for recent duration tiles, including pending-delete visual state, immediate removal, and snackbar undo that restores to original position.

### Modified Capabilities

- `timer-setup`: History management is now explicit — `addTimer(duration, position)` replaces the implicit side-effect inside `start()`. Both regular tile taps and the Custom sheet use the new API.
- `timer-persistence`: `TimerStore` gains `addToHistory(minutes, position)` and `removeFromHistory(minutes)`. The `updateHistory` parameter is removed from `startTimer`.

## Impact

- `TimerStore` interface — two new methods, `updateHistory` param removed from `startTimer`
- `TimerPreferenceStore` — implements new methods; `startTimer` simplified
- `FakeTimerStore` (×2 — unit + instrumented) — mirrors interface changes
- `TimerController` — new public methods; `startInternal` loses history side-effect
- `SetupScreen` / `DurationGrid` — two call sites updated; delete/undo wiring added
- Existing tests for `startTimer` / `start()` — minor updates to drop `updateHistory` arg
