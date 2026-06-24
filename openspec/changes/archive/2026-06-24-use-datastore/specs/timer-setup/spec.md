## MODIFIED Requirements

### Requirement: Quick‑start buttons
The main screen SHALL display a compact quick duration table containing up to 6 distinct recent nap durations, with no "RECENT" or "PRESETS" section titles. Before the user has enough history, the table SHALL be pre-seeded with 5, 10, and 30 minute durations. Duration history SHALL be loaded and updated through DataStore-backed asynchronous state rather than synchronous SharedPreferences reads.

#### Scenario: Seeded quick durations are shown
- **WHEN** the main screen is shown before the user has any saved nap duration history
- **THEN** the quick duration table includes 5, 10, and 30 minute durations

#### Scenario: Tapping a quick duration starts a timer
- **WHEN** the user taps a duration in the quick duration table
- **THEN** a countdown of that duration starts immediately, regardless of the text field contents, and the app shows the running‑timer screen

#### Scenario: Quick duration table is capped
- **WHEN** more than 6 last nap durations are available
- **THEN** the main screen displays no more than 6 distinct durations in the quick duration table

#### Scenario: Duplicate duration moves to front
- **WHEN** the user starts a nap duration that is already present in the quick duration table
- **THEN** that duration is moved to the newest position rather than duplicated

#### Scenario: History does not block on disk
- **WHEN** the setup screen loads duration history
- **THEN** it collects DataStore-backed asynchronous state instead of calling a synchronous disk-backed preference read
