## Context

The current app is functional but uses a basic Material layout: a decimal duration field, a long quick-start list, a simple running timer, and a simple alarm screen. The handoff provides a high-fidelity Material 3 dark visual direction and several behavior ideas. The agreed first pass adopts the visual direction and selected low-risk UI behavior while leaving undecided or ignored behavior out of scope.

## Goals / Non-Goals

**Goals:**
- Implement the first-pass duration selection: up to 6 distinct recent nap durations, seeded with 5, 10, and 30 minutes, with the existing duration field retained below the table.
- Make the countdown and alarm screens visually match the handoff while preserving existing timer and alarm mechanics.
- Apply the Calm Teal Material 3 dark scheme, Roboto/tabular time typography, rounded shapes, spacing, edge-to-edge treatment, and adaptive icon direction.

**Non-Goals:**
- Do not remove the existing decimal timer input.
- Do not add keypad custom entry or a bottom sheet.
- Do not remove the current 5-second countdown padding.
- Do not alter the current alarm auto-dismiss or sound/vibration fade sequence.
- Do not add snooze.
- Do not update README content in this change.

## Decisions

- Use a compact duration table instead of the handoff's separate Recent and Presets sections. This follows the product decision to support up to 6 distinct recent nap durations with initial seed values of 5, 10, and 30 minutes and no section titles.
- Keep recents lightweight and implementation-local to the setup flow. Durations are stored as minute values for this first pass because the retained input field still accepts minute-based decimal values and keypad `mm:ss` input is out of scope.
- Preserve service-owned timer state and notification behavior. The countdown UI derives progress and remaining time from the existing running timer state rather than introducing new scheduling semantics.
- Treat "Stop" as the visible alarm action label replacing "Cancel"; it stops the alarm using the existing alarm-stop behavior.
- Implement visual design through Compose theme tokens and reusable UI primitives where practical, avoiding HTML reuse from the design handoff.
- Replace launcher icon assets with a native adaptive icon representation of the alarm clock plus `zzz` direction from the handoff.

## Risks / Trade-offs

- Recents semantics are less complete than the handoff because `mm:ss` custom durations are out of scope. Mitigation: keep storage/API names general enough to support total seconds later, while still deduping minute values in this first pass.
- README will temporarily describe old quick-start behavior because #22 was marked ignored. Mitigation: keep the OpenSpec scope explicit so this mismatch is intentional for this change.
- Visual fidelity may vary across device sizes. Mitigation: use Compose density-independent sizes, minimum 48dp targets, and preview/device validation.
- Roboto is normally the Android platform default, but explicit font handling may vary by Compose configuration. Mitigation: use Compose text styles with tabular font feature settings for timer numerals.
