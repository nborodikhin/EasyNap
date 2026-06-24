## Why

The running countdown currently risks coupling display text and progress movement too closely to millisecond-level state. This polish pass also tightens main-screen visual fidelity against the handoff so the first screen and countdown feel intentional and production-ready.

## What Changes

- Update the running countdown UI behavior to operate on a one-second sync cadence for visible text.
- Display remaining time rounded up to the nearest second on each sync tick, so the last visible value before completion is likely `00:01`.
- Cap the countdown screen's visible remaining time at the user-requested duration, so startup latency before the countdown begins is perceived as delay rather than an incorrect longer timer value.
- Animate the progress ring toward the anticipated position at the next sync moment.
- Use `minOf(syncInterval, remainingMs)` when calculating the anticipated progress target so the final partial interval remains accurate.
- Update main-screen alignment, spacing, and font sizes to match `design_handoff_easy_nap`; revise subtitle text to "Tap a length to start your nap" (no trailing period).
- Ensure user-visible strings introduced or touched by this change use Android string resources rather than untranslated hardcoded literals.
- When the alarm is active, intercept volume-up and volume-down key presses and treat them as a 1-minute snooze (same effect as tapping "+1 min").
- Preserve timer scheduling, countdown padding, foreground notification behavior, and alarm completion behavior.

## Capabilities

### New Capabilities

### Modified Capabilities
- `countdown-timer`: Refine running-timer screen text/progress synchronization behavior.
- `timer-setup`: Polish main-screen alignment, spacing, typography, and subtitle/string handling.
- `nap-alarm`: Add volume-key snooze trigger — volume up or down while the alarm is active snoozes for 1 minute.

## Impact

- Affected app code: running countdown UI, main setup screen UI, string resources, any helper used for rounded-up foreground display formatting, and the alarm activity for volume-key handling.
- Affected specs: `countdown-timer`, `timer-setup`, `nap-alarm`.
- No changes to timer service notification formatting, timer completion timing, or persisted timer state are intended.
