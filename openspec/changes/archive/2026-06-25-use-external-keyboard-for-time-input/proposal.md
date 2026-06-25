## Why

The custom duration sheet is keypad-only on touch devices, but external keyboard users currently have no direct way to enter a precise duration without tapping the on-screen keypad. Supporting hardware keys makes the sheet usable on tablets, foldables, Chromebooks, and phones with connected keyboards.

## What Changes

- Allow the custom duration bottom sheet to accept external keyboard digit input.
- Treat hardware dot (`.`) and colon (`:`) keys as the same colon entry action for `mm:ss` input.
- Treat hardware Enter as the same action as tapping "Start nap" when the current custom duration is valid.
- Preserve the existing on-screen keypad, validation, range limits, and timer start behavior.

## Capabilities

### New Capabilities

### Modified Capabilities
- `timer-setup`: Add external keyboard input support to the custom duration bottom sheet.

## Impact

- Affected app code: custom duration sheet Compose UI and keyboard event handling.
- Affected tests: keypad/buffer unit coverage and UI or instrumentation coverage for hardware key entry.
- Affected specs: `timer-setup`.
- This change does not alter quick duration tiles, countdown timing semantics, alarm behavior, or the visible on-screen keypad layout.
