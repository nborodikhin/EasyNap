## 1. AlarmService audio fallback

- [x] 1.1 Replace single-URI `setDataSource` call in `startAudioFadeIn()` with a loop over `[TYPE_ALARM, TYPE_NOTIFICATION, TYPE_RINGTONE]` URIs, catching `Exception` per attempt and releasing the player on failure
- [x] 1.2 Verify `mediaPlayer` stays `null` when all URIs fail so fade steps are no-ops rather than NPEs

## 2. AlarmService isRunning flag fix

- [x] 2.1 Move `isRunning = true` assignment in `onStartCommand()` to after the `ACTION_STOP` early-return guard
- [x] 2.2 Add `isRunning = false` as the first statement in `stopAlarm()`, before `stopForeground()`

## 3. AlarmActivity spurious-launch guard

- [x] 3.1 Add `receiverRegistered: Boolean` field to `AlarmActivity`; set it to `true` immediately after `registerReceiver` succeeds
- [x] 3.2 Guard `unregisterReceiver` in `onDestroy()` with `if (receiverRegistered)`
- [x] 3.3 Add `isRunning` check at the top of `onCreate()`: if `!AlarmService.isRunning`, call `finish()` and `return`

## 4. Verification

- [x] 4.1 Build debug APK and install on Chromebook via ADB
- [x] 4.2 Set a 5-second custom timer, let the alarm fire, confirm vibration runs and no crash occurs
- [x] 4.3 Press Stop and confirm the alarm window closes and returns to the main screen without spawning a second alarm window
- [x] 4.4 Confirm no `FATAL EXCEPTION` in logcat during the test run
