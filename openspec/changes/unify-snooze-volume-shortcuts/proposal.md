## Why

The alarm screen already presents snooze durations as a unified ordered list, but hardware volume-key shortcuts are hard-coded separately. This creates drift risk and makes it harder for users to discover which physical button maps to which snooze duration.

## What Changes

- Use the first two configured snooze options as the source of truth for volume-key shortcuts while the alarm screen is active.
- Map volume down to the first snooze option and volume up to the second snooze option.
- Show localized second-line shortcut labels on the corresponding alarm snooze buttons: `Vol-` for the first option and `Vol+` for the second option.
- Leave the third snooze button as a one-line button with no reserved blank shortcut space.
- Keep the notification Snooze action behavior unchanged unless a later decision explicitly changes it.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `nap-alarm`: Volume-key snooze behavior changes from both keys triggering a fixed 1-minute snooze to volume down using the first snooze option and volume up using the second snooze option.
- `accessibility`: Alarm snooze buttons with visible shortcut labels need descriptive localized/accessibility text that includes the shortcut where appropriate.

## Impact

- Affected code: `AlarmActivity` snooze key handling and alarm snooze button rendering.
- Affected resources: localized strings for volume shortcut labels and any shortcut-aware accessibility descriptions.
- Affected tests: alarm UI/content tests and key-handling tests for volume down/up snooze durations.
- No new dependencies or external APIs.
