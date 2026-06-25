## Why

EasyNap relies on notifications to make an active timer and ringing alarm attributable to the app. If app or alarm-channel notifications are disabled, a user may hear or feel an alarm but lose the obvious app entry point, especially after dismissing or killing the alarm activity.

## What Changes

- Detect when notifications are effectively unavailable on supported Android versions.
- Treat notifications as unavailable when app notifications are disabled or the alarm notification channel is blocked.
- Show a small bottom-end setup-screen text prompt asking the user to enable notifications when they are unavailable.
- Open system notification settings from the prompt so the user can re-enable notifications for EasyNap.
- Ensure notification channels exist before checking their enabled/blocked state.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `timer-setup`: Show a setup-screen warning and settings entry point when notifications needed for alarms are unavailable.
- `nap-alarm`: Define alarm notification availability as a precondition for reliable alarm attribution and recovery.

## Impact

- Affected UI: setup screen layout and interaction behavior.
- Affected Android integration: notification permission/status checks, notification channel creation timing, and notification settings intents.
- Affected tests: setup-screen warning visibility, settings click behavior, and notification-state evaluation.
