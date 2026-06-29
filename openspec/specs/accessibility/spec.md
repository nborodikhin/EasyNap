# Accessibility

## Purpose

Defines requirements for screen-reader support, touch-target sizing, contrast, font scaling, and motion sensitivity across all app screens.

## Requirements

### Requirement: Interactive duration tiles expose start and delete actions
Each duration tile in `SetupScreen` SHALL expose, to accessibility services, a button role and an `onClickLabel` naming the start action and duration (e.g. "Start 20-minute nap"). Because the tile's delete affordance is a long-press, the tile SHALL also expose the delete entry point to accessibility services via an `onLongClickLabel` (or equivalent custom semantics action), e.g. "Delete 20-minute nap", so screen-reader users who cannot perform a long-press gesture can still reach it.

#### Scenario: TalkBack announces a duration tile's start action
- **WHEN** a screen-reader user focuses a 20-minute duration tile
- **THEN** it is announced as a button whose activation starts a 20-minute nap

#### Scenario: Long-press delete is reachable without a long-press gesture
- **WHEN** a screen-reader user focuses a duration tile
- **THEN** a labeled "delete" action for that duration is available through accessibility services

#### Scenario: Custom tile is announced meaningfully
- **WHEN** a screen-reader user focuses the custom-duration tile
- **THEN** it is announced as "Custom duration" (button), and the decorative "+" glyph is not read separately

### Requirement: Destructive controls are labeled for accessibility
The delete badge shown on a duration tile in pending-delete mode SHALL expose a content description on its clickable node identifying the destructive action and its target (e.g. "Delete 20-minute nap"); its inner icon remains non-announced.

#### Scenario: Delete badge has a description
- **WHEN** a duration tile is in pending-delete mode and a screen-reader user focuses its delete badge
- **THEN** the badge is announced with a description naming the delete action and the affected duration

### Requirement: Custom duration input is announced as a single unit
The value display ("0:55") and human-readable helper text ("55 seconds") in the custom duration sheet SHALL be merged into a single TalkBack focus node. The announced description SHALL be the human-readable helper text when a valid duration is entered, the error text when the value is out of range, the raw buffer when input is in progress, and "Enter nap length" when the buffer is empty.

#### Scenario: Valid input is announced as a human-readable duration
- **WHEN** the user has entered "0:55" in the custom duration sheet
- **THEN** TalkBack announces a single item "55 seconds", not "0:55" and "55 seconds" separately

#### Scenario: Empty input prompts the user to enter a value
- **WHEN** the custom duration sheet opens with an empty buffer
- **THEN** TalkBack announces "Enter nap length"

#### Scenario: Out-of-range input announces the error
- **WHEN** the entered duration is outside the allowed range
- **THEN** TalkBack announces the error description rather than the raw value

### Requirement: Keypad keys are labeled for screen readers
Numeric-keypad keys that render as glyphs without a spoken equivalent SHALL provide a content description; in particular the backspace key ("⌫") SHALL be announced as "Delete".

#### Scenario: Backspace key is announced
- **WHEN** a screen-reader user focuses the backspace key on the custom-duration keypad
- **THEN** it is announced as "Delete" rather than the raw glyph

### Requirement: Countdown block is presented as a single accessible unit
The countdown ring, time display, and "REMAINING" label on `RunningScreen` SHALL be merged into a single TalkBack focus node with a content description of the form "MM:SS remaining" (e.g. "14:59 remaining"). The ring, time text, and label SHALL NOT be traversed as separate nodes.

#### Scenario: Countdown is announced as one unit
- **WHEN** a nap countdown is running and a screen-reader user navigates to the countdown area
- **THEN** TalkBack announces a single item such as "14:59 remaining" and does not read the progress ring, time, and label separately

### Requirement: Alarm snooze buttons have descriptive labels
Snooze option buttons on the alarm screen SHALL expose a content description of the form "Snooze N minute(s)" (e.g. "Snooze 5 minutes") so their purpose is clear to screen readers. The visual "+N min" label is retained unchanged. The "SNOOZE" section header is a decorative grouping element and SHALL be hidden from accessibility traversal.

#### Scenario: Snooze button is announced with action and duration
- **WHEN** a screen-reader user focuses a snooze button
- **THEN** it is announced as "Snooze N minute(s), button", not "+N min"

#### Scenario: SNOOZE header is skipped by TalkBack
- **WHEN** a screen-reader user traverses the alarm screen
- **THEN** the "SNOOZE" label is not announced as a separate item

### Requirement: Decorative glyphs are hidden from accessibility
Purely decorative on-screen glyphs rendered as text or graphics (the alarm confirmation check "✓", the custom-tile "+", the input cursor "|") SHALL be hidden from accessibility services (via `clearAndSetSemantics`, `hideFromAccessibility`, or a null `contentDescription` for `Icon`s) so they are skipped by screen readers.

#### Scenario: Decorative glyphs are skipped
- **WHEN** a screen-reader user traverses the alarm screen, the duration grid, and the custom-duration input
- **THEN** the decorative "✓", "+", and cursor "|" are not announced as content

### Requirement: Text and UI elements meet WCAG contrast minimums
All text SHALL meet a contrast ratio of at least 4.5:1 against its background (3:1 for large text). In particular, the custom-duration error text SHALL be legible on the sheet surface — the dark-theme `error` color role SHALL be light enough for foreground text, with the delete badge using a container/`onError` pairing so it also passes.

#### Scenario: Error text is legible
- **WHEN** the custom-duration sheet shows the out-of-range error message
- **THEN** the error text meets at least 4.5:1 contrast against the sheet background

#### Scenario: Delete badge remains legible after the error-color change
- **WHEN** the delete badge is shown with its icon
- **THEN** the icon meets at least 3:1 contrast against the badge fill

### Requirement: UI remains usable at 200% font scale
All on-screen text SHALL use `sp` units and scale with the user's font-size setting (including Android 14 non-linear scaling to 200%) without being clipped or truncated. Containers holding scaled text SHALL not impose fixed heights that cut off enlarged text.

#### Scenario: Countdown is readable at maximum font scale
- **WHEN** the running countdown is shown at 200% font scale
- **THEN** the remaining time and caption are fully visible, not clipped by the progress ring

#### Scenario: Buttons and keypad grow with font scale
- **WHEN** the keypad, Start, Cancel, and alarm buttons are shown at 200% font scale
- **THEN** their labels are fully visible without vertical clipping

### Requirement: Animations respect the system reduce-motion setting
When the system "Remove animations" setting is enabled (animator duration scale 0), the app SHALL skip non-essential and looping animations (e.g. the blinking input cursor, prompt fades) and render the end state directly.

#### Scenario: Cursor does not blink when motion is reduced
- **WHEN** "Remove animations" is enabled and the custom-duration sheet is open
- **THEN** the input cursor is shown statically rather than blinking

### Requirement: Interactive elements meet the minimum touch target size
Every interactive element SHALL present a touch target of at least 48dp × 48dp, per Material accessibility guidelines.

#### Scenario: Custom delete badge meets the minimum target
- **WHEN** the delete badge is shown on a duration tile
- **THEN** its touchable area is at least 48dp × 48dp

#### Scenario: Keypad and tile targets meet the minimum
- **WHEN** the numeric keypad and duration tiles are displayed
- **THEN** each key and tile presents a touch target of at least 48dp × 48dp
