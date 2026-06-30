## Why

Easy Nap currently represents nap durations as floating-point minutes across the persistence boundary. This is awkward now that the app supports second-level custom durations: values such as 80 seconds must be stored as `80f / 60f`, then rounded back to seconds for display, notifications, and timer math.

Durations are inherently whole seconds in the product model. Persisting them as integer seconds removes float precision concerns, makes stored values easier to reason about, and aligns DataStore with the custom-duration input model.

## What Changes

- Change persisted duration values from minutes-as-`Float` to seconds-as-`Int`.
- Store selected nap duration, active timer duration, and recent duration history as integer seconds.
- Use new DataStore preference keys anywhere the stored value type or format changes, so an upgraded installation behaves like a fresh install for those values.
- Treat this as a breaking persistence change with no migration from the existing float-minute keys or comma-separated minute history.
- Keep user-facing behavior the same: duration tiles, custom durations, countdown timing, notifications, alarm copy, snooze behavior, and deletion/undo should continue to display and behave as they do today.
- Preserve minute-based labels where appropriate by deriving display minutes from stored seconds rather than storing minute floats.

## Capabilities

### Modified Capabilities

- `timer-persistence`: Timer duration state and duration history are persisted as integer seconds using new keys where persisted types/formats change.
- `timer-setup`: Duration history and selected durations are modeled from second values while preserving existing tile labels and custom-duration behavior.
- `countdown-timer`: Running timer state uses second-based duration values while preserving countdown timing and progress behavior.
- `nap-alarm`: Completed nap duration display reads second-based persisted duration state while preserving existing alarm text.
- `duration-deletion`: Recent-duration deletion and undo compare and restore integer second durations rather than float minute values.

## Impact

- Affected app code: `TimerStore`, `TimerPreferenceStore`, `TimerController`, `TimerState`, duration helper functions, setup UI, running UI, alarm UI/service, notification services, fake stores, and tests.
- Persistence: replace float-minute DataStore keys such as `nap_duration_minutes` with second-based keys such as `nap_duration_seconds`; replace minute-history storage with a second-based key/format.
- Migration: none. Existing installations with old duration keys should ignore those values and fall back to the same defaults as a new install.
- Compatibility: this intentionally discards persisted active timers and duration history from pre-change builds because there are no real users yet.
