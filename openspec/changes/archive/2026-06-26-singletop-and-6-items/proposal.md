## Why

Two unrelated UX defects surfaced on multi-window platforms (ChromeOS, tablets): the alarm fires in a separate OS window instead of the existing app window, and the "+" custom tile wraps onto a third row because the grid holds 7 items (6 history + "+").

## What Changes

- `AlarmActivity` launch mode changes from `singleInstance` to `singleTop` so it shares the existing app task and window on ChromeOS/tablet split-screen.
- The displayed history in `DurationGrid` is capped at 5 items so that history + "+" always fits in exactly 2 rows (6 cells).
- `TimerPreferenceStore.withSeedDurations` and `updatedHistory` storage limit remain at 6 — only the display slice changes.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `nap-alarm`: Alarm screen must open inside the existing app window in multi-window environments; it no longer requires its own task.
- `timer-setup`: Grid cap changes — the grid displays at most 5 duration tiles (plus the Custom tile), guaranteeing "+" always appears in the first two rows.

## Impact

- `AndroidManifest.xml`: `AlarmActivity` `launchMode` attribute.
- `SetupScreen.kt` / `DurationGrid`: slice passed to the grid.
- `TimerPreferenceStore`: no storage-format change; only display logic affected.
- `timer-setup` spec: "Grid is capped" scenario wording updated (6 stored → 5 displayed + Custom).
- `nap-alarm` spec: new multi-window requirement added.
