## ADDED Requirements

### Requirement: Start countdown from snooze
After an alarm fires, the system SHALL allow the alarm snooze action to start a new countdown for the selected snooze duration using the same single-active-timer rules and foreground countdown service behavior as a normal timer start.

#### Scenario: Snooze starts foreground countdown
- **WHEN** the user selects a snooze duration from the alarm screen
- **THEN** the system starts a new countdown for that duration and maintains the foreground countdown service and ongoing notification

#### Scenario: Snooze countdown remains single source of truth
- **WHEN** a snooze countdown is started
- **THEN** the completed alarm state is cleared and the snooze countdown becomes the single active timer
