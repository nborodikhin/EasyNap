## Why

There is no app yet — EasyNap is a new, single-purpose Android nap timer (package `me.easynap`). People taking a short nap need a dead-simple way to start a countdown and be reliably woken by a loud, unmissable alarm, even if the phone locks, the screen turns off, or the app is backgrounded. This change creates the first working version of the app. The project's guiding motto — *"The nap, at its simplest"* — keeps the scope deliberately minimal.

## What Changes

- Scaffold a new Jetpack Compose Android app (`me.easynap`) using Material 3 and a dark theme matching the reference layout.
- Add a **main/setup screen**: a "Timer duration (minutes)" float input, a "Start Timer" button, an "Or quick start:" label, and a vertical list of quick-start buttons in 5‑minute increments from 5 up to 60 minutes.
- Add a **running-timer screen** that shows the live remaining time and a "Cancel" button; the app opens directly to this screen whenever a timer is already active.
- Run a **foreground service** for the lifetime of an active timer that keeps a persistent notification updated with the live countdown, so the timer survives the app being backgrounded or closed.
- Schedule the timer's completion with an exact, Doze-resistant alarm so it fires on time even when the device is idle.
- On completion, launch a **full-screen alarm activity** that appears over the lock screen, plays the system alarm sound, and vibrates for 30 seconds or until the user presses Cancel — whichever comes first.
- Add and maintain a root **README** that describes the app and carries the project motto.

## Capabilities

### New Capabilities
- `timer-setup`: The main screen for choosing a nap duration (free-form minutes entry plus 5‑minute quick-start buttons) and starting a timer.
- `countdown-timer`: The running-timer lifecycle — starting, tracking, and cancelling a single active countdown via a foreground service with a live countdown notification, and landing the app on the running-timer screen when a timer is active.
- `nap-alarm`: The completion alarm experience — a full-screen, shows-over-lock-screen activity that plays the system alarm sound and vibrates for up to 30 seconds, dismissible by Cancel.
- `project-readme`: A maintained root `README.md` that describes the app and its usage, carries the project motto ("The nap, at its simplest"), and is kept in sync with the app's behavior over time.

### Modified Capabilities
- None (greenfield app; no existing specs).

## Impact

- **New project**: Gradle/Kotlin Android project scaffolded with the `android` CLI; Jetpack Compose + Material 3; `minSdk 26`, `targetSdk 35` (edge-to-edge required at 35+).
- **New components**: `MainActivity` (Compose, setup + running screens), `AlarmActivity` (full-screen alarm over lock screen), a foreground timer service, an alarm receiver, and a small timer state holder shared between UI and service.
- **Manifest / permissions**: `FOREGROUND_SERVICE` (+ a declared service type), `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `VIBRATE`, `WAKE_LOCK`, and an exact-alarm permission for the scheduled completion.
- **System integration**: notification channels, `AlarmManager` exact alarm, `Ringtone`/alarm audio, and the `Vibrator`.
- **Documentation**: a root `README.md` describing the app (with the motto "The nap, at its simplest"), kept updated whenever user-facing behavior changes.
- **Out of scope (non-goals)**: surviving a device reboot mid-nap, multiple concurrent timers, custom alarm sounds, history/stats, and adaptive tablet/foldable/multi-pane layouts.
