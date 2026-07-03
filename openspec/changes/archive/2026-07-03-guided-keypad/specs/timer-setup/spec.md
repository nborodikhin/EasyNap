## MODIFIED Requirements

### Requirement: Keypad-only custom duration input
The custom duration sheet SHALL provide a keypad containing digits and a backspace key, with no dedicated colon key. Only keys that can extend the current input toward a valid duration SHALL be enabled; all other digit keys SHALL be disabled. The colon separator SHALL be inserted automatically into the buffer when the user presses a digit while the buffer is `"0"`. Sub-minute seconds SHALL always be entered as exactly two digits — a single-digit seconds value is not a reachable state. Decimal input SHALL NOT be offered and SHALL be rejected or ignored.

#### Scenario: No colon key is present
- **WHEN** the custom duration sheet is displayed
- **THEN** the keypad contains digits and a backspace key, and no colon key

#### Scenario: Colon is auto-inserted entering sub-minute mode
- **WHEN** the custom input is `0` and the user presses digit key `2`
- **THEN** the input buffer becomes `0:2`, without the user pressing a separate colon key

#### Scenario: Only valid next keys are enabled
- **WHEN** the custom duration sheet is displayed with any input buffer
- **THEN** only the digit keys that could extend the buffer toward a valid duration are enabled, and backspace is enabled only when the buffer is non-empty; all other keys are disabled

#### Scenario: Digits that would exceed the maximum are disabled
- **WHEN** the custom input is a two-digit minute value whose value times ten would exceed 120 (e.g. `13`)
- **THEN** every digit key is disabled and only backspace or Start remain available

#### Scenario: Sub-minute seconds require two digits
- **WHEN** the custom input is entering a sub-minute value (buffer begins with `0:`)
- **THEN** the seconds field is not considered complete, and the Start button is not enabled, until exactly two digits have been entered after the colon

#### Scenario: Decimal key is unavailable
- **WHEN** the custom duration sheet is displayed
- **THEN** the keypad contains digits and backspace, and does not contain a decimal key

#### Scenario: Decimal input is ignored
- **WHEN** decimal input is attempted through any available input path
- **THEN** the decimal character is rejected or ignored and the custom input remains a whole-minute or `mm:ss` value

#### Scenario: Seconds are clamped
- **WHEN** the custom input contains a seconds field greater than 59
- **THEN** the parsed seconds value is clamped to 59

### Requirement: External keyboard custom duration input
The custom duration bottom sheet SHALL accept supported external keyboard input and apply the same buffer, parsing, validation, and start behavior used by the on-screen keypad and "Start nap" button. Hardware digit input SHALL be subject to the same per-state key-acceptance rules as the on-screen keypad, including automatic colon insertion when entering sub-minute mode. The hardware colon and period keys SHALL NOT insert a separator, since colon is never a directly-typeable character.

#### Scenario: Hardware digits enter custom duration
- **WHEN** the custom duration sheet is open and the user presses hardware digit keys `1`, `0`, and `5`
- **THEN** the custom input contains `105`

#### Scenario: Hardware digit triggers automatic colon insertion
- **WHEN** the custom duration sheet is open, the custom input is `0`, and the user presses hardware digit key `2`
- **THEN** the custom input becomes `0:2`

#### Scenario: Hardware colon and dot keys are ignored
- **WHEN** the custom duration sheet is open and the user presses hardware key `:` or `.`
- **THEN** the custom input is unchanged

#### Scenario: Hardware Enter starts valid duration
- **WHEN** the custom duration sheet is open, the custom input contains `12:30`, and the user presses hardware Enter
- **THEN** a 12-minute 30-second countdown starts and the app shows the running-timer screen

#### Scenario: Hardware Enter does not start invalid duration
- **WHEN** the custom duration sheet is open, the custom input is empty, incomplete, shorter than 5 seconds, or longer than 120 minutes, and the user presses hardware Enter
- **THEN** no countdown starts and the custom duration sheet remains open with existing validation feedback

#### Scenario: Unsupported hardware keys are ignored
- **WHEN** the custom duration sheet is open and the user presses a hardware key other than a digit, backspace, delete, or Enter
- **THEN** the custom input is unchanged
