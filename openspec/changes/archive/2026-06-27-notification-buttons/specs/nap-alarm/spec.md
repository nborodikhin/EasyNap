## ADDED Requirements

### Requirement: Alarm notification title and actions
While the alarm is active, its foreground notification SHALL display a title identifying the completed nap as `<duration> nap is over` (e.g. "20 min nap is over", or "1:30 nap is over" for sub‑minute or fractional durations) and body text "Time to wake up!". The notification SHALL provide two actions, presented in the order Snooze, then Stop:

- "Snooze" SHALL snooze the alarm for 1 minute, identical in effect to tapping "+1 min" on the alarm screen: it stops the current alarm sound and vibration, finishes the current alarm state (including any visible alarm screen), and starts a new 1‑minute countdown. It SHALL act directly from the notification without launching the app or requiring the device to be unlocked.
- "Stop" SHALL stop the alarm immediately, identical in effect to the alarm screen's Stop control.

Neither action SHALL duplicate the notification's full‑screen / tap (content) action, which presents the full‑screen alarm screen.

#### Scenario: Alarm notification shows nap title and wake-up text
- **WHEN** the alarm is active for a completed 20‑minute nap
- **THEN** its notification shows the title "20 min nap is over" and the body "Time to wake up!"

#### Scenario: Snooze action precedes Stop action
- **WHEN** the alarm notification is displayed
- **THEN** its actions appear in the order "Snooze" first, then "Stop"

#### Scenario: Notification Snooze snoozes one minute
- **WHEN** the user taps the "Snooze" action on the alarm notification
- **THEN** the alarm sound and vibration stop, any visible alarm screen finishes, and a new 1‑minute countdown starts

#### Scenario: Notification Snooze does not open the app
- **WHEN** the user taps the "Snooze" action on the alarm notification
- **THEN** the snooze countdown starts without launching the app to the foreground

#### Scenario: Notification Stop stops the alarm
- **WHEN** the user taps the "Stop" action on the alarm notification
- **THEN** the alarm sound and vibration stop immediately, any visible alarm screen finishes, and no timer remains active

#### Scenario: Notification actions work from the lock screen
- **WHEN** the device is locked while the alarm is active
- **THEN** the "Snooze" and "Stop" actions can be invoked from the lock-screen notification without unlocking the device
