## Context

`TimerController` is a Hilt `@Singleton` that manages timer lifecycle and duration history. History is persisted via `TimerStore` / `TimerPreferenceStore` (DataStore). Currently, `start()` carries an implicit `updateHistory` side-effect controlled by a boolean parameter — callers cannot add to history without also starting a timer. The setup screen's `DurationGrid` renders up to 5 recent durations with no removal affordance.

## Goals / Non-Goals

**Goals:**
- Long-press delete flow with pending-delete visual state (red outline + × badge, others dimmed)
- Immediate removal on confirmation; snackbar undo that restores to the exact original position
- Explicit history management API — decouple `addTimer` from `start()`
- Full rotation safety with zero UI serialization of internal state

**Non-Goals:**
- Multi-select or batch deletion
- Deleting or reordering the Custom tile
- Persistent undo beyond the snackbar lifetime
- Animating tile reflow on deletion/undo

## Decisions

### Undo state lives in `TimerController`, not the UI

`TimerController` is a `@Singleton` and survives configuration changes. Keeping the pending undo (duration + original position) inside the controller means rotation is handled with zero serialization — the controller's state is just there when the UI reconnects.

**Alternative considered:** `removeFromHistory()` returns an `UndoToken`; UI holds it in `rememberSaveable`. Rejected because it leaks the internal position concept to the UI and requires the token to be `Parcelable`.

### `undo()` takes no parameters; `removeFromHistory()` returns `Unit`

The UI knows the deleted duration (it long-pressed the tile), so it has enough to build the snackbar message. It does not need to know the position. An opaque `undo()` is the simplest contract that satisfies rotation and avoids exposing internals.

**Alternative considered:** Sealed `UndoToken` as a public type. Rejected — once the token crosses the public boundary the implementation is constrained and tests become coupled to token structure.

### Auto-expire pending undo inside the controller

`removeFromHistory()` launches a coroutine on the controller's own `scope` that nulls the internal pending undo after ~5 seconds (matching snackbar lifetime). This ensures `undo()` is a no-op even if called unexpectedly after the window closes. The UI's snackbar drives visibility independently; the controller auto-expires as a belt-and-suspenders guard.

### Explicit `addTimer(durationMinutes, position)` for all history writes

`start()` currently has an `updateHistory` flag that mixes timer lifecycle with history management. Replacing it with an explicit `addTimer` call at each call site makes both methods independently testable and enables position-aware insertion for undo. History management becomes a first-class operation rather than a side-effect.

`addToHistory(minutes, position)` semantics: deduplicate (remove existing occurrence of `minutes` first), then insert at `min(position, list.size)`, cap at 5. This is correct for all cases — new addition at front (position 0), undo to original position, and graceful handling if the list changed size.

**Alternative considered:** Keep `updateHistory` flag, add a separate `restoreToHistory(minutes, position)` for undo only. Rejected as it solves the undo case but leaves the implicit side-effect in `start()`.

### `DurationGridMode` sealed interface for UI delete state

Long-press introduces a modal state on the grid. A sealed interface (`Normal` / `PendingDelete(minutes)`) makes the state machine explicit and is naturally extensible — future multi-select is a new branch with no changes to existing branches.

## Risks / Trade-offs

**Stale undo window** → `undo()` called after the user considers the deletion permanent.  
Mitigation: controller auto-expires after 5 seconds. `undo()` is a no-op if `pendingUndo` is null.

**Position drift on undo** → if the user starts a nap with the same duration during the undo window, that duration re-enters history at position 0 before undo fires.  
Mitigation: `addToHistory` deduplicates before inserting. The item ends up at the captured position within the current list, which may differ by one slot. Acceptable — corner case and the result is still coherent.

**`FakeTimerStore` drift** → two separate fake implementations (unit + instrumented) must track the interface.  
Mitigation: both fakes are updated as part of this change's task list.
