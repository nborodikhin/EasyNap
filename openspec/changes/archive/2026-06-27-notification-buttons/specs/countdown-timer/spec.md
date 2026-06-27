## MODIFIED Requirements

### Requirement: Foreground service with live countdown notification
While a timer is active, the system SHALL run a foreground service that maintains an ongoing (non‑dismissable) notification. The notification SHALL display a title identifying the active nap as `<duration> nap is active` (e.g. "20 min nap is active", or "1:30 nap is active" for sub‑minute or fractional durations) and SHALL display the remaining time, updated at least once per second. The notification SHALL provide a single "Stop" action that cancels the timer without opening the app; this action SHALL have the same effect as the running‑screen Cancel — the countdown stops, the foreground service and its notification are removed, the scheduled completion is cancelled, and the app returns to its idle setup state. The "Stop" action SHALL NOT duplicate the notification's tap (content) action, which opens the running‑timer screen.

#### Scenario: Notification shows nap title and remaining time
- **WHEN** a timer is active
- **THEN** a foreground service is running and its notification shows the title `<duration> nap is active` and the remaining time (e.g. mm:ss), refreshed at least every second

#### Scenario: Timer survives backgrounding and app close
- **WHEN** the app is backgrounded or its task is closed while a timer is active
- **THEN** the foreground service and its countdown notification continue running, and the timer keeps counting down

#### Scenario: Tapping the notification opens the app
- **WHEN** the user taps the countdown notification (not an action button)
- **THEN** the app opens and shows the running‑timer screen

#### Scenario: Notification Stop cancels the timer and its alarm
- **WHEN** the user taps the "Stop" action on the countdown notification
- **THEN** the countdown stops, the foreground service and its notification are removed, the scheduled alarm completion is cancelled so no alarm fires, and no timer remains active

#### Scenario: Notification Stop does not open the app
- **WHEN** the user taps the "Stop" action on the countdown notification
- **THEN** the timer is cancelled without launching the app to the foreground
