# Bundled Alarm Sound

## Purpose

Defines the bundled audio asset used as a guaranteed fallback when no system alarm sound is available, ensuring the alarm is always audible (e.g. on ChromeOS).

## Requirements

### Requirement: Bundled alarm sound asset
The app SHALL include `Helium.ogg` (from AOSP material alarms, Apache 2.0) as a raw resource at `res/raw/helium.ogg`. The asset SHALL be used as the final audio fallback in `AlarmService` when no system alarm URI is playable. Apache 2.0 attribution for the asset SHALL be included in the project.

#### Scenario: Bundled asset present in APK
- **WHEN** the app is built
- **THEN** `R.raw.helium` resolves to a valid OGG audio file

#### Scenario: Bundled asset plays with alarm audio routing
- **WHEN** the bundled asset is used as the fallback alarm sound
- **THEN** it is played with `AudioAttributes.USAGE_ALARM` so it routes through the alarm audio stream
