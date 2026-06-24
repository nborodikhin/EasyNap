## Why

The handoff's custom duration flow removes the friction of a visible text field while still supporting precise nap lengths. This change captures the remaining duration-entry decisions separately from the first-pass visual refresh so it can be reviewed and implemented independently.

## What Changes

- Remove the inline "Timer duration (minutes)" text field from the home screen.
- Add a Custom duration entry point that opens a `ModalBottomSheet` over the home screen.
- Replace decimal typed entry with keypad-only input supporting whole minutes and `mm:ss`.
- Reject or ignore decimal input by not providing a decimal key.
- Validate custom durations against the range `0:05` through `120 min`.
- Disable the custom Start action and show error-colored helper text when input is out of range or invalid.
- Start the countdown from valid custom input and dismiss the sheet back into the countdown flow.

## Capabilities

### New Capabilities

### Modified Capabilities
- `timer-setup`: Replace inline decimal duration entry with a Custom bottom sheet and keypad-only whole-minute or `mm:ss` parsing.

## Impact

- Affected app code: setup/home Compose UI, custom duration parsing, validation state, start action wiring, and duration history integration.
- Affected specs: `timer-setup`.
- This change does not alter countdown padding semantics, alarm behavior, snooze, visual theme, or README content.
