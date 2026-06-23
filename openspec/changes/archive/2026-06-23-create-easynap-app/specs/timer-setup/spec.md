## ADDED Requirements

### Requirement: Duration entry field
The main screen SHALL display a labeled "Timer duration (minutes)" text field that accepts a positive floating‑point number of minutes, using a decimal numeric keyboard.

#### Scenario: Field is shown on the main screen
- **WHEN** the app is opened and no timer is active
- **THEN** a text field labeled "Timer duration (minutes)" is displayed at the top of a vertical layout

#### Scenario: Field accepts a decimal value
- **WHEN** the user types a fractional value such as `12.5`
- **THEN** the field accepts the input and treats it as 12.5 minutes

### Requirement: Start a timer from the entered duration
The main screen SHALL provide a "Start Timer" button that starts a countdown for the duration entered in the field.

#### Scenario: Start with a valid duration
- **WHEN** the field contains a positive number and the user taps "Start Timer"
- **THEN** a countdown of that many minutes starts and the app shows the running‑timer screen

#### Scenario: Reject an invalid duration
- **WHEN** the field is empty, zero, negative, or not a valid number and the user taps "Start Timer"
- **THEN** no countdown starts and the user is shown that the duration is invalid

### Requirement: Quick‑start buttons
The main screen SHALL display, below the "Or quick start:" label, a vertically scrollable list of quick‑start buttons in 5‑minute increments from 5 minutes up to and including 60 minutes.

#### Scenario: Quick‑start buttons are listed
- **WHEN** the main screen is shown
- **THEN** buttons are listed for 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, and 60 minutes, in ascending order

#### Scenario: Tapping a quick‑start button starts a timer
- **WHEN** the user taps a quick‑start button labeled "N minutes"
- **THEN** a countdown of N minutes starts immediately, regardless of the text field contents, and the app shows the running‑timer screen

### Requirement: Setup screen is shown only when idle
The main/setup screen SHALL be presented only when no timer is currently active.

#### Scenario: Setup hidden while a timer runs
- **WHEN** a timer is active and the user is in the app
- **THEN** the setup screen is not shown; the running‑timer screen is shown instead
