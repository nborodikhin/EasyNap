## Context

Two independent UX defects appear on ChromeOS (and potentially tablet split-screen). First, `AlarmActivity` uses `launchMode="singleInstance"`, which forces it into a separate Android task — ChromeOS surfaces each task as its own OS window, so the alarm fires in a second window alongside the app. Second, `DurationGrid` receives up to 6 history items from `TimerPreferenceStore.withSeedDurations`, which together with the "+" Custom tile produces 7 cells. In a 3-column grid that is 3 rows, with "+" stranded alone on row 3.

Both fixes are intentionally minimal and local.

## Goals / Non-Goals

**Goals:**
- Alarm opens inside the existing app window on multi-window platforms.
- "+" Custom tile always appears on the same two-row grid as the duration tiles (≤ 6 cells total).
- Zero changes to the DataStore storage format or history eviction logic.

**Non-Goals:**
- Merging `AlarmActivity` and `MainActivity` into a single activity.
- Changing the number of items stored in history (still 6).
- Altering grid column count or tile dimensions.

## Decisions

### 1. `singleTop` instead of `singleInstance` for `AlarmActivity`

`singleInstance` guarantees a dedicated task (and thus a dedicated window). `singleTop` allows the activity to join the existing app task. Both prevent duplicate instances when the activity is at the top of the stack.

**Why not single-activity?** Merging would require actively reversing `setShowWhenLocked`, `setTurnScreenOn`, and `FLAG_KEEP_SCREEN_ON` on every dismiss path, and the lock-screen flags scoped to `AlarmActivity`'s own window lifecycle provide a clean, automatic boundary.

**Edge case — app not running:** When `AlarmService` fires with no existing app task, `FLAG_ACTIVITY_NEW_TASK` in the service's launch intent still creates a fresh task for `AlarmActivity`. On ChromeOS this is a new window, but it is the only window — acceptable and correct.

**No changes needed to intent flags:** The existing `FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP` flags used when dismissing the alarm (to return to `MainActivity`) remain correct with `singleTop`.

### 2. Cap the display slice at 5, not the storage limit

`TimerPreferenceStore.withSeedDurations` and `updatedHistory` already cap storage at 6 items. The display just slices that list before passing it to `DurationGrid`. Taking `history.take(5)` at the call site in `SetupScreen` is sufficient — no DataStore schema change, no migration.

**Why not lower the storage cap too?** Storing 6 and displaying 5 keeps a one-item buffer: the most-recently-used duration is always slot 0 and always visible, regardless of how seeds fill the rest.

## Risks / Trade-offs

- **singleTop loses guaranteed task isolation** → Pressing Back from `AlarmActivity` now navigates to `MainActivity` underneath, rather than to the home screen. This is actually better UX; the previous behavior (separate task → pressing Back exits the app entirely) was a side-effect of `singleInstance`, not an intentional design choice.
- **Display/storage mismatch (6 stored, 5 shown)** → The sixth stored item is invisible until an older item is displaced. Acceptable: the grid is a recency-ordered shortcut, not a complete history view.

## Migration Plan

No data migration required. The DataStore key `duration_history` format is unchanged. Existing users keep all 6 stored durations; only the display trims to 5.
