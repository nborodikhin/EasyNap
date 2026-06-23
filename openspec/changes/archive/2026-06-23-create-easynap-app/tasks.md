## 1. Project scaffolding & configuration

- [x] 1.1 Create the project with `android create empty-activity --name="EasyNap" --output=.` and set the application/namespace id to `me.easynap`
- [x] 1.2 Set `minSdk 26` and `compileSdk`/`targetSdk 35` in the module `build.gradle`; confirm Compose + Material 3 dependencies are present
- [x] 1.3 Configure the Material 3 dark theme/colors to match the reference (dark background, teal quick‑start buttons, light pill Start button)
- [x] 1.4 Build the empty scaffold (`./gradlew assembleDebug`) to confirm a green baseline before adding features

## 2. Timer state & persistence (core)

- [x] 2.1 Define `TimerState` (`Idle`, `Running(endAtMillis)`) and a `TimerController` singleton exposing a `StateFlow<TimerState>`
- [x] 2.2 Persist/restore `endAtMillis` via `SharedPreferences`/DataStore so state survives process death; on init, treat a future timestamp as `Running` and a past/absent one as `Idle`
- [x] 2.3 Implement `start(durationMinutes: Float)` (compute `endAtMillis`, adding a 5 s pad when the duration is ≥ 1 minute, persist, start countdown service, schedule completion) and `cancel()` (clear state, stop service, cancel the scheduled alarm)
- [x] 2.4 Add pure helpers for parsing the duration input and formatting remaining time as `mm:ss`, with unit tests

## 3. Setup screen (capability: timer-setup)

- [x] 3.1 Build `SetupScreen` Compose layout: "Timer duration (minutes)" `OutlinedTextField` (decimal keyboard), "Start Timer" button, "Or quick start:" label, vertical list
- [x] 3.2 Generate quick‑start buttons for 5..60 in steps of 5; each calls `TimerController.start(n)`
- [x] 3.3 Wire "Start Timer" to validate the field (positive number) and start; show an inline error and do not start on empty/zero/negative/non‑numeric input
- [x] 3.4 Make the duration field IME‑safe (`windowSoftInputMode="adjustResize"`, inset/`imePadding` handling per the edge‑to‑edge skill) and the quick‑start list scrollable

## 4. Countdown foreground service & notification (capability: countdown-timer)

- [x] 4.1 Create the **Timer** notification channel (low importance, silent) and the **Alarm** channel (high importance, `CATEGORY_ALARM`, own sound suppressed)
- [x] 4.2 Implement `NapTimerService` as a foreground service (type `specialUse`); on start it reads `endAtMillis` and posts the ongoing countdown notification
- [x] 4.3 Tick ~1 Hz: recompute remaining from `endAtMillis`, update the notification text and `TimerController` state; use `START_STICKY`/redelivery and rebuild from persisted state on restart
- [x] 4.4 Stop the service cleanly when cancelled or when the timer completes

## 5. Reliable completion scheduling (capability: countdown-timer)

- [x] 5.1 Schedule completion with `AlarmManager.setAlarmClock()` to a `PendingIntent` for `AlarmReceiver`, set on `start()` and cancelled on `cancel()`
- [x] 5.2 Implement `AlarmReceiver` (BroadcastReceiver) that, on fire, stops the countdown notification path and starts `AlarmService`

## 6. Running‑timer screen & reopen behavior (capability: countdown-timer)

- [x] 6.1 Build `RunningScreen` showing live remaining time (collected from `TimerController`) and a "Cancel" button
- [x] 6.2 In `MainActivity`, select Setup vs Running from the observed `TimerState` so reopening the app while a timer is active lands on `RunningScreen`
- [x] 6.3 Wire "Cancel" to `TimerController.cancel()` and return to `SetupScreen`

## 7. Nap alarm (capability: nap-alarm)

- [x] 7.1 Implement `AlarmService` as a foreground service (type `mediaPlayback`): acquire a `WAKE_LOCK`, play the default alarm `Ringtone` on `STREAM_ALARM`, and start vibration
- [x] 7.2 Post the high‑importance Alarm notification with `setFullScreenIntent(piToAlarmActivity, true)`; clear any leftover countdown state
- [x] 7.3 Enforce a 30‑second auto‑stop (timeout) that stops sound + vibration and finishes the alarm
- [x] 7.4 Implement `AlarmActivity` with `setShowWhenLocked(true)` + `setTurnScreenOn(true)` (keep‑screen‑on), showing the "time's up" UI and a "Cancel" button
- [x] 7.5 Route Cancel and the 30 s timeout through `AlarmService` so sound, vibration, and `AlarmActivity` all stop together and the app returns to idle

## 8. Manifest & permissions

- [x] 8.1 Declare permissions: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `VIBRATE`, `WAKE_LOCK`
- [x] 8.2 Register both services (with `foregroundServiceType`), `AlarmReceiver`, and `AlarmActivity` (with `showWhenLocked`/`turnScreenOn` attributes and an appropriate launch mode) in the manifest
- [x] 8.3 Request `POST_NOTIFICATIONS` at runtime on Android 13+; ensure completion/alarm still work if it is denied
- [x] 8.4 Verify/guide `USE_FULL_SCREEN_INTENT` availability on Android 14+; degrade gracefully to the heads‑up notification + service playback if not honored

## 9. Edge‑to‑edge & polish

- [x] 9.1 Call `enableEdgeToEdge()` in both activities and apply `WindowInsets.safeDrawing` via `Scaffold`; verify no content is clipped by the system bars
- [x] 9.2 Confirm dark system‑bar icon contrast and that the scrollable quick‑start list draws correctly behind the bars

## 10. Verification

- [x] 10.1 Run unit tests for duration parsing and time formatting
- [x] 10.2 On a device/emulator (`android run`, `android screenshot`): start via field and via quick‑start; confirm the running screen, the live countdown notification, backgrounding survival, and Cancel
- [x] 10.3 Verify completion fires on time with the screen off, the full‑screen alarm appears over the lock screen and turns the screen on, sound + vibration play, and it stops after 30 s or on Cancel

## 11. Documentation

- [x] 11.1 Create a root `README.md` describing EasyNap — purpose, the motto "The nap, at its simplest", core features (custom + quick‑start durations, foreground countdown notification, full‑screen lock‑screen alarm with 30 s sound + vibration), and build/run instructions
- [x] 11.2 Keep `README.md` in sync with behavior — update it in the same change whenever a user‑facing capability changes
