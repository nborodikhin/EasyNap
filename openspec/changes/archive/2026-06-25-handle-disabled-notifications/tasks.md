## 1. Notification Infrastructure

- [x] 1.1 Create a shared notification helper that creates the timer and alarm notification channels idempotently.
- [x] 1.2 Update `MainActivity` and foreground services to use the shared channel creation helper before notification status checks or notification posting.
- [x] 1.3 Add an effective alarm notification availability check using app-level notification state and alarm-channel importance.
- [x] 1.4 Add an intent helper for opening EasyNap notification settings, preferring alarm-channel settings when available.

## 2. Setup Screen UI

- [x] 2.1 Pass notification availability state and settings click handling into the idle setup screen.
- [x] 2.2 Show a small bottom-end enable-notifications text prompt only when alarm notifications are unavailable.
- [x] 2.3 Refresh notification availability when returning to the app from settings or resume.
- [x] 2.4 Add localized strings for the enable-notifications prompt.

## 3. Verification

- [x] 3.1 Add focused unit tests for notification availability across app-enabled, app-disabled, alarm-channel-enabled, and alarm-channel-blocked states.
- [x] 3.2 Add UI coverage or screenshot/Compose checks for prompt visible and hidden setup-screen states.
- [x] 3.3 Manually verify on device with app notifications allowed, app notifications denied, and alarm channel blocked.
- [x] 3.4 Run the relevant Gradle test/build tasks and confirm no regressions.
