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
2. **5 s** — the default system alarm sound starts at volume 0 and rises linearly to full volume over the next 10 seconds.
3. **15 s** — the sound plays at full volume.
4. **45 s** — the sound fades back to volume 0 linearly over 10 seconds.
5. **55 s** — the alarm auto‑stops: sound and vibration end and the alarm activity finishes.

#### Scenario: Vibration runs throughout
- **WHEN** the alarm is active
- **THEN** the device vibrates continuously from the moment the alarm starts until it ends (whether by timeout or Cancel)

#### Scenario: Sound fades in after initial vibration
- **WHEN** 5 seconds have elapsed since the alarm started
- **THEN** the alarm sound begins at zero volume and rises linearly to full volume over the following 10 seconds

#### Scenario: Sound fades out before auto‑stop
- **WHEN** 45 seconds have elapsed since the alarm started
- **THEN** the alarm sound begins to decrease linearly from full volume to zero over the following 10 seconds

### Requirement: Auto‑dismiss after 55 seconds
The alarm SHALL stop its sound and vibration and finish automatically 55 seconds after it begins if the user has not cancelled it.

#### Scenario: Alarm times out
- **WHEN** the alarm has been running for 55 seconds without being cancelled
- **THEN** the sound and vibration stop and the alarm activity finishes

### Requirement: Cancel stops the alarm immediately
The alarm activity SHALL provide a "Stop" control that immediately stops the sound and vibration and finishes the activity. This requirement preserves the existing stop behavior while changing the user-facing action label from "Cancel" to "Stop".

#### Scenario: User stops the alarm
- **WHEN** the user presses "Stop" while the alarm is active
- **THEN** the sound and vibration stop immediately and the alarm activity finishes

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
