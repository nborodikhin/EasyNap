## MODIFIED Requirements

### Requirement: DataStore-backed timer persistence
The system SHALL persist active timer end time, selected nap duration, and recent duration history using Jetpack DataStore instead of direct SharedPreferences access. The selected nap duration SHALL be stored as an integer number of seconds using the key `nap_duration_seconds`. Recent duration history SHALL be stored as a comma-separated string of integer seconds using the key `duration_history_seconds`.

#### Scenario: Timer state is persisted
- **WHEN** a timer is started
- **THEN** the selected nap duration (as integer seconds) and target end time are persisted through DataStore

#### Scenario: Recent history is persisted
- **WHEN** a nap duration is added to recent history
- **THEN** the updated history (as integer seconds) is persisted through DataStore

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

## REMOVED Requirements

### Requirement: SharedPreferences migration
**Reason:** The duration keys being replaced (`nap_duration_minutes`, `duration_history`) used a float-minute representation incompatible with the new integer-second keys. Migrating them would require a format conversion that is error-prone and unnecessary given that no live users exist.
**Migration:** Upgraded installs treat the absence of `nap_duration_seconds` and `duration_history_seconds` keys as a fresh install: the selected duration defaults to 0 and history defaults to the seed values (5 min, 10 min, 30 min). The `end_at_millis` SharedPreferences key remains unaffected; any existing active timer end time still reaches the DataStore via the existing SharedPreferences migration path.
