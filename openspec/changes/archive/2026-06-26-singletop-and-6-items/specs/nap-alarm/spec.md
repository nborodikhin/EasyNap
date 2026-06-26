## ADDED Requirements

### Requirement: Alarm opens in the existing app window in multi-window environments
In multi-window environments (ChromeOS, tablet split-screen), the alarm activity SHALL open inside the existing app task and window rather than launching as a separate OS-level window. The activity SHALL use `launchMode="singleTop"` so it can share the app task while still preventing duplicate instances.

#### Scenario: Alarm fires on ChromeOS while app is open
- **WHEN** the app is open in a window on ChromeOS and the timer completes
- **THEN** the alarm screen opens inside the same window, not in a new separate OS window

#### Scenario: No duplicate alarm instance when already at top
- **WHEN** the alarm activity is already at the top of the task stack and the system attempts to launch it again
- **THEN** no new instance is created and the existing alarm screen remains visible
