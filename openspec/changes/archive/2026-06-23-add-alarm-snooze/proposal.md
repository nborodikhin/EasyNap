## Why

The handoff adds a lightweight snooze path so a user can extend a nap without fully returning to setup. This change isolates snooze behavior from the alarm visual refresh and preserves the existing alarm sequence decisions that were marked ignored.

## What Changes

- Add snooze controls to the alarm screen for `+1 min`, `+5 min`, and `+10 min`.
- When a snooze option is tapped, stop the active alarm sound/vibration and re-arm the countdown for the selected added duration.
- Return the app to the running countdown screen after snooze.
- Treat a back navigation event from the alarm activity the same as the Stop action.
- Preserve the existing alarm auto-dismiss timing and gradual sound/vibration sequence.

## Capabilities

### New Capabilities

### Modified Capabilities
- `nap-alarm`: Add snooze actions that stop the current alarm and restart countdown for the selected duration; treat alarm back navigation as Stop.
- `countdown-timer`: Allow a snooze action to start a new countdown after alarm completion.

## Impact

- Affected app code: alarm UI, alarm service stop handling, timer controller start path, and navigation from alarm to running countdown.
- Affected specs: `nap-alarm`, `countdown-timer`.
- This change does not alter the 55-second auto-dismiss behavior, alarm sound/vibration sequence, countdown padding rules, custom duration entry, or README content.
