## MODIFIED Requirements

### Requirement: Gradual audible and haptic alert
The alarm SHALL follow a fixed sequence designed to wake the user gently:

1. **0 s** — vibration begins (500 ms on / 500 ms off pattern) and continues for the entire alarm sequence.
2. **5 s** — the system attempts to play the default system alarm URI (`TYPE_ALARM`). If that URI cannot be opened, the bundled fallback sound (`R.raw.helium`) is used instead. If neither can be opened, audio is silently skipped and only vibration continues. When audio is available it starts at volume 0 and rises linearly to full volume over the next 10 seconds.
3. **15 s** — the sound plays at full volume.
4. **45 s** — the sound fades back to volume 0 linearly over 10 seconds.
5. **55 s** — the alarm auto‑stops: sound and vibration end and the alarm activity finishes.

#### Scenario: Vibration runs throughout
- **WHEN** the alarm is active
- **THEN** the device vibrates continuously from the moment the alarm starts until it ends (whether by timeout or Cancel)

#### Scenario: Sound fades in after initial vibration
- **WHEN** 5 seconds have elapsed since the alarm started and a playable system alarm URI is available
- **THEN** the alarm sound begins at zero volume and rises linearly to full volume over the following 10 seconds

#### Scenario: Bundled fallback plays when system alarm URI fails
- **WHEN** 5 seconds have elapsed since the alarm started and the system default alarm URI cannot be opened
- **THEN** the bundled `R.raw.helium` asset plays instead, starting at zero volume and rising linearly to full volume over the following 10 seconds

#### Scenario: Audio unavailable does not crash
- **WHEN** 5 seconds have elapsed since the alarm started and neither the system alarm URI nor the bundled fallback can be opened
- **THEN** the alarm continues with vibration only and the app does not crash

#### Scenario: Sound fades out before auto‑stop
- **WHEN** 45 seconds have elapsed since the alarm started
- **THEN** the alarm sound begins to decrease linearly from full volume to zero over the following 10 seconds
