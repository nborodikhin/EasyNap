## MODIFIED Requirements

### Requirement: Duration selection grid
The main screen SHALL display a single unified grid of duration tiles without separate section labels. A Custom tile SHALL appear as the last tile in the grid. Tapping any duration tile starts a nap immediately. Duration history SHALL be loaded through DataStore-backed asynchronous state rather than synchronous SharedPreferences reads.

On first launch, before any timer has been recorded, the grid SHALL display the default seed durations (5, 10, and 30 minutes). Once history has been written to DataStore, only the stored durations are shown — default seeds are never re-injected to fill gaps.

Duration history and the selected duration SHALL be represented as integer seconds in all in-memory and persisted state. Tile labels SHALL continue to display whole minutes for durations that are exact multiples of 60 seconds, and `mm:ss` for sub-minute or fractional-minute durations.

#### Scenario: Unified grid shows recorded durations
- **WHEN** the app is opened, no timer is active, and the user has previously recorded nap durations
- **THEN** a single grid of those recorded duration tiles is displayed with no "RECENT" or "PRESETS" section labels

#### Scenario: First launch shows default durations
- **WHEN** the app is opened and no nap duration has ever been recorded
- **THEN** the grid displays the default seed durations (5, 10, and 30 minutes)

#### Scenario: Custom tile is always present
- **WHEN** the app is opened and no timer is active
- **THEN** a Custom tile appears as the last tile in the grid

#### Scenario: Tapping a quick duration starts a timer
- **WHEN** the user taps a duration tile in the grid
- **THEN** a countdown of that duration starts immediately and the app shows the running‑timer screen

#### Scenario: Grid is capped
- **WHEN** nap duration history is available
- **THEN** the main screen displays at most 5 distinct durations in the grid (plus the Custom tile), for a maximum of 6 tiles total across 2 rows

#### Scenario: Duplicate duration moves to front
- **WHEN** the user starts a nap duration that is already present in the grid
- **THEN** that duration is moved to the newest position rather than duplicated

#### Scenario: History does not block on disk
- **WHEN** the setup screen loads duration history
- **THEN** it collects DataStore-backed asynchronous state instead of calling a synchronous disk-backed preference read

### Requirement: Start a timer from the entered duration
The custom duration bottom sheet SHALL provide a "Start nap" button that starts a countdown for the valid duration entered in the sheet.

#### Scenario: Start with valid whole minutes
- **WHEN** the custom input contains `25` and the user taps "Start nap"
- **THEN** a 25-minute countdown starts and the app shows the running‑timer screen

#### Scenario: Start with valid minutes and seconds
- **WHEN** the custom input contains `12:30` and the user taps "Start nap"
- **THEN** a 12-minute 30-second countdown starts and the app shows the running‑timer screen

#### Scenario: Custom duration is passed as integer seconds
- **WHEN** the custom duration sheet produces a valid duration
- **THEN** the duration is passed to the timer controller as an integer number of seconds without an intermediate float-minutes conversion

#### Scenario: Reject an invalid duration
- **WHEN** the custom input is empty, invalid, shorter than 5 seconds, or longer than 120 minutes
- **THEN** the "Start nap" button is disabled and no countdown starts
