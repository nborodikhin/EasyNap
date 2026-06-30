## MODIFIED Requirements

### Requirement: Start a countdown
When a timer is started, the system SHALL record a completion time at the requested number of seconds in the future and begin tracking the remaining time toward it. When the requested duration is 60 seconds or longer, the system SHALL add 5 seconds of padding to the completion time, so that a freshly started whole‑minute timer does not immediately tick down below the entered value. Durations shorter than 60 seconds SHALL NOT be padded.

#### Scenario: Countdown begins with padding for a one‑minute‑or‑longer duration
- **WHEN** a timer is started with a duration D of 60 seconds or more
- **THEN** the system records a completion timestamp D seconds plus 5 seconds ahead and begins counting down toward it

#### Scenario: Sub‑minute duration is not padded
- **WHEN** a timer is started with a duration shorter than 60 seconds
- **THEN** the system records a completion timestamp exactly that duration ahead, with no added padding

#### Scenario: Only one active timer
- **WHEN** a timer is already active
- **THEN** the system does not present controls to start a second concurrent timer; the existing timer remains the single source of truth

### Requirement: Foreground service with live countdown notification
While a timer is active, the system SHALL run a foreground service that maintains an ongoing (non‑dismissable) notification. The notification SHALL display a title identifying the active nap as `<duration> nap is active` (e.g. "20 min nap is active", or "1:30 nap is active" for sub‑minute or fractional durations) and SHALL display the remaining time, updated at least once per second. The notification SHALL provide a single "Stop" action that cancels the timer without opening the app; this action SHALL have the same effect as the running‑screen Cancel — the countdown stops, the foreground service and its notification are removed, the scheduled completion is cancelled, and the app returns to its idle setup state. The "Stop" action SHALL NOT duplicate the notification's tap (content) action, which opens the running‑timer screen.

The notification title SHALL derive minute and second display values from the integer-seconds duration stored in `PersistedTimer`.

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

### Requirement: Running‑timer screen
While a timer is active and the app is in the foreground, the system SHALL show a running‑timer screen displaying the chosen-duration caption, a circular progress ring, the live remaining time, a "REMAINING" label, and a "Cancel nap" button. The visible remaining-time text SHALL update on a one-second sync cadence and SHALL display the actual remaining time rounded up to the nearest second. The circular progress ring SHALL animate smoothly toward the anticipated progress position for the next sync moment, calculated using `minOf(syncInterval, remainingMs)` so final partial intervals do not overshoot.

The total duration used for progress calculation SHALL be derived from the integer-seconds `durationSeconds` field of `TimerState.Running`.

#### Scenario: Open the app while a timer is active
- **WHEN** the user opens or reopens the app while a timer is active
- **THEN** the app lands directly on the running‑timer screen showing the chosen-duration caption, live remaining time, circular progress ring, "REMAINING" label, and "Cancel nap" button

#### Scenario: Remaining text updates on second cadence
- **WHEN** the running‑timer screen is visible
- **THEN** the displayed remaining time updates on a one-second sync cadence using the actual remaining time rounded up to the nearest second

#### Scenario: Final visible second before completion
- **WHEN** the running‑timer screen has less than one second remaining before timer completion
- **THEN** the displayed remaining time is `00:01` until completion transitions to the alarm

#### Scenario: Progress anticipates next sync point
- **WHEN** the running‑timer screen updates on an animation sync tick
- **THEN** the circular progress ring animates toward the anticipated position it should have at the next sync moment

#### Scenario: Progress handles final partial interval
- **WHEN** less than one sync interval remains before timer completion
- **THEN** the progress target is calculated using `minOf(syncInterval, remainingMs)` so the target reaches zero without overshooting negative progress
