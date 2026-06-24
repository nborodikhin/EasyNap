## 1. Parsing And State

- [x] 1.1 Add a custom duration parser that accepts whole minutes and `mm:ss` and returns total seconds.
- [x] 1.2 Reject or ignore decimal characters and unsupported input.
- [x] 1.3 Clamp the seconds field to 0-59 when parsing `mm:ss`.
- [x] 1.4 Add validation for the inclusive `0:05` to `120 min` range.

## 2. Custom Sheet UI

- [x] 2.1 Remove the inline "Timer duration (minutes)" field from the main setup screen.
- [x] 2.2 Add a Custom duration entry point to the home screen.
- [x] 2.3 Implement the Material 3 modal bottom sheet with value display, helper text, keypad, backspace, and "Start nap" button.
- [x] 2.4 Disable "Start nap" and show error-colored helper text for invalid or out-of-range input.

## 3. Start Flow Integration

- [x] 3.1 Wire valid custom durations into the existing timer start path.
- [x] 3.2 Dismiss the custom sheet when a valid custom nap starts.
- [x] 3.3 Add started custom durations to the recent-duration history using the same distinct recency rules.

## 4. Validation

- [x] 4.1 Add unit tests for whole-minute parsing, `mm:ss` parsing, seconds clamping, decimal rejection, and range validation.
- [x] 4.2 Build the debug Android app.
- [x] 4.3 Manually verify the sheet open, dismiss, invalid, and start flows.
