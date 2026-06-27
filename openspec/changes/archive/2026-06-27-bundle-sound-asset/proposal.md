## Why

ChromeOS devices do not reliably have a system alarm sound set as default, causing the alarm to fire silently. Bundling a known-good AOSP alarm sound guarantees audible alarms on all supported platforms.

## What Changes

- Add `Helium.ogg` (AOSP material alarms, Apache 2.0) as a raw resource at `res/raw/helium.ogg`
- Simplify alarm audio playback to: try system default alarm URI first, fall back to bundled asset
- Remove the notification and ringtone URI fallbacks from the audio chain (system alarm URI only)
- Add Apache 2.0 attribution for the bundled sound asset

## Capabilities

### New Capabilities

- `bundled-alarm-sound`: A raw audio asset (`R.raw.helium`) guaranteed to play on any device, used as the final fallback when no system alarm sound is available

### Modified Capabilities

- `nap-alarm`: Alarm audio playback chain changes — system default alarm URI tried first; bundled asset used as fallback instead of notification/ringtone URIs

## Impact

- `app/src/main/res/raw/helium.ogg` — new file added to APK (~small, OGG)
- `AlarmService.kt` — `startAudioFadeIn()` simplified from loop-over-candidates to try/catch
- APK size increase: minimal (~100–200 KB for a short OGG alarm tone)
- No new permissions, no DataStore changes, no UI changes
