## Why

The handoff defines a calmer, more polished first-pass Easy Nap experience while preserving the app's current core timer mechanics. This change captures the agreed "First" items so implementation can proceed without pulling in undecided custom-entry, exact-duration, snooze, README, or alarm-sequence changes.

## What Changes

- Replace the long 5-60 minute quick-start list with a compact table of up to 6 distinct recent nap durations, pre-seeded with 5, 10, and 30 minutes.
- Keep the current timer duration input box for now, but move it below the duration table.
- Refresh the countdown screen with the handoff layout: chosen-duration caption, circular progress ring, remaining label, and "Cancel nap" action.
- Keep the foreground service and ongoing countdown notification behavior unchanged.
- Refresh the alarm screen copy and controls: "Time to wake up", duration-specific body copy, completed-ring visual, and "Stop" action.
- Apply the Calm Teal Material 3 dark visual system, typography, spacing, shapes, edge-to-edge system bars, and adaptive launcher icon from the handoff.
- Explicitly exclude the ignored handoff differences: exact countdown duration semantics, alarm auto-dismiss changes, alarm sound/vibration sequence changes, and README updates.

## Capabilities

### New Capabilities
- `app-visual-design`: Defines the app-wide visual design system, screen layout fidelity, edge-to-edge treatment, and launcher icon.

### Modified Capabilities
- `timer-setup`: Replace the current quick-start list with a compact distinct recent-duration table and move the existing duration input below it.
- `countdown-timer`: Update the running-timer screen presentation while preserving countdown service and notification behavior.
- `nap-alarm`: Update alarm presentation and rename the user stop action from Cancel to Stop while preserving the existing alarm sequence and auto-dismiss behavior.

## Impact

- Affected app code: Compose setup, running timer, alarm UI, theme files, and launcher icon resources.
- Affected specs: `timer-setup`, `countdown-timer`, `nap-alarm`, plus new `app-visual-design`.
- No change to timer duration padding, foreground notification semantics, alarm auto-dismiss timing, alarm fade/vibration sequence, or README content in this change.
