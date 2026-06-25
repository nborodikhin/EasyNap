## ADDED Requirements

### Requirement: Disabled notification prompt
The setup screen SHALL show a bottom-end text prompt asking the user to enable notifications when alarm notifications are effectively unavailable. Alarm notifications SHALL be considered unavailable when app notifications are disabled or the alarm notification channel has `IMPORTANCE_NONE`.

#### Scenario: Prompt appears when app notifications are disabled
- **WHEN** the app is idle on the setup screen and app notifications are disabled
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications

#### Scenario: Prompt appears when alarm channel is blocked
- **WHEN** the app is idle on the setup screen and the alarm notification channel has `IMPORTANCE_NONE`
- **THEN** the setup screen shows a bottom-end prompt asking the user to enable notifications

#### Scenario: Prompt is hidden when alarm notifications are available
- **WHEN** the app is idle on the setup screen, app notifications are enabled, and the alarm notification channel importance is not `IMPORTANCE_NONE`
- **THEN** the setup screen does not show the enable-notifications prompt

### Requirement: Notification settings entry point
The disabled notification prompt SHALL open Android notification settings for EasyNap when clicked so the user can enable app or alarm-channel notifications.

#### Scenario: User opens settings from prompt
- **WHEN** the setup screen shows the disabled notification prompt and the user clicks it
- **THEN** Android notification settings for EasyNap are opened

### Requirement: Setup notification state is refreshed
The setup screen SHALL refresh effective alarm notification availability when it becomes visible or resumes after returning from system settings.

#### Scenario: Prompt clears after enabling notifications
- **WHEN** the user opens notification settings from the prompt, enables notifications, and returns to the app
- **THEN** the setup screen refreshes notification state and hides the prompt

#### Scenario: Prompt appears after disabling notifications
- **WHEN** notifications are disabled while the app is backgrounded and the user returns to the idle setup screen
- **THEN** the setup screen refreshes notification state and shows the prompt
