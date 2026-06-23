## MODIFIED Requirements

### Requirement: Duration entry field
The main screen SHALL display the existing labeled "Timer duration (minutes)" text field below the quick duration table. The field SHALL continue to accept a positive floating-point number of minutes using a decimal numeric keyboard.

#### Scenario: Field is shown below quick durations
- **WHEN** the app is opened and no timer is active
- **THEN** a text field labeled "Timer duration (minutes)" is displayed below the quick duration table

#### Scenario: Field accepts a decimal value
- **WHEN** the user types a fractional value such as `12.5`
- **THEN** the field accepts the input and treats it as 12.5 minutes

### Requirement: Quick‑start buttons
The main screen SHALL display a compact quick duration table containing up to 6 distinct recent nap durations, with no "RECENT" or "PRESETS" section titles. Before the user has enough history, the table SHALL be pre-seeded with 5, 10, and 30 minute durations.

#### Scenario: Seeded quick durations are shown
- **WHEN** the main screen is shown and the user has fewer than 6 saved nap durations in history
- **THEN** the quick duration table includes 5, 10, and 30 minute durations as seeds to fill remaining slots (seeds that are already in history are not duplicated)

#### Scenario: Tapping a quick duration starts a timer
- **WHEN** the user taps a duration in the quick duration table
- **THEN** a countdown of that duration starts immediately, regardless of the text field contents, and the app shows the running‑timer screen

#### Scenario: Quick duration table is capped
- **WHEN** more than 6 last nap durations are available
- **THEN** the main screen displays no more than 6 distinct durations in the quick duration table

#### Scenario: Duplicate duration moves to front
- **WHEN** the user starts a nap duration that is already present in the quick duration table
- **THEN** that duration is moved to the newest position rather than duplicated
