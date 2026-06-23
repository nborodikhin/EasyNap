## MODIFIED Requirements

### Requirement: Cancel stops the alarm immediately
The alarm activity SHALL provide a "Stop" control that immediately stops the sound and vibration and finishes the activity. This requirement preserves the existing stop behavior while changing the user-facing action label from "Cancel" to "Stop".

#### Scenario: User stops the alarm
- **WHEN** the user presses "Stop" while the alarm is active
- **THEN** the sound and vibration stop immediately and the alarm activity finishes

#### Scenario: State is clean after the alarm ends
- **WHEN** the alarm finishes, whether by timeout or by Stop
- **THEN** no timer is active, the countdown notification and foreground service are gone, and the app returns to its idle setup state

## ADDED Requirements

### Requirement: Alarm wake-up presentation
The alarm activity SHALL present the handoff wake-up layout, including a completed-ring visual, the title "Time to wake up", and body copy that includes the completed nap duration.

#### Scenario: Alarm displays wake-up copy
- **WHEN** the alarm activity is displayed for a completed 20-minute nap
- **THEN** it shows a completed-ring visual, the title "Time to wake up", body copy indicating that the 20-minute nap is done, and a "Stop" control
