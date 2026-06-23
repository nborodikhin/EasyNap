## MODIFIED Requirements

### Requirement: Running‑timer screen
While a timer is active and the app is in the foreground, the system SHALL show a running‑timer screen displaying the chosen-duration caption, a circular progress ring, the live remaining time, a "REMAINING" label, and a "Cancel nap" button.

#### Scenario: Open the app while a timer is active
- **WHEN** the user opens or reopens the app while a timer is active
- **THEN** the app lands directly on the running‑timer screen showing the chosen-duration caption, live remaining time, circular progress ring, "REMAINING" label, and "Cancel nap" button

#### Scenario: Remaining time updates live
- **WHEN** the running‑timer screen is visible
- **THEN** the displayed remaining time decreases in real time toward zero and the circular progress ring animates smoothly and continuously to reflect countdown progress (no per-second jumps)
