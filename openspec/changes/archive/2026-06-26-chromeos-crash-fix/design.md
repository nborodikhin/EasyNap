## Context

EasyNap uses a two-activity architecture: `MainActivity` for the timer setup UI and `AlarmActivity` (launched from `AlarmService`) for the full-screen alarm. On ChromeOS, Android apps run in ARC (Android Runtime for Chrome) with freeform windowing. Two platform-specific behaviors triggered crashes during testing:

1. `content://settings/system/alarm_alert` cannot be opened as a file descriptor by `MediaPlayerService` on ChromeOS — the system has no alarm sound configured, and `setDataSource()` throws `IOException` on the main thread, killing the process.

2. ChromeOS's ARC notification presenter fires the notification's `fullScreenIntent` when `stopForeground(STOP_FOREGROUND_REMOVE)` removes the notification window, treating removal as a user interaction. Because `AlarmActivity` is `singleInstance` and its task is already being torn down at that point, Android creates a new task (new window) with a fresh `AlarmActivity` instance. The original bug report was "Stop opens another alarm window."

## Goals / Non-Goals

**Goals:**
- Alarm audio gracefully falls back to any available system sound instead of crashing
- Pressing Stop always closes the alarm UI with no spurious re-launch on ChromeOS
- No regressions on standard Android phones

**Non-Goals:**
- Bundling a custom alarm sound as a raw resource (avoided to keep APK size down and respect user's system preferences)
- Fixing the two-window UX root cause (that requires a single-activity refactor tracked separately)

## Decisions

### Decision: URI fallback chain instead of try/catch around the whole player

Alternatives considered:
- **Single try/catch around all of `startAudioFadeIn`** — simpler but catches too broadly; a bug in the fade scheduler would also be silenced.
- **URI availability pre-check** — query `ContentResolver` before calling `setDataSource`. Adds complexity and the check is not atomic with the open.
- **Chosen: per-URI try/catch in a loop** — tries alarm → notification → ringtone, releases and nulls the `MediaPlayer` on each failure, breaks on first success. `mediaPlayer` being null is already the safe no-audio path used by the fade steps.

### Decision: Set `isRunning = false` inside `stopAlarm()` before `stopForeground()`

The spurious second `AlarmActivity` is launched by ChromeOS in the ~60 ms window between `stopForeground()` removing the notification and `onDestroy()` setting `isRunning = false`. Moving the assignment to the top of `stopAlarm()` closes that window synchronously on the main thread, so the flag is cleared before the platform can act on the notification removal.

Alternatives considered:
- **Cancel the PendingIntent** before `stopForeground` — would prevent the intent from being fired, but `PendingIntent.cancel()` on a `getActivity` pending intent is not guaranteed to prevent an already-in-flight system dispatch.
- **Wait for `onDestroy`** — too late; the spurious launch happens before `onDestroy` runs.

### Decision: Guard in `AlarmActivity.onCreate()` rather than in the service

The `isRunning` guard in `onCreate()` handles the case defensively: regardless of how the activity is launched (service `startActivity`, notification `fullScreenIntent`, or any future path), a post-stop instance immediately finishes. This is a belt-and-suspenders layer on top of the flag timing fix.

### Decision: `receiverRegistered` boolean flag

When `onCreate()` exits early via `finish()`, the broadcast receiver is never registered. `onDestroy()` is still called and would throw `IllegalArgumentException` on `unregisterReceiver`. A simple boolean set at registration time is the minimal, non-leaking fix. Alternatives (try/catch in `onDestroy`, isFinishing check) are less explicit about intent.

## Risks / Trade-offs

- **`isRunning` is a `@Volatile` companion object field** — correct for cross-thread visibility, but the guard is still a best-effort race mitigation, not a hard lock. On a very slow device the 60 ms window could be longer. The guard in `onCreate()` is the backstop.
- **Fallback audio may not match user expectations** — if the alarm URI fails and notification/ringtone plays instead, the sound differs from what the user hears for other alarms. Acceptable: silence or wrong sound is better than a crash.
- **No regression risk on phones** — the URI loop hits the alarm URI on the first attempt on standard Android; all other changes are guard conditions that never trigger on phones.
