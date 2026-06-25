## 1. AlarmService: isRunning Flag

- [x] 1.1 Add `@Volatile var isRunning: Boolean = false` to `AlarmService.companion object`.
- [x] 1.2 Set `isRunning = true` at the start of `onStartCommand` (before the early-return on `ACTION_STOP`).
- [x] 1.3 Set `isRunning = false` in `onDestroy`.

## 2. MainActivity: onResume Recovery

- [x] 2.1 Add a private `startAlarmActivity()` helper in `MainActivity` that starts `AlarmActivity` with `FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP`.
- [x] 2.2 In `MainActivity.onResume`, after the existing `refreshAlarmNotificationAvailability()` call, check `AlarmService.isRunning` and call `startAlarmActivity()` if true.

## 3. MainActivity: Activity-Side Fallback Launch

- [x] 3.1 Add a nullable `Job` field (`alarmLaunchJob`) to `MainActivity` to hold the lifecycle-scoped coroutine.
- [x] 3.2 Add a `scheduleAlarmActivityLaunch()` private method that cancels any existing `alarmLaunchJob`, then launches a new `lifecycleScope` coroutine. Inside the coroutine, collect `timerController.state`: for each `TimerState.Running` state, cancel any previous inner delay job and start a new one that delays until `endAtMillis` then calls `startAlarmActivity()`; for any other state, cancel the inner job.
- [x] 3.3 Call `scheduleAlarmActivityLaunch()` from `onResume`, after the `AlarmService.isRunning` check.
- [x] 3.4 Cancel `alarmLaunchJob` in `onPause` so the coroutine does not fire while the app is in the background.

## 4. Tests

- [x] 4.1 Add a Robolectric unit test verifying that `AlarmService.isRunning` is `false` before the service starts, `true` after `onStartCommand`, and `false` after `onDestroy`.
- [x] 4.2 Add a Robolectric unit test (or extend an existing one) verifying that `MainActivity.onResume` starts `AlarmActivity` when `AlarmService.isRunning` is `true`.
- [x] 4.3 Confirm that the existing connected `EasyNapE2ETest` tests still pass (they grant the notification permission and test navigation flows; they should not be affected by this change).

## 5. Verification

- [x] 5.1 Run `testDebugUnitTest`.
- [x] 5.2 Run `connectedDebugAndroidTest` if a device is available.
- [x] 5.3 Validate the OpenSpec change.
