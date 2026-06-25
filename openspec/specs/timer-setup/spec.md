# Timer Setup

## Purpose

Defines the idle setup screen where the user enters a duration or picks a quick-start preset to begin a nap timer.

## Requirements

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

### Requirement: Duration selection grid
The main screen SHALL display a single unified grid of duration tiles. Recently used durations and default seed durations SHALL appear together in the same grid without separate section labels. A Custom tile SHALL appear as the last tile in the grid. Tapping any duration tile starts a nap immediately. Duration history SHALL be loaded and updated through DataStore-backed asynchronous state rather than synchronous SharedPreferences reads.

#### Scenario: Unified grid shows all durations
- **WHEN** the app is opened and no timer is active
- **THEN** a single grid of duration tiles is displayed, combining recently used durations and default durations, with no "RECENT" or "PRESETS" section labels

#### Scenario: Custom tile is always present
- **WHEN** the app is opened and no timer is active
- **THEN** a Custom tile appears as the last tile in the grid

#### Scenario: Seeded quick durations are shown
- **WHEN** the main screen is shown and the user has fewer than 6 saved nap durations in history
- **THEN** the grid includes 5, 10, and 30 minute durations as seeds to fill remaining slots (seeds that are already in history are not duplicated)

#### Scenario: Tapping a quick duration starts a timer
- **WHEN** the user taps a duration tile in the grid
- **THEN** a countdown of that duration starts immediately and the app shows the running‑timer screen

#### Scenario: Grid is capped
- **WHEN** more than 6 last nap durations are available
- **THEN** the main screen displays no more than 6 distinct durations in the grid (plus the Custom tile)

#### Scenario: Duplicate duration moves to front
- **WHEN** the user starts a nap duration that is already present in the grid
- **THEN** that duration is moved to the newest position rather than duplicated

#### Scenario: History does not block on disk
- **WHEN** the setup screen loads duration history
- **THEN** it collects DataStore-backed asynchronous state instead of calling a synchronous disk-backed preference read

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

### Requirement: Main screen visual polish
The main screen SHALL use top-aligned content with 20dp horizontal padding and the following vertical rhythm: 18dp top inset, 20sp medium-weight app title, 28dp gap, 28sp regular-weight headline, 8dp gap, 14sp regular-weight subtitle, 42dp gap, duration grid, 28dp bottom inset.

#### Scenario: Main screen uses handoff layout polish
- **WHEN** the app is opened and no timer is active
- **THEN** the main screen content is top-aligned and uses handoff-consistent spacing and font sizes

### Requirement: Main screen subtitle
The main screen SHALL display the subtitle "Tap a length to start your nap" (no trailing period) in `bodyMedium` style below the headline.

#### Scenario: Subtitle is shown
- **WHEN** the main screen is displayed
- **THEN** the text "Tap a length to start your nap" appears below the headline

### Requirement: User-visible setup strings are localized resources
User-visible strings on the main setup screen that are introduced or modified by this change SHALL be defined in Android string resources instead of hardcoded Compose literals.

#### Scenario: Main screen strings come from resources
- **WHEN** a user-visible main-screen label or action is introduced or modified by this change
- **THEN** the text is backed by an Android string resource

### Requirement: Setup screen is shown only when idle
The main/setup screen SHALL be presented only when no timer is currently active.

#### Scenario: Setup hidden while a timer runs
- **WHEN** a timer is active and the user is in the app
- **THEN** the setup screen is not shown; the running‑timer screen is shown instead

### Requirement: Disabled notification prompt
The setup screen SHALL show a bottom-end text prompt asking the user to enable notifications when alarm notifications are effectively unavailable. Alarm notifications SHALL be considered unavailable when app notifications are disabled or the alarm notification channel has `IMPORTANCE_NONE`.

#### Scenario: Prompt appears when app notifications are disabled
- **WHEN** the app is idle on the setup screen and app notifications are disabled
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications

#### Scenario: Prompt appears when alarm channel is blocked
- **WHEN** the app is idle on the setup screen and the alarm notification channel has `IMPORTANCE_NONE`
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications

#### Scenario: Prompt is hidden when alarm notifications are available
- **WHEN** the app is idle on the setup screen, app notifications are enabled, and the alarm notification channel importance is not `IMPORTANCE_NONE`
- **THEN** the setup screen does not show the enable-notifications prompt

### Requirement: Notification settings entry point
The disabled notification prompt SHALL open Android notification settings for EasyNap when clicked so the user can enable app or alarm-channel notifications.

#### Scenario: User opens settings from prompt
- **WHEN** the setup screen shows the disabled notification prompt and the user clicks it
- **THEN** Android notification settings for EasyNap are opened

### Requirement: Setup notification state is refreshed
The setup screen SHALL refresh effective alarm notification availability when it becomes visible or resumes after returning from system settings.

#### Scenario: Prompt clears after enabling notifications
- **WHEN** the user opens notification settings from the prompt, enables notifications, and returns to the app
- **THEN** the setup screen refreshes notification state and hides the prompt

#### Scenario: Prompt appears after disabling notifications
- **WHEN** notifications are disabled while the app is backgrounded and the user returns to the idle setup screen
- **THEN** the setup screen refreshes notification state and shows the prompt
