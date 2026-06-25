## ADDED Requirements

### Requirement: Alarm notification availability check
Before the user relies on a nap alarm, the system SHALL be able to determine whether alarm notifications are effectively available. Alarm notifications SHALL be considered available only when app notifications are enabled and the alarm notification channel importance is not `IMPORTANCE_NONE`.

#### Scenario: App notifications disabled makes alarm notifications unavailable
- **WHEN** app notifications are disabled for EasyNap
- **THEN** alarm notifications are treated as unavailable

#### Scenario: Alarm channel blocked makes alarm notifications unavailable
- **WHEN** app notifications are enabled but the alarm notification channel has `IMPORTANCE_NONE`
- **THEN** alarm notifications are treated as unavailable

#### Scenario: App and alarm channel enabled makes alarm notifications available
- **WHEN** app notifications are enabled and the alarm notification channel importance is not `IMPORTANCE_NONE`
- **THEN** alarm notifications are treated as available

### Requirement: Alarm notification channels exist before availability checks
The system SHALL create the timer and alarm notification channels before checking alarm notification availability on the setup screen.

#### Scenario: Fresh install can evaluate alarm channel state
- **WHEN** the app opens to the setup screen after a fresh install
- **THEN** the alarm notification channel exists before alarm notification availability is checked
