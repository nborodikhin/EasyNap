## ADDED Requirements

### Requirement: Alarm screen reachable when alarm service is active
Whenever `AlarmService` is running, the alarm screen SHALL be reachable by the user. If `AlarmActivity` is not visible when the user brings the app to the foreground, `MainActivity` SHALL launch `AlarmActivity` automatically.

#### Scenario: AlarmActivity never appeared due to BAL restriction
- **WHEN** `AlarmService` is running and `AlarmActivity` is not visible and the user opens `MainActivity`
- **THEN** `MainActivity` launches `AlarmActivity` so the user can stop or snooze the alarm

#### Scenario: AlarmActivity was killed and user reopens the app
- **WHEN** `AlarmService` is running and the user previously dismissed `AlarmActivity` from recents and then opens `MainActivity`
- **THEN** `MainActivity` launches `AlarmActivity` so the user can stop or snooze the alarm

### Requirement: Activity-side alarm launch when app is foreground at expiry
When `MainActivity` is in the foreground while a nap timer is running, it SHALL launch `AlarmActivity` directly when the timer expires, as a fallback in case the service's own background activity launch is blocked.

#### Scenario: Timer expires while MainActivity is foreground
- **WHEN** a nap timer is running and `MainActivity` is in the foreground and the timer reaches its end time
- **THEN** `MainActivity` launches `AlarmActivity` directly without waiting for `AlarmService` to do so

#### Scenario: Duplicate launch from both service and activity is safe
- **WHEN** both `AlarmService` and `MainActivity` launch `AlarmActivity` at approximately the same time
- **THEN** only one `AlarmActivity` instance is shown and the alarm audio and haptic sequence runs normally
