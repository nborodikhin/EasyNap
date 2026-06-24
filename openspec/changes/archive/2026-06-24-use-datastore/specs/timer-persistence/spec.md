## ADDED Requirements

### Requirement: DataStore-backed timer persistence
The system SHALL persist active timer end time, selected nap duration, and recent duration history using Jetpack DataStore instead of direct SharedPreferences access.

#### Scenario: Timer state is persisted
- **WHEN** a timer is started
- **THEN** the selected nap duration and target end time are persisted through DataStore

#### Scenario: Recent history is persisted
- **WHEN** a nap duration is added to recent history
- **THEN** the updated history is persisted through DataStore

### Requirement: No synchronous main-thread preference reads
The system SHALL NOT perform synchronous SharedPreferences disk reads from UI-facing or service startup paths for timer state, nap duration, or duration history.

#### Scenario: Setup screen loads history asynchronously
- **WHEN** the setup screen displays recent durations
- **THEN** it obtains duration history through DataStore-backed asynchronous state rather than a synchronous disk read

#### Scenario: Alarm screen loads duration asynchronously
- **WHEN** the alarm screen displays the completed nap duration
- **THEN** it obtains the nap duration through DataStore-backed asynchronous state rather than a synchronous disk read

#### Scenario: Countdown service loads active timer asynchronously
- **WHEN** the countdown foreground service starts
- **THEN** it obtains the active timer end time through DataStore-backed asynchronous access rather than direct SharedPreferences reads

### Requirement: SharedPreferences migration
The system SHALL migrate existing SharedPreferences timer values into DataStore so users do not lose active timer, nap duration, or duration history data during upgrade.

#### Scenario: Existing active timer is migrated
- **WHEN** the app launches after upgrading from a version that stored `end_at_millis` in SharedPreferences
- **THEN** the DataStore-backed state includes that active timer end time if it is still in the future

#### Scenario: Existing duration history is migrated
- **WHEN** the app launches after upgrading from a version that stored `duration_history` in SharedPreferences
- **THEN** the DataStore-backed state includes the migrated recent duration history
