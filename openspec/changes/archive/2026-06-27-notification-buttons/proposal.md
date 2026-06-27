## Why

The countdown and alarm notifications currently expose no actions, so the only way to stop a nap early is to open the app, and the only way to snooze or stop a firing alarm is through the full-screen alarm screen. Adding notification actions lets the user act directly from the shade or lock screen. Per Android guidance, these actions complement — they do not duplicate — the notification's tap (content) action, which opens the corresponding screen. The titles are also generic ("EasyNap"); surfacing the nap's identity makes the active nap and the completed nap legible at a glance.

## What Changes

- **Countdown notification**
  - Title becomes `<duration> nap is active` (e.g. "20 min nap is active", "1:30 nap is active").
  - Add a single **Stop** action that cancels the nap — including its scheduled alarm — without opening the app.
- **Alarm notification**
  - Title becomes `<duration> nap is over`; body stays "Time to wake up!".
  - Add two actions, ordered **Snooze** then **Stop**:
    - **Snooze** snoozes 1 minute (matching the volume-key quick-snooze) and acts from the notification without opening the app.
    - **Stop** stops the alarm, identical to the existing Stop control.
- Reuse `ic_alarm_off` for both Stop actions and `ic_snooze` for Snooze.
- No action duplicates the notification's tap / full-screen intent.

## Capabilities

### Modified Capabilities
- `countdown-timer`: the live countdown notification gains a nap-identifying title and a Stop action that cancels the timer and its scheduled alarm without opening the app.
- `nap-alarm`: the alarm notification gains a nap-identifying title and Snooze/Stop actions (in that order) operable from the shade and lock screen.

## Impact

- Affected app code: `NapTimerService` (title + Stop action; inject `TimerController`), `AlarmService` (title + Snooze/Stop actions; inject `TimerController`; new `ACTION_SNOOZE`), notification strings, and the `ic_alarm_off` / `ic_snooze` drawables.
- Affected specs: `countdown-timer`, `nap-alarm`.
- This change does not alter the full-screen alarm screen, the in-app `+1/+5/+10` snooze chips, the alarm audio/haptic/auto-dismiss sequence, or countdown timing/padding.
