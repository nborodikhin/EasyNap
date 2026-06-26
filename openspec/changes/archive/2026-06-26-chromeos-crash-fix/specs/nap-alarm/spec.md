## MODIFIED Requirements

### Requirement: Gradual audible and haptic alert
The alarm SHALL follow a fixed sequence designed to wake the user gently:

1. **0 s** — vibration begins (500 ms on / 500 ms off pattern) and continues for the entire alarm sequence.
2. **5 s** — the system attempts to play an alarm sound, trying the default alarm URI, then the default notification URI, then the default ringtone URI in order. If none can be opened, audio is silently skipped and only vibration continues. When audio is available it starts at volume 0 and rises linearly to full volume over the next 10 seconds.
3. **15 s** — the sound plays at full volume.
4. **45 s** — the sound fades back to volume 0 linearly over 10 seconds.
5. **55 s** — the alarm auto‑stops: sound and vibration end and the alarm activity finishes.

#### Scenario: Vibration runs throughout
- **WHEN** the alarm is active
- **THEN** the device vibrates continuously from the moment the alarm starts until it ends (whether by timeout or Cancel)

#### Scenario: Sound fades in after initial vibration
- **WHEN** 5 seconds have elapsed since the alarm started and a playable system URI is available
- **THEN** the alarm sound begins at zero volume and rises linearly to full volume over the following 10 seconds

#### Scenario: Audio unavailable does not crash
- **WHEN** 5 seconds have elapsed since the alarm started and no system alarm, notification, or ringtone URI can be opened (e.g. on ChromeOS)
- **THEN** the alarm continues with vibration only and the app does not crash

#### Scenario: Sound fades out before auto‑stop
- **WHEN** 45 seconds have elapsed since the alarm started
- **THEN** the alarm sound begins to decrease linearly from full volume to zero over the following 10 seconds

### Requirement: Cancel stops the alarm immediately
The alarm activity SHALL provide a "Stop" control that immediately stops the sound and vibration and finishes the activity. On platforms where the system notification presenter fires the alarm activity's `fullScreenIntent` when the foreground notification is removed (e.g. ChromeOS ARC), any spuriously re-launched alarm activity instance SHALL be dismissed immediately rather than presenting the alarm UI again.

#### Scenario: User stops the alarm
- **WHEN** the user presses "Stop" while the alarm is active
- **THEN** the sound and vibration stop immediately and the alarm activity finishes

#### Scenario: Spurious re-launch after stop is dismissed
- **WHEN** the platform re-launches the alarm activity after the alarm has already been stopped (e.g. ChromeOS fires the fullScreenIntent on notification removal)
- **THEN** the re-launched activity immediately finishes without showing the alarm UI

#### Scenario: State is clean after the alarm ends
- **WHEN** the alarm finishes, whether by timeout or by Stop
- **THEN** no timer is active, the countdown notification and foreground service are gone, and the app returns to its idle setup state
