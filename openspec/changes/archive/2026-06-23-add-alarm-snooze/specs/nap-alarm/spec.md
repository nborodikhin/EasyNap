## ADDED Requirements

### Requirement: Back navigation stops alarm
The alarm activity SHALL treat any back navigation event while the alarm is active the same as the Stop action: the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state.

#### Scenario: Back gesture stops active alarm
- **WHEN** the alarm is active and the user performs the system back gesture
- **THEN** the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state

#### Scenario: Back button stops active alarm
- **WHEN** the alarm is active and the user presses a system back button
- **THEN** the alarm sound and vibration stop immediately, the alarm activity finishes, and the app returns to its idle setup state

### Requirement: Snooze alarm
The alarm activity SHALL provide snooze controls for `+1 min`, `+5 min`, and `+10 min`. Choosing a snooze control SHALL stop the current alarm sound and vibration, finish the current alarm state, and start a new countdown for the selected snooze duration.

#### Scenario: Snooze for one minute
- **WHEN** the alarm is active and the user taps "+1 min"
- **THEN** the alarm sound and vibration stop and a new 1-minute countdown starts

#### Scenario: Snooze for five minutes
- **WHEN** the alarm is active and the user taps "+5 min"
- **THEN** the alarm sound and vibration stop and a new 5-minute countdown starts

#### Scenario: Snooze for ten minutes
- **WHEN** the alarm is active and the user taps "+10 min"
- **THEN** the alarm sound and vibration stop and a new 10-minute countdown starts

#### Scenario: Snooze shows countdown
- **WHEN** the user selects any snooze duration
- **THEN** the app shows the running‑timer screen for the newly started snooze countdown
