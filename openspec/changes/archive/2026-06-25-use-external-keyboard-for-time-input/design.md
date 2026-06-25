## Context

The custom duration sheet currently renders a value display, an on-screen numeric keypad, and a "Start nap" button. Input is stored as a string buffer and normalized through `appendToBuffer`, while `parseCustomDurationSeconds` and `isCustomDurationInRange` decide whether the Start action is enabled.

The existing `timer-setup` spec says decimal input is rejected or ignored because the touch keypad does not offer decimals. This change keeps that product rule for duration formats, but adds a hardware-key convenience: a physical dot key maps to the colon action inside the custom duration sheet.

## Goals / Non-Goals

**Goals:**
- Let external keyboard users enter the same values available through the on-screen keypad.
- Reuse the existing buffer, parsing, validation, and start flow semantics.
- Support both main keyboard and numpad digit keys where Compose exposes them distinctly.
- Make Enter start only when the current value is valid, matching the enabled "Start nap" button.

**Non-Goals:**
- Add a visible text field or IME-focused text entry model.
- Support decimal durations.
- Add global app shortcuts outside the custom duration sheet.
- Change the on-screen keypad layout.

## Decisions

### Route hardware keys through the existing buffer rules

Digit, colon, dot, and backspace/delete key events should be translated to the same logical keys used by the on-screen keypad, then passed through the existing buffer update path.

Alternative considered: Maintain a separate text-input parser for external keyboards. That would duplicate limits around three whole-minute digits, two seconds digits, colon insertion, and unsupported characters.

### Treat dot as a colon alias only in the custom sheet

When the custom duration sheet receives hardware `.` input, it should request colon insertion rather than decimal insertion. This preserves the no-decimal duration format while making `12.30` keyboard muscle memory resolve to `12:30`.

Alternative considered: Ignore dot to match the existing decimal rejection scenario literally. That is stricter, but it misses the intended convenience for external keyboard users.

### Consume Enter only for valid starts

Enter should start the nap when the custom duration is valid. When the value is empty, incomplete, or out of range, Enter should not start a timer and should leave the sheet open with the same validation feedback as the button path.

Alternative considered: Always consume Enter even when invalid. That can hide keyboard events without a useful outcome; matching the Start button state is easier to reason about and test.

## Risks / Trade-offs

- Key event focus can be brittle in Compose bottom sheets if no focusable element requests focus. Mitigation: make the sheet content focusable and request focus when shown.
- Some keyboards expose numpad keys differently from top-row keys. Mitigation: map both standard digit keys and numpad digit keys where available.
- Dot-as-colon slightly qualifies the existing decimal-rejection language. Mitigation: update `timer-setup` with a specific external-keyboard scenario so the behavior is explicit.
