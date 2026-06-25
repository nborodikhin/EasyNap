## Context

`AlarmService` starts when `AlarmReceiver` fires from an `AlarmManager.setAlarmClock()` callback. On Android 10+ the platform restricts Background Activity Launches (BAL). The `setAlarmClock` scheduling grants an exemption window, but the window is finite (~10 s) and may expire if the service is slow to start under system load, leaving the alarm ringing with no UI.

Even when `AlarmActivity` does launch successfully, the user can swipe it away from recents while the service is still running. Because `AlarmReceiver` already called `timerController.completeTimer()` (clearing `TimerState` to `Idle`) before starting the service, `MainActivity.onResume` currently has no signal that an alarm is ringing.

`AlarmActivity` already declares `launchMode="singleInstance"`, so it always lives in its own task and any duplicate `startActivity` call is safely reduced to `onNewIntent`.

## Goals / Non-Goals

**Goals:**

- Ensure `AlarmActivity` is reachable whenever `AlarmService` is running, regardless of whether the service's own `startActivity` succeeded.
- Handle the case where `AlarmActivity` was launched and then killed (swiped from recents) while the service is still active.
- Cover the BAL-failure case when an activity is already in the foreground at the moment the timer expires — the foreground activity can launch `AlarmActivity` without BAL restrictions.

**Non-Goals:**

- Handling "missed alarm" (service already auto-stopped after 55 s). A user away from their phone simply misses the alarm — that is acceptable.
- Adding a new `TimerState` variant or changing `TimerStore`, `TimerController`, `AlarmReceiver`, or `AlarmActivity`.
- Persisting alarm-ringing state across process death (if the process is killed, `AlarmService` won't restart — `START_NOT_STICKY` — so there is nothing to recover).

## Decisions

- **Static `isRunning` flag on `AlarmService` companion object.**
  A `@Volatile var isRunning: Boolean` set `true` in `onStartCommand` and `false` in `onDestroy` gives any in-process component a cheap, synchronous signal that an alarm is currently ringing. `ActivityManager.getRunningServices()` is deprecated since API 26 and the system limits it to own-process services anyway; a static flag is simpler and sufficient. Because `AlarmService` is `START_NOT_STICKY`, it never restarts after process death, so the flag correctly reads `false` after any process restart.

- **`MainActivity.onResume` as the recovery entry point.**
  `onResume` fires whenever the user brings the app to the foreground — from the launcher, from recents, or from the notification. Checking `AlarmService.isRunning` here and immediately calling `startActivity(AlarmActivity)` covers both failure modes: service running but `AlarmActivity` never appeared (BAL failure), and `AlarmActivity` was killed after launch. Using `FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP` matches the flags already used by `AlarmService` itself and ensures no second instance is created.

- **Lifecycle-scoped coroutine for activity-side fallback launch.**
  When `MainActivity` is already in the foreground while the timer is counting down, a coroutine that runs while the lifecycle is at least `STARTED` subscribes to `timerController.state`. For a `Running` state it schedules `delay(endAtMillis - now)` then calls `startActivity(AlarmActivity)`. A foreground activity is always allowed to launch another activity, so this path is immune to BAL restrictions. The coroutine is cancelled on `onStop`, so it has no effect when the app is in the background. On state changes (e.g., cancel or new timer start), the collector restarts the inner job for the new state.
  - Alternative considered: `LaunchedEffect` inside the `is TimerState.Running` compose branch. Rejected because the effect is cancelled when `AlarmReceiver` changes `timerState` to `Idle` — the state transition can race with the delay completion, making the fallback unreliable.

## Risks / Trade-offs

- The activity-side launch and the service launch may both fire within the same second. → `singleInstance` reduces the duplicate to `onNewIntent`; no visual glitch.
- If `MainActivity` is in the foreground and the activity-side coroutine fires slightly before `AlarmReceiver`, `AlarmActivity` opens first, then `AlarmReceiver` fires and starts `AlarmService`. The service's `startActivity` then delivers `onNewIntent` to the already-visible `AlarmActivity`. → Harmless; `AlarmService` continues its audio/haptic sequence normally.
- The static `isRunning` flag is process-scoped only. If the app's process is killed while the alarm is ringing, the service and flag are both gone — the user will not see `AlarmActivity` on the next app open. → Acceptable per Non-Goals; the alarm has stopped anyway.
