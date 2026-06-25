## ADDED Requirements

### Requirement: External keyboard custom duration input
The custom duration bottom sheet SHALL accept supported external keyboard input and apply the same buffer, parsing, validation, and start behavior used by the on-screen keypad and "Start nap" button.

#### Scenario: Hardware digits enter custom duration
- **WHEN** the custom duration sheet is open and the user presses hardware digit keys `1`, `2`, and `5`
- **THEN** the custom input contains `125`

#### Scenario: Hardware colon enters separator
- **WHEN** the custom duration sheet is open and the user presses hardware keys `1`, `2`, `:`, `3`, and `0`
- **THEN** the custom input contains `12:30`

#### Scenario: Hardware dot enters separator
- **WHEN** the custom duration sheet is open and the user presses hardware keys `1`, `2`, `.`, `3`, and `0`
- **THEN** the custom input contains `12:30`

#### Scenario: Hardware Enter starts valid duration
- **WHEN** the custom duration sheet is open, the custom input contains `12:30`, and the user presses hardware Enter
- **THEN** a 12-minute 30-second countdown starts and the app shows the running-timer screen

#### Scenario: Hardware Enter does not start invalid duration
- **WHEN** the custom duration sheet is open, the custom input is empty, incomplete, shorter than 5 seconds, or longer than 120 minutes, and the user presses hardware Enter
- **THEN** no countdown starts and the custom duration sheet remains open with existing validation feedback

#### Scenario: Unsupported hardware keys are ignored
- **WHEN** the custom duration sheet is open and the user presses a hardware key other than a digit, dot, colon, backspace, delete, or Enter
- **THEN** the custom input is unchanged
