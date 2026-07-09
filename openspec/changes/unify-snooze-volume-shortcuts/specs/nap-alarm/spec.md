## MODIFIED Requirements

### Requirement: Snooze alarm
The alarm activity SHALL provide snooze controls for the configured ordered snooze options (initially 1 minute, 5 minutes, and 10 minutes). Choosing a snooze control SHALL stop the current alarm sound and vibration, finish the current alarm state, and start a new countdown for the selected snooze duration expressed as integer seconds (initially 60, 300, or 600). In addition, pressing the volume-down key while the alarm is active SHALL trigger the first configured snooze option, identical in effect to tapping the first snooze control, and pressing the volume-up key while the alarm is active SHALL trigger the second configured snooze option, identical in effect to tapping the second snooze control.

#### Scenario: Snooze for one minute
- **WHEN** the alarm is active and the user taps the 1-minute snooze control
- **THEN** the alarm sound and vibration stop and a new 60-second countdown starts

#### Scenario: Snooze for five minutes
- **WHEN** the alarm is active and the user taps the 5-minute snooze control
- **THEN** the alarm sound and vibration stop and a new 300-second countdown starts

#### Scenario: Snooze for ten minutes
- **WHEN** the alarm is active and the user taps the 10-minute snooze control
- **THEN** the alarm sound and vibration stop and a new 600-second countdown starts

#### Scenario: Volume down snoozes using first option
- **WHEN** the alarm is active and the user presses the volume-down key
- **THEN** the alarm sound and vibration stop and a new countdown starts for the first configured snooze option

#### Scenario: Volume up snoozes using second option
- **WHEN** the alarm is active and the user presses the volume-up key
- **THEN** the alarm sound and vibration stop and a new countdown starts for the second configured snooze option

#### Scenario: Snooze shows countdown
- **WHEN** the user selects any snooze duration
- **THEN** the app shows the running-timer screen for the newly started snooze countdown

### Requirement: Alarm wake-up presentation
The alarm activity SHALL present the handoff wake-up layout, including a completed-ring visual, the title "Time to wake up", body copy that includes the completed nap duration, and snooze controls. The completed nap duration SHALL be read from `TimerController`'s integer-seconds duration state and converted to a display label using the same minute/second display logic used elsewhere in the app (whole minutes for exact multiples of 60, seconds otherwise). Snooze controls SHALL use the same duration label treatment as main-screen duration tiles: a prominent number with `min` or `sec` on the second line, without a leading `+`. The first snooze control SHALL show a localized `Vol-` shortcut label below its button, the second snooze control SHALL show a localized `Vol+` shortcut label below its button, and snooze controls without hardware shortcuts SHALL not reserve blank shortcut-label space.

#### Scenario: Alarm displays wake-up copy
- **WHEN** the alarm activity is displayed for a completed 20-minute nap
- **THEN** it shows a completed-ring visual, the title "Time to wake up", body copy indicating that the 20-minute nap is done, and a "Stop" control

#### Scenario: Alarm displays volume shortcut labels
- **WHEN** the alarm activity displays snooze controls
- **THEN** each snooze control shows a prominent duration number with `min` or `sec` on the second line and no leading `+`, the first snooze control shows a localized `Vol-` shortcut label below its button, the second snooze control shows a localized `Vol+` shortcut label below its button, and the third snooze control does not reserve blank shortcut-label space
