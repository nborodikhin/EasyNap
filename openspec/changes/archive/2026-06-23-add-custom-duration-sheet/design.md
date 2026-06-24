## Context

After `update-design`, the home screen has a compact recent-duration table and still retains the legacy decimal input below it. The handoff's final direction is simpler: keep the home screen focused on one-tap starts and move precise entry into a transient bottom sheet.

## Goals / Non-Goals

**Goals:**
- Remove the inline duration text field from the home screen.
- Add a Custom entry point that opens a Material 3 modal bottom sheet.
- Support whole-minute and `mm:ss` input through a custom keypad with digits, colon, and backspace.
- Enforce `0:05` through `120 min` validation before enabling Start.
- Feed valid custom durations into the same timer start path and recent-duration history as quick durations.

**Non-Goals:**
- Do not change timer scheduling, countdown padding, foreground service behavior, or alarm behavior.
- Do not add snooze.
- Do not update README content in this change.

## Decisions

- Keep custom entry as transient UI state. The input buffer, parsed seconds, and validation message belong to the sheet state and reset when dismissed.
- Store parsed custom duration as total seconds internally. This supports `mm:ss` without losing precision, while still allowing existing minute-only durations to be represented exactly.
- Clamp seconds in `mm:ss` input to 0-59 during parsing. If normalization is visible, the displayed value should reflect the normalized parse before Start.
- Disable Start for empty, zero, below-5-second, and above-120-minute values. Invalid states use the theme error color for helper text.
- Do not expose any decimal input path. The keypad omits `.`, and pasted/hardware decimal characters are ignored if an implementation path can receive them.

## Risks / Trade-offs

- Removing the visible text field is a larger behavior change than the first-pass visual refresh. Mitigation: keep the Custom tile discoverable and preserve one-tap recent durations.
- Total-second durations may require adjusting existing APIs that currently accept `Float` minutes. Mitigation: introduce conversion boundaries or migrate timer start APIs deliberately.
- Hardware keyboard input could bypass the visual keypad if not handled. Mitigation: centralize input filtering to digits, one colon, and backspace semantics.
