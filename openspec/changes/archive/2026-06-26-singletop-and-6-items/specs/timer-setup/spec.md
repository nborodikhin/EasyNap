## MODIFIED Requirements

### Requirement: Duration selection grid
The main screen SHALL display a single unified grid of duration tiles without separate section labels. A Custom tile SHALL appear as the last tile in the grid. Tapping any duration tile starts a nap immediately. Duration history SHALL be loaded through DataStore-backed asynchronous state rather than synchronous SharedPreferences reads.

On first launch, before any timer has been recorded, the grid SHALL display the default seed durations (5, 10, and 30 minutes). Once history has been written to DataStore, only the stored durations are shown — default seeds are never re-injected to fill gaps.

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
