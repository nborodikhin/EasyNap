# App Visual Design

## Purpose

Defines the visual design system for Easy Nap, including the color palette, typography, shape and spacing rules, edge-to-edge presentation, and launcher icon.

## Requirements

### Requirement: Calm Teal Material 3 dark theme
The app SHALL use a single dark Material 3 visual theme based on the Calm Teal handoff palette, including `primary #5AD6C5`, `onPrimary #00382F`, `primaryContainer #00504A`, `onPrimaryContainer #76F3E1`, `secondaryContainer #28453F`, `onSecondaryContainer #9FE9DC`, `background #0F1513`, `surfaceContainer #1A211F`, `surfaceContainerHigh #232A28`, `onSurface #E0E4E1`, `onSurfaceVariant #8DA29B`, `outline #3A4642`, `outlineVariant #2A322F`, and `error #B3261E`.

#### Scenario: App uses Calm Teal dark colors
- **WHEN** any Easy Nap screen is displayed
- **THEN** the screen uses the Calm Teal dark Material 3 color roles for backgrounds, text, controls, borders, and accents

### Requirement: Typography and numeric stability
The app SHALL use the handoff typography direction with Roboto-compatible text styles, large light-weight timer numerals, uppercase section labels where labels are present, and tabular numerals for time and duration values so digits do not visually jitter.

#### Scenario: Timer numerals use tabular figures
- **WHEN** countdown or duration values are displayed
- **THEN** the displayed numerals use tabular figure styling

### Requirement: Shapes, spacing, and touch targets
The app SHALL apply the handoff shape and spacing direction: 18-24dp horizontal screen padding, rounded cards and buttons, fully rounded primary actions, and interactive targets of at least 48dp. Controls that are buttons or visually/functionally serve as buttons SHALL NOT exceed 320dp in width.

#### Scenario: Controls meet touch target minimums
- **WHEN** the user views tappable duration, countdown, or alarm controls
- **THEN** each tappable control is at least 48dp in its smaller dimension

#### Scenario: Button-like controls stay within maximum width
- **WHEN** a button or button-like control is displayed on any app screen
- **THEN** its width does not exceed 320dp

### Requirement: Edge-to-edge dark surfaces
The app SHALL present its screens edge-to-edge with transparent system bars and dark surfaces that remain legible behind system insets.

#### Scenario: Screen draws edge-to-edge
- **WHEN** an Easy Nap screen is displayed
- **THEN** the app content and background extend edge-to-edge with transparent system bars and legible foreground content

### Requirement: Adaptive launcher icon
The app SHALL provide an adaptive launcher icon using the handoff's alarm clock plus `zzz` direction on a dark teal radial-style background, keeping foreground artwork inside the adaptive icon safe zone.

#### Scenario: Launcher shows Easy Nap icon
- **WHEN** the app appears in the Android launcher
- **THEN** its icon shows a dark teal Easy Nap adaptive icon with an alarm clock and `zzz` motif

#### Scenario: Themed icon support on Android 13+
- **WHEN** the user enables themed app icons on Android 13 or later
- **THEN** the launcher applies the system-derived tint to the app icon using the `monochrome` adaptive icon layer
