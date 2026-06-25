## MODIFIED Requirements

### Requirement: Disabled notification prompt
The setup screen SHALL show a bottom-end text prompt asking the user to enable notifications when alarm notifications are effectively unavailable. Alarm notifications SHALL be considered unavailable when app notifications are disabled or the alarm notification channel has `IMPORTANCE_NONE`. The prompt SHALL reflect the current notification availability immediately on initial setup-screen display. After the setup screen is visible, subsequent prompt visibility changes SHALL fade in when alarm notifications become unavailable and fade out when alarm notifications become available.

#### Scenario: Prompt appears when app notifications are disabled
- **WHEN** the app is idle on the setup screen and app notifications are disabled before the setup screen is displayed
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications without waiting for an entrance animation

#### Scenario: Prompt appears when alarm channel is blocked
- **WHEN** the app is idle on the setup screen and the alarm notification channel has `IMPORTANCE_NONE` before the setup screen is displayed
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications without waiting for an entrance animation

#### Scenario: Prompt is hidden when alarm notifications are available
- **WHEN** the app is idle on the setup screen, app notifications are enabled, and the alarm notification channel importance is not `IMPORTANCE_NONE`
- **THEN** the setup screen does not show the enable-notifications prompt

#### Scenario: Prompt fades in after visible notification denial
- **WHEN** the setup screen is visible without the prompt and alarm notifications become unavailable while the setup screen remains visible
- **THEN** the enable-notifications prompt fades in at the bottom end of the setup screen

#### Scenario: Prompt fades out after visible notification enablement
- **WHEN** the setup screen is visible with the prompt and alarm notifications become available while the setup screen remains visible
- **THEN** the enable-notifications prompt fades out from the bottom end of the setup screen
