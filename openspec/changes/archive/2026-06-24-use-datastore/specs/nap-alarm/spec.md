## ADDED Requirements

### Requirement: Async persisted alarm duration
The alarm screen SHALL obtain the completed nap duration through DataStore-backed asynchronous state rather than direct synchronous SharedPreferences access.

#### Scenario: Alarm displays persisted duration
- **WHEN** the alarm screen displays the completed nap duration
- **THEN** it uses the DataStore-backed persisted nap duration value

#### Scenario: Alarm duration read avoids main-thread disk access
- **WHEN** the alarm screen is composed or created
- **THEN** it does not synchronously read SharedPreferences from the main thread to obtain the nap duration
