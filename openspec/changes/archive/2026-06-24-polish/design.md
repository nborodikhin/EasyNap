## Context

The running timer screen displays remaining time and a circular progress ring. Text should be stable and second-based, while progress should animate smoothly enough that users do not perceive visible jumps. The progress ring can safely anticipate the next sync point because the authoritative completion time remains unchanged. The main screen should also align more tightly with the design handoff's spacing, alignment, and type scale. The alarm screen should support volume-key snooze so users can snooze without needing to tap the screen.

## Goals / Non-Goals

**Goals:**
- Keep visible countdown text on a one-second cadence.
- Round visible remaining time up to the nearest second.
- Animate progress toward the anticipated next sync position.
- Keep final partial intervals accurate with `minOf(syncInterval, remainingMs)`.
- Apply main-screen alignment, spacing, and font sizes from `design_handoff_easy_nap`; revise subtitle to "Tap a length to start your nap" (no trailing period).
- Keep user-visible strings in Android string resources.
- Intercept volume-up and volume-down key events in the alarm activity and trigger a 1-minute snooze.

**Non-Goals:**
- Do not change countdown scheduling or completion time.
- Do not remove existing timer padding behavior.
- Do not change foreground notification update behavior.
- Do not change alarm trigger behavior.
- Do not reintroduce the handoff's custom bottom sheet as part of this polish change.
- Do not perform broad architectural migrations that already have dedicated changes, such as DataStore migration.

## Decisions

- Use a fixed `syncInterval` of 1 second for the foreground running screen.
- Track actual remaining time at each sync tick for text, and compute display text using ceiling division by 1000ms.
- Compute the progress target from anticipated remaining time: `remainingMs - minOf(syncInterval, remainingMs)`, clamped at zero.
- Animate progress linearly to the anticipated target over the sync interval.
- Keep notification formatting separate unless implementation naturally shares a helper without changing notification behavior.
- Align the main screen to the handoff's top-aligned structure with vertical rhythm: 18dp top, title (20sp/500), 28dp, headline (28sp/400), 8dp, subtitle (14sp/400), 42dp, grid, 28dp bottom; 20dp horizontal padding.
- Revise subtitle text to "Tap a length to start your nap" (remove trailing period) and back it with a string resource.
- Move hardcoded user-visible strings touched by this change into `strings.xml`, including main-screen labels/actions and countdown labels/actions if they are edited.
- Override `onKeyDown` in the alarm activity to intercept `KEYCODE_VOLUME_UP` and `KEYCODE_VOLUME_DOWN`; call the existing snooze-for-1-minute logic and return `true` to consume the event so system volume does not change.

## Risks / Trade-offs

- The text may show `00:01` until the alarm transition rather than showing `00:00` immediately before completion. This is intentional and matches the requested behavior.
- Progress and text may be based on different remaining-time values within a tick. This is intentional: text favors readable second steps, progress favors perceived smoothness.
- The subtitle text lost its trailing period to match the revised copy direction.
- Consuming volume-key events means the hardware volume level does not change when the user presses volume up/down during the alarm. This is the desired behavior — the keys act as snooze buttons, not volume controls.
