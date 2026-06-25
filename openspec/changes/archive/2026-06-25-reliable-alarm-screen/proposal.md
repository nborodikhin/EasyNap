## Why

When `AlarmService` starts, it attempts to launch `AlarmActivity` via `startActivity()`. On Android 10+ this can be silently blocked by Background Activity Launch restrictions, and even when the activity does launch, the user can swipe it away from recents while the alarm is still ringing. In both cases the alarm continues (sound and vibration) but the user has no way to reach the stop/snooze UI — especially if notifications are disabled.

## What Changes

- `AlarmService` exposes a process-scoped `isRunning` flag so other components can determine whether an alarm is currently ringing.
- `MainActivity.onResume` checks `AlarmService.isRunning` and launches `AlarmActivity` immediately if the service is active but the alarm screen is not showing.
- While `MainActivity` is in the foreground with a running timer, a lifecycle-scoped coroutine schedules an activity-side `AlarmActivity` launch at `endAtMillis` as a fallback in case the service's own `startActivity` was blocked.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `nap-alarm`: The alarm screen gains two new recovery paths — activity-side launch at timer expiry, and MainActivity foreground recovery — ensuring the alarm UI is reachable whenever `AlarmService` is running.

## Impact

- `AlarmService`: adds a `companion object` `isRunning` flag (set in `onStartCommand`, cleared in `onDestroy`).
- `MainActivity`: adds an `onResume` check and a `lifecycleScope` coroutine that subscribes to `timerController.state` and schedules a fallback `AlarmActivity` launch.
- No changes to `TimerState`, `TimerStore`, `TimerController`, `AlarmReceiver`, or `AlarmActivity`.
- `AlarmActivity` already declares `launchMode="singleInstance"`, so duplicate launches from both the service and the activity are safe — the second launch delivers `onNewIntent` to the running instance.
