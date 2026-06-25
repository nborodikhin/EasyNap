## Why

On Android 13 and newer, the notification permission dialog can change alarm notification availability while the setup screen is already visible. The enable-notifications prompt should avoid abrupt layout changes in that case while still reflecting the correct state immediately on first render.

## What Changes

- Keep the disabled-notification prompt state correct on initial setup-screen display without an entrance animation delay.
- Fade the prompt in when alarm notifications become unavailable while the setup screen remains visible.
- Fade the prompt out when alarm notifications become available while the setup screen remains visible.
- Preserve the existing bottom-end placement and settings click behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `timer-setup`: The disabled notification prompt gains transition behavior for in-session notification availability changes.

## Impact

- Affects the setup screen Compose UI around the disabled notification prompt.
- May add or update Compose UI tests for initial visibility and state-change fade behavior.
- No new permissions, platform APIs, services, or persistence changes.
