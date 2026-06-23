## Context

EasyNap is a greenfield Android app (`me.easynap`). Its whole job is: pick a short duration, count down reliably, and fire a loud, unmissable, lock‑screen alarm at zero. The hard parts are not the UI — they are Android's background‑execution and lock‑screen constraints: the timer must keep running when the app is closed and the screen is off, it must fire on time even in Doze, and the alarm must appear over the lock screen and turn the screen on. This document records the architecture and the platform decisions that make those guarantees hold.

Project is scaffolded with the `android` CLI (`android create empty-activity`), giving a Kotlin + Jetpack Compose + Material 3 project. Per the edge-to-edge skill, the app targets SDK 35+ and must be edge‑to‑edge. `minSdk` is 26 (notification channels + modern foreground services). Up‑to‑date API specifics (foreground‑service types, full‑screen intent rules) should be confirmed during implementation with `android docs search`.

## Goals / Non-Goals

**Goals:**
- A single active nap timer that survives app backgrounding/close and a screen‑off device.
- Fire completion **on time** even under Doze / idle.
- A full‑screen alarm shown over the lock screen that turns the screen on, plays the system alarm sound, and vibrates for 30 s or until Cancel.
- A clean dark‑themed Compose UI matching the reference (duration field, Start, quick‑start list).

**Non-Goals:**
- Surviving a **device reboot** mid‑nap (no `BOOT_COMPLETED` re‑scheduling — naps are short).
- Multiple concurrent timers, custom alarm sounds, snooze, or nap history/stats.
- Adaptive tablet/foldable/multi‑pane layouts (phone‑portrait first).

## Decisions

### Component map
- `MainActivity` (Compose, edge‑to‑edge): renders `SetupScreen` when idle and `RunningScreen` when a timer is active, chosen by observed timer state.
- `SetupScreen` / `RunningScreen` (composables) + a `MainViewModel` exposing UI state and parsing duration input.
- `TimerController` (singleton/repository): the single source of truth. Holds a `StateFlow<TimerState>` (`Idle` / `Running(endAtMillis)`), persists `endAtMillis` to `SharedPreferences`/DataStore, and on `start()` launches the countdown service + schedules the completion alarm; on `cancel()` tears all of that down.
- `NapTimerService` (foreground service, type `specialUse`): while running, ticks ~1 Hz, computes remaining time from the persisted `endAtMillis`, and updates the ongoing countdown notification and the shared state.
- `AlarmReceiver` (BroadcastReceiver): target of the `AlarmManager` PendingIntent; starts `AlarmService` on fire.
- `AlarmService` (foreground service, type `mediaPlayback`): acquires a wake lock, plays the default alarm `Ringtone` on `STREAM_ALARM`, vibrates, posts a high‑importance full‑screen‑intent notification that launches `AlarmActivity`, and enforces the 30 s auto‑stop.
- `AlarmActivity` (full‑screen, `setShowWhenLocked(true)` + `setTurnScreenOn(true)`): the "time's up" UI with a Cancel button; Cancel stops `AlarmService`.

### Decision: `AlarmManager.setAlarmClock()` for completion (not WorkManager / in‑process timer)
The completion is scheduled with `AlarmManager.setAlarmClock()`. **Why:** it is exempt from Doze, fires at an exact wall‑clock time, surfaces the upcoming alarm in the status bar, and — unlike `setExactAndAllowWhileIdle()` — needs no `SCHEDULE_EXACT_ALARM` runtime grant. **Alternatives rejected:** WorkManager (can be deferred minutes — unacceptable for an alarm); an in‑process `CountDownTimer`/`Handler` as the sole timer (dies when the app/process is killed and is throttled in Doze). The foreground service's own ticking is only for the live notification UI, never the authority on *when* the alarm fires.

### Decision: timestamp is the source of truth (not a decrementing counter)
`TimerController` persists an absolute `endAtMillis`. The notification and `RunningScreen` derive remaining time as `endAtMillis - now`. **Why:** robust against process death, service restarts, and missed ticks — any consumer can recompute the truth at any moment. The service uses `START_STICKY`/redelivery and rebuilds state from the persisted timestamp. For durations of 1 minute or more, `endAtMillis` includes a +5 s pad so a freshly started whole‑minute timer doesn't immediately display mm:59; sub‑minute durations are left unpadded.

### Decision: separate countdown and alarm foreground services
`NapTimerService` (`specialUse`, silent) and `AlarmService` (`mediaPlayback`, audio + wake lock + 30 s) are distinct. **Why:** they have different foreground‑service types and lifecycles; swapping a service's type mid‑flight is awkward. On completion the countdown service stops and `AlarmReceiver` starts the alarm service.

### Decision: full‑screen‑intent **notification**, with playback in the service (not direct `startActivity`)
The alarm is surfaced via a high‑importance notification (category `CATEGORY_ALARM`) with `setFullScreenIntent(piToAlarmActivity, true)`, and the **sound/vibration live in `AlarmService`, not in the activity**. **Why:** Background activity‑launch restrictions (Android 10+) forbid starting an Activity from the background; the full‑screen intent is the sanctioned path. And on Android 14+ a full‑screen intent may degrade to a heads‑up notification when `USE_FULL_SCREEN_INTENT` isn't honored — keeping playback in the service guarantees the user is still alerted audibly even if the activity never comes to the foreground. The 30 s auto‑stop and Cancel both route through the service so sound, vibration, and the activity stop together.

### Decision: state‑driven screen selection, not a navigation library
`MainActivity` shows Setup vs Running purely as a function of `TimerState`. **Why:** the requirement "open the app while a timer is active → land on the running screen" is conditional navigation driven by state, not a user back stack; this is simpler than pulling in Navigation 3. Navigation 3 (skill available) can be adopted later if the screen graph grows. `AlarmActivity` is a separate Activity because it is launched independently from a notification with lock‑screen window flags.

### Decision: permissions & manifest
Declared: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `VIBRATE`, `WAKE_LOCK`. `POST_NOTIFICATIONS` is requested at runtime (Android 13+). Two notification channels: a low‑importance silent **Timer** channel and a high‑importance **Alarm** channel (own sound suppressed at the channel since the service plays it). Edge‑to‑edge: `enableEdgeToEdge()` in both activities, `windowSoftInputMode="adjustResize"` for the duration field, `Scaffold` + `WindowInsets.safeDrawing`.

## Risks / Trade-offs

- **`USE_FULL_SCREEN_INTENT` restricted on Android 14+** → The app's primary purpose is an alarm clock, which qualifies for the auto‑grant; additionally playback lives in `AlarmService` so the user is alerted even if the intent degrades to a heads‑up. Optionally deep‑link the user to the full‑screen‑intent setting if not granted.
- **`POST_NOTIFICATIONS` denied (Android 13+)** → The foreground service still runs and the alarm still fires; only the visible countdown notification is suppressed. Request the permission up front; never make completion depend on the notification being visible.
- **OEM battery killers / aggressive Doze** → `setAlarmClock()` is Doze‑exempt and the foreground service keeps the process warm; even if the process is killed, the alarm wakes it. Reboot is an accepted non‑goal.
- **Wall‑clock changes** → completion and UI both derive from the same RTC `endAtMillis`; acceptable for nap‑length durations.
- **Hard to automate alarm/lock‑screen behavior in CI** → cover duration parsing and remaining‑time formatting with unit tests; verify the lock‑screen/full‑screen/Doze paths manually on a device/emulator (`android run`, `android screenshot`). The `testing-setup` skill can scaffold the harness.
