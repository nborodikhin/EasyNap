## MODIFIED Requirements

### Requirement: Duration selection grid
The main screen SHALL display a single unified grid of duration tiles. Recently used durations and default seed durations SHALL appear together in the same grid without separate section labels. A Custom tile SHALL appear as the last tile in the grid. Tapping any duration tile starts a nap immediately.

#### Scenario: Unified grid shows all durations
- **WHEN** the app is opened and no timer is active
- **THEN** a single grid of duration tiles is displayed, combining recently used durations and default durations, with no "RECENT" or "PRESETS" section labels

#### Scenario: Custom tile is always present
- **WHEN** the app is opened and no timer is active
- **THEN** a Custom tile appears as the last tile in the grid

### Requirement: Duration entry field
The main screen SHALL NOT display an inline duration text field. Instead, it SHALL provide a Custom duration entry point that opens a modal bottom sheet over the home screen for precise duration entry.

#### Scenario: No inline duration field is shown
- **WHEN** the app is opened and no timer is active
- **THEN** no text field labeled "Timer duration (minutes)" is displayed on the main screen

#### Scenario: Custom opens duration sheet
- **WHEN** the user taps the Custom duration entry point
- **THEN** a modal bottom sheet opens over the home screen for precise duration entry

### Requirement: Start a timer from the entered duration
The custom duration bottom sheet SHALL provide a "Start nap" button that starts a countdown for the valid duration entered in the sheet.

#### Scenario: Start with valid whole minutes
- **WHEN** the custom input contains `25` and the user taps "Start nap"
- **THEN** a 25-minute countdown starts and the app shows the running‑timer screen

#### Scenario: Start with valid minutes and seconds
- **WHEN** the custom input contains `12:30` and the user taps "Start nap"
- **THEN** a 12-minute 30-second countdown starts and the app shows the running‑timer screen

#### Scenario: Reject an invalid duration
- **WHEN** the custom input is empty, invalid, shorter than 5 seconds, or longer than 120 minutes
- **THEN** the "Start nap" button is disabled and no countdown starts

## ADDED Requirements

### Requirement: Keypad-only custom duration input
The custom duration sheet SHALL provide a keypad containing digits, a colon key, and a backspace key. The system SHALL accept whole minutes when no colon is present and `mm:ss` when a colon is present. Decimal input SHALL NOT be offered and SHALL be rejected or ignored.

#### Scenario: Decimal key is unavailable
- **WHEN** the custom duration sheet is displayed
- **THEN** the keypad contains digits, colon, and backspace, and does not contain a decimal key

#### Scenario: Decimal input is ignored
- **WHEN** decimal input is attempted through any available input path
- **THEN** the decimal character is rejected or ignored and the custom input remains a whole-minute or `mm:ss` value

#### Scenario: Seconds are clamped
- **WHEN** the custom input contains a seconds field greater than 59
- **THEN** the parsed seconds value is clamped to 59

### Requirement: Custom duration validation feedback
The custom duration sheet SHALL show a human-readable duration description (e.g. "25 minutes" or "12 minutes 30 seconds") below the input when the current value is valid. When the input is out of range, error-colored helper text SHALL be shown instead. When the input is empty or incomplete, no helper text is shown.

#### Scenario: Valid range enables start and shows description
- **WHEN** the custom input parses to a duration from 5 seconds through 120 minutes inclusive
- **THEN** the "Start nap" button is enabled and helper text shows the duration in plain language (e.g. "25 minutes", "12 minutes 30 seconds", "30 seconds")

#### Scenario: Out-of-range input shows error
- **WHEN** the custom input parses to less than 5 seconds or more than 120 minutes
- **THEN** the "Start nap" button is disabled and helper text is shown in the error color

#### Scenario: Empty or incomplete input shows no helper text
- **WHEN** the custom input is empty or does not yet parse to a complete value
- **THEN** no helper text is displayed below the input
