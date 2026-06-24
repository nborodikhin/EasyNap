## ADDED Requirements

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
