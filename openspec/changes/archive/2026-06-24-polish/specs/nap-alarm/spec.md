## MODIFIED Requirements

### Requirement: Snooze alarm
The alarm activity SHALL provide snooze controls for `+1 min`, `+5 min`, and `+10 min`. Choosing a snooze control SHALL stop the current alarm sound and vibration, finish the current alarm state, and start a new countdown for the selected snooze duration. In addition, pressing the volume-up or volume-down key while the alarm is active SHALL trigger a 1-minute snooze, identical in effect to tapping "+1 min".

#### Scenario: Volume key snoozes for one minute
- **WHEN** the alarm is active and the user presses the volume-up or volume-down key
- **THEN** the alarm sound and vibration stop and a new 1-minute countdown starts
