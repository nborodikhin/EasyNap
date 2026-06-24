## MODIFIED Requirements

### Requirement: Running‑timer screen
While a timer is active and the app is in the foreground, the system SHALL show a running‑timer screen displaying the chosen-duration caption, a circular progress ring, the live remaining time, a "REMAINING" label, and a "Cancel nap" button. The visible remaining-time text SHALL update on a one-second sync cadence and SHALL display the actual remaining time rounded up to the nearest second. The circular progress ring SHALL animate smoothly toward the anticipated progress position for the next sync moment, calculated using `minOf(syncInterval, remainingMs)` so final partial intervals do not overshoot.

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
