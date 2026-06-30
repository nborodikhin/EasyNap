## MODIFIED Requirements

### Requirement: Alarm wake-up presentation
The alarm activity SHALL present the handoff wake-up layout, including a completed-ring visual, the title "Time to wake up", and body copy that includes the completed nap duration. The completed nap duration SHALL be read from `TimerController`'s integer-seconds duration state and converted to a display label using the same minute/second display logic used elsewhere in the app (whole minutes for exact multiples of 60, seconds otherwise).

#### Scenario: Alarm displays wake-up copy
- **WHEN** the alarm activity is displayed for a completed 20-minute nap
- **THEN** it shows a completed-ring visual, the title "Time to wake up", body copy indicating that the 20-minute nap is done, and a "Stop" control

### Requirement: Snooze alarm
The alarm activity SHALL provide snooze controls for `+1 min`, `+5 min`, and `+10 min`. Choosing a snooze control SHALL stop the current alarm sound and vibration, finish the current alarm state, and start a new countdown for the selected snooze duration expressed as integer seconds (60, 300, or 600). In addition, pressing the volume-up or volume-down key while the alarm is active SHALL trigger a 1-minute snooze (60 seconds), identical in effect to tapping "+1 min".

#### Scenario: Snooze for one minute
- **WHEN** the alarm is active and the user taps "+1 min"
- **THEN** the alarm sound and vibration stop and a new 60-second countdown starts

#### Scenario: Snooze for five minutes
- **WHEN** the alarm is active and the user taps "+5 min"
- **THEN** the alarm sound and vibration stop and a new 300-second countdown starts

#### Scenario: Snooze for ten minutes
- **WHEN** the alarm is active and the user taps "+10 min"
- **THEN** the alarm sound and vibration stop and a new 600-second countdown starts

#### Scenario: Volume key snoozes for one minute
- **WHEN** the alarm is active and the user presses the volume-up or volume-down key
- **THEN** the alarm sound and vibration stop and a new 60-second countdown starts

#### Scenario: Snooze shows countdown
- **WHEN** the user selects any snooze duration
- **THEN** the app shows the running‑timer screen for the newly started snooze countdown
