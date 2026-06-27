# Nap Alarm

## Purpose

Defines the full-screen alarm that fires when the countdown completes, including its audio/haptic sequence, auto-dismiss behavior, and post-alarm state cleanup.

## Requirements

### Requirement: Full‑screen alarm over the lock screen
When a timer completes, the system SHALL launch a full‑screen alarm activity that turns the screen on and is shown over the lock screen without requiring the device to be unlocked.

#### Scenario: Alarm appears on completion
- **WHEN** the timer completes
- **THEN** a full‑screen alarm activity is displayed, turning the screen on

#### Scenario: Alarm shows while the device is locked
- **WHEN** the device is locked at the moment the timer completes
- **THEN** the full‑screen alarm is shown over the lock screen without the user having to unlock the device

### Requirement: Gradual audible and haptic alert
The alarm SHALL follow a fixed sequence designed to wake the user gently:

1. **0 s** — vibration begins (500 ms on / 500 ms off pattern) and continues for the entire alarm sequence.
2. **5 s** — the system attempts to play an alarm sound, trying the default alarm URI, then the default notification URI, then the default ringtone URI in order. If none can be opened, audio is silently skipped and only vibration continues. When audio is available it starts at volume 0 and rises linearly to full volume over the next 10 seconds.
3. **15 s** — the sound plays at full volume.
4. **45 s** — the sound fades back to volume 0 linearly over 10 seconds.
5. **55 s** — the alarm auto‑stops: sound and vibration end and the alarm activity finishes.

#### Scenario: Vibration runs throughout
- **WHEN** the alarm is active
- **THEN** the device vibrates continuously from the moment the alarm starts until it ends (whether by timeout or Cancel)

#### Scenario: Sound fades in after initial vibration
- **WHEN** 5 seconds have elapsed since the alarm started and a playable system URI is available
- **THEN** the alarm sound begins at zero volume and rises linearly to full volume over the following 10 seconds

#### Scenario: Audio unavailable does not crash
- **WHEN** 5 seconds have elapsed since the alarm started and no system alarm, notification, or ringtone URI can be opened (e.g. on ChromeOS)
- **THEN** the alarm continues with vibration only and the app does not crash

#### Scenario: Sound fades out before auto‑stop
- **WHEN** 45 seconds have elapsed since the alarm started
- **THEN** the alarm sound begins to decrease linearly from full volume to zero over the following 10 seconds

### Requirement: Auto‑dismiss after 55 seconds
The alarm SHALL stop its sound and vibration and finish automatically 55 seconds after it begins if the user has not cancelled it.

#### Scenario: Alarm times out
- **WHEN** the alarm has been running for 55 seconds without being cancelled
- **THEN** the sound and vibration stop and the alarm activity finishes

### Requirement: Cancel stops the alarm immediately
The alarm activity SHALL provide a "Stop" control that immediately stops the sound and vibration and finishes the activity. On platforms where the system notification presenter fires the alarm activity's `fullScreenIntent` when the foreground notification is removed (e.g. ChromeOS ARC), any spuriously re-launched alarm activity instance SHALL be dismissed immediately rather than presenting the alarm UI again.

#### Scenario: User stops the alarm
- **WHEN** the user presses "Stop" while the alarm is active
- **THEN** the sound and vibration stop immediately and the alarm activity finishes

#### Scenario: Spurious re-launch after stop is dismissed
- **WHEN** the platform re-launches the alarm activity after the alarm has already been stopped (e.g. ChromeOS fires the fullScreenIntent on notification removal)
- **THEN** the re-launched activity immediately finishes without showing the alarm UI

#### Scenario: State is clean after the alarm ends
- **WHEN** the alarm finishes, whether by timeout or by Stop
- **THEN** no timer is active, the countdown notification and foreground service are gone, and the app returns to its idle setup state

### Requirement: Alarm wake-up presentation
The alarm activity SHALL present the handoff wake-up layout, including a completed-ring visual, the title "Time to wake up", and body copy that includes the completed nap duration.

#### Scenario: Alarm displays wake-up copy
- **WHEN** the alarm activity is displayed for a completed 20-minute nap
- **THEN** it shows a completed-ring visual, the title "Time to wake up", body copy indicating that the 20-minute nap is done, and a "Stop" control

### Requirement: Async persisted alarm duration
The alarm screen SHALL obtain the completed nap duration through DataStore-backed asynchronous state rather than direct synchronous SharedPreferences access.

#### Scenario: Alarm displays persisted duration
- **WHEN** the alarm screen displays the completed nap duration
- **THEN** it uses the DataStore-backed persisted nap duration value

#### Scenario: Alarm duration read avoids main-thread disk access
- **WHEN** the alarm screen is composed or created
- **THEN** it does not synchronously read SharedPreferences from the main thread to obtain the nap duration

### Requirement: Back navigation stops alarm
The alarm activity SHALL treat any back navigation event while the alarm is active the same as the Stop action: the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state.

#### Scenario: Back gesture stops active alarm
- **WHEN** the alarm is active and the user performs the system back gesture
- **THEN** the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state

#### Scenario: Back button stops active alarm
- **WHEN** the alarm is active and the user presses a system back button
- **THEN** the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state

### Requirement: Snooze alarm
The alarm activity SHALL provide snooze controls for `+1 min`, `+5 min`, and `+10 min`. Choosing a snooze control SHALL stop the current alarm sound and vibration, finish the current alarm state, and start a new countdown for the selected snooze duration. In addition, pressing the volume-up or volume-down key while the alarm is active SHALL trigger a 1-minute snooze, identical in effect to tapping "+1 min".

#### Scenario: Snooze for one minute
- **WHEN** the alarm is active and the user taps "+1 min"
- **THEN** the alarm sound and vibration stop and a new 1-minute countdown starts

#### Scenario: Snooze for five minutes
- **WHEN** the alarm is active and the user taps "+5 min"
- **THEN** the alarm sound and vibration stop and a new 5-minute countdown starts

#### Scenario: Snooze for ten minutes
- **WHEN** the alarm is active and the user taps "+10 min"
- **THEN** the alarm sound and vibration stop and a new 10-minute countdown starts

#### Scenario: Volume key snoozes for one minute
- **WHEN** the alarm is active and the user presses the volume-up or volume-down key
- **THEN** the alarm sound and vibration stop and a new 1-minute countdown starts

#### Scenario: Snooze shows countdown
- **WHEN** the user selects any snooze duration
- **THEN** the app shows the running‑timer screen for the newly started snooze countdown

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

### Requirement: Alarm notification availability check
Before the user relies on a nap alarm, the system SHALL be able to determine whether alarm notifications are effectively available. Alarm notifications SHALL be considered available only when app notifications are enabled and the alarm notification channel importance is not `IMPORTANCE_NONE`.

#### Scenario: App notifications disabled makes alarm notifications unavailable
- **WHEN** app notifications are disabled for EasyNap
- **THEN** alarm notifications are treated as unavailable

#### Scenario: Alarm channel blocked makes alarm notifications unavailable
- **WHEN** app notifications are enabled but the alarm notification channel has `IMPORTANCE_NONE`
- **THEN** alarm notifications are treated as unavailable

#### Scenario: App and alarm channel enabled makes alarm notifications available
- **WHEN** app notifications are enabled and the alarm notification channel importance is not `IMPORTANCE_NONE`
- **THEN** alarm notifications are treated as available

### Requirement: Alarm notification channels exist before availability checks
The system SHALL create the timer and alarm notification channels before checking alarm notification availability on the setup screen.

#### Scenario: Fresh install can evaluate alarm channel state
- **WHEN** the app opens to the setup screen after a fresh install
- **THEN** the alarm notification channel exists before alarm notification availability is checked

### Requirement: Alarm screen reachable when alarm service is active
Whenever `AlarmService` is running, the alarm screen SHALL be reachable by the user. If `AlarmActivity` is not visible when the user brings the app to the foreground, `MainActivity` SHALL launch `AlarmActivity` automatically.

#### Scenario: AlarmActivity never appeared due to BAL restriction
- **WHEN** `AlarmService` is running and `AlarmActivity` is not visible and the user opens `MainActivity`
- **THEN** `MainActivity` launches `AlarmActivity` so the user can stop or snooze the alarm

#### Scenario: AlarmActivity was killed and user reopens the app
- **WHEN** `AlarmService` is running and the user previously dismissed `AlarmActivity` from recents and then opens `MainActivity`
- **THEN** `MainActivity` launches `AlarmActivity` so the user can stop or snooze the alarm

### Requirement: Activity-side alarm launch when app is foreground at expiry
When `MainActivity` is in the foreground while a nap timer is running, it SHALL launch `AlarmActivity` directly when the timer expires, as a fallback in case the service's own background activity launch is blocked.

#### Scenario: Timer expires while MainActivity is foreground
- **WHEN** a nap timer is running and `MainActivity` is in the foreground and the timer reaches its end time
- **THEN** `MainActivity` launches `AlarmActivity` directly without waiting for `AlarmService` to do so

#### Scenario: Duplicate launch from both service and activity is safe
- **WHEN** both `AlarmService` and `MainActivity` launch `AlarmActivity` at approximately the same time
- **THEN** only one `AlarmActivity` instance is shown and the alarm audio and haptic sequence runs normally

### Requirement: Alarm opens in the existing app window in multi-window environments
In multi-window environments (ChromeOS, tablet split-screen), the alarm activity SHALL open inside the existing app task and window rather than launching as a separate OS-level window. The activity SHALL use `launchMode="singleTop"` so it can share the app task while still preventing duplicate instances.

#### Scenario: Alarm fires on ChromeOS while app is open
- **WHEN** the app is open in a window on ChromeOS and the timer completes
- **THEN** the alarm screen opens inside the same window, not in a new separate OS window

#### Scenario: No duplicate alarm instance when already at top
- **WHEN** the alarm activity is already at the top of the task stack and the system attempts to launch it again
- **THEN** no new instance is created and the existing alarm screen remains visible
