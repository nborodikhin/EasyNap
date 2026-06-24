## ADDED Requirements

### Requirement: Async persisted countdown state
The countdown controller and foreground service SHALL load active timer state through DataStore-backed asynchronous access while preserving existing countdown behavior.

#### Scenario: App restores active timer from DataStore
- **WHEN** the app starts and a persisted future timer end time exists
- **THEN** the app restores the running timer state from DataStore-backed persisted state

#### Scenario: Foreground service avoids SharedPreferences disk reads
- **WHEN** the countdown foreground service starts
- **THEN** it loads the persisted timer end time without directly reading SharedPreferences synchronously
