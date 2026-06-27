## Context

The countdown (`NapTimerService`) and alarm (`AlarmService`) foreground notifications currently carry no actions and a generic "EasyNap" title. Both services are Hilt `@AndroidEntryPoint`. The countdown's completion is fired by an independent `AlarmManager.setAlarmClock` scheduled by `TimerController`; the alarm already supports `AlarmService.ACTION_STOP`. Snooze logic already exists as `TimerController.startSnooze(minutes)`, today driven only from `AlarmActivity`.

## Goals / Non-Goals

**Goals:**
- Add a Stop action to the countdown notification that fully cancels the nap, including its scheduled alarm.
- Add Snooze (+1 min) and Stop actions to the alarm notification, ordered Snooze then Stop.
- Retitle both notifications to identify the nap.
- Keep every action distinct from the notification's tap / full-screen intent.

**Non-Goals:**
- Do not change the full-screen `AlarmActivity` layout or its `+1/+5/+10` snooze chips.
- Do not add a configurable snooze duration for the notification (fixed +1 min).
- Do not change the alarm audio/haptic/auto-dismiss sequence or countdown padding.

## Decisions

- **Alarm action order: Snooze first, Stop second.** Material defines no left/right safety rule for notification actions; the platform reference (AOSP DeskClock's firing-alarm notification) adds Snooze then Stop, and the dialog convention (the terminal/affirmative action on the trailing edge) also lands Stop last. This is intentionally the reverse of the in-app `AlarmActivity` (where Stop is the big primary button), because the notification follows the system-alarm convention users already have muscle memory for.
- **Notification snooze = fixed +1 min**, matching the existing volume-key quick-snooze (`onKeyDown` → `snooze(1f)`). The full `+1/+5/+10` set stays in the activity, where there is room for three chips.
- **Countdown Stop must run the full cancel path** (`TimerController.cancel()`), not a service-local `stopSelf()`. The wake-up is an independent `AlarmManager.setAlarmClock`; killing only the foreground service would leave the alarm to fire anyway. Wire by injecting `TimerController` into `NapTimerService` and handling an `ACTION_STOP` intent.
- **Alarm Stop reuses the existing `AlarmService.ACTION_STOP`.** Alarm Snooze adds a new `ACTION_SNOOZE` that stops the alarm sound/vibration, broadcasts `ACTION_FINISH` (so `AlarmActivity` finishes if it is showing), and starts a 1-minute countdown via `TimerController.startSnooze(1f)`.
- **Notification actions do not open the app.** Countdown Stop returns to idle; alarm Snooze/Stop act directly from the shade or lock screen without launching `MainActivity`. (The in-activity snooze still opens the running countdown screen — that path is unchanged.)
- **Icons: one `ic_alarm_off` for both Stop actions, `ic_snooze` for Snooze.** Countdown Stop also cancels the pending alarm, so "alarm off" is semantically coherent for it too. On Android 12+ action icons are not rendered (label only); the icons matter for Android 7–11, Wear, and some OEM shades.
- **Distinct PendingIntent request codes** per action, to avoid `FLAG_UPDATE_CURRENT` collisions with each other and with the content intent.
- **Title format:** compact duration label plus suffix — `<n> min nap is …` for whole minutes, `m:ss nap is …` for sub-minute/fractional durations. During a snooze the active timer's duration *is* the snooze length, so the countdown title reflects the snooze duration (e.g. "1 min nap is active"). This is accepted as correct.

## Risks / Trade-offs

- **Countdown Stop forgetting to cancel the alarm** → the alarm fires after the user already stopped the nap. Mitigation: route Stop through `TimerController.cancel()` (which cancels the `AlarmManager` alarm); covered by a spec scenario and validation.
- **Starting a foreground countdown service from the snooze action** crosses background-start restrictions. Mitigation: the alarm service is already foreground and the action is user-initiated; reuse the same snooze start path the in-activity snooze uses.
- **Delivering action intents to the services on Android 12+** under background-start limits. Mitigation: the services are already running and the action is user-initiated; use `getForegroundService` where appropriate.
- **Snooze from the notification while `AlarmActivity` is visible** must finish the activity. Mitigation: `ACTION_SNOOZE` broadcasts `ACTION_FINISH`, exactly as the stop path does.
