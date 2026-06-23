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
The alarm activity SHALL provide a "Cancel" control that immediately stops the sound and vibration and finishes the activity.

#### Scenario: User cancels the alarm
- **WHEN** the user presses "Cancel" while the alarm is active
- **THEN** the sound and vibration stop immediately and the alarm activity finishes

#### Scenario: State is clean after the alarm ends
- **WHEN** the alarm finishes, whether by timeout or by Cancel
- **THEN** no timer is active, the countdown notification and foreground service are gone, and the app returns to its idle setup state
