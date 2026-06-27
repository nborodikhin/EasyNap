## 1. Icons & strings

- [x] 1.1 Confirm the `ic_alarm_off_24` and `ic_snooze_24dp` vector drawables exist in `res/drawable` (already generated).
- [x] 1.2 Add notification strings: countdown title `%1$s nap is active`, alarm title `%1$s nap is over`, and action labels "Stop" and "Snooze". Reuse the existing `notif_alarm_text` ("Time to wake up!") for the alarm body.

## 2. Countdown notification (`NapTimerService`)

- [x] 2.1 Inject `TimerController` into `NapTimerService`.
- [x] 2.2 Set the notification title to `<duration> nap is active` from the active timer's duration label.
- [x] 2.3 Add a "Stop" action (`ic_alarm_off`) whose PendingIntent targets the service with an `ACTION_STOP`, handled by calling `TimerController.cancel()` so the timer, foreground service/notification, and scheduled alarm are all cleared.
- [x] 2.4 Give the Stop action a request code distinct from the content (open-app) intent.

## 3. Alarm notification (`AlarmService`)

- [x] 3.1 Inject `TimerController` into `AlarmService`.
- [x] 3.2 Set the notification title to `<duration> nap is over`; keep the body "Time to wake up!".
- [x] 3.3 Add a "Snooze" action (`ic_snooze`, first) routed to a new `ACTION_SNOOZE`: stop the alarm sound/vibration, broadcast `ACTION_FINISH`, and start a 1-minute countdown via `startSnooze(1f)` — without opening the app.
- [x] 3.4 Add a "Stop" action (`ic_alarm_off`, second) routed to the existing `ACTION_STOP`.
- [x] 3.5 Keep the action order Snooze-then-Stop and give each action a request code distinct from the full-screen intent and from each other.

## 4. Validation

- [x] 4.1 Build the debug Android app.
- [x] 4.2 Manually verify countdown "Stop" cancels the timer **and** that no alarm fires afterward.
- [x] 4.3 Manually verify alarm "Snooze" starts a 1-minute countdown from the shade/lock screen without opening the app.
- [x] 4.4 Manually verify alarm "Stop" stops the alarm from the notification.
- [x] 4.5 Verify both titles render correctly for whole-minute and sub-minute/fractional durations.
