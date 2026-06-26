## Why

The alarm feature crashed on ChromeOS due to an unavailable system alarm URI, and a second bug caused a new alarm window to spawn every time the user tapped Stop — both discovered during live device testing on a Chromebook.

## What Changes

- `AlarmService.startAudioFadeIn()`: try alarm → notification → ringtone URIs in sequence, catching `IOException` at each fallback; if all fail, vibration continues without audio instead of crashing
- `AlarmService.onStartCommand()`: move `isRunning = true` assignment past the `ACTION_STOP` guard so it is never set during a stop command
- `AlarmService.stopAlarm()`: set `isRunning = false` before calling `stopForeground()` so the flag is cleared before ChromeOS's ARC notification presenter can fire the `fullScreenIntent` in response to notification removal
- `AlarmActivity.onCreate()`: immediately call `finish()` and return if `AlarmService.isRunning` is false, dismissing any spuriously re-launched instance
- `AlarmActivity`: add `receiverRegistered` flag; only unregister `finishReceiver` in `onDestroy()` if it was actually registered, preventing `IllegalArgumentException` on the early-exit path

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- `nap-alarm`: alarm audio now falls back through system URI candidates instead of crashing; AlarmActivity guards against spurious re-launch by ChromeOS notification system

## Impact

- `app/src/main/java/me/easynap/alarm/AlarmService.kt`
- `app/src/main/java/me/easynap/alarm/AlarmActivity.kt`
- No API, dependency, or manifest changes
