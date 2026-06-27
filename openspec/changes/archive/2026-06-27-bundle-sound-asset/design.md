## Context

`AlarmService.startAudioFadeIn()` currently tries three system URIs in sequence (`TYPE_ALARM` → `TYPE_NOTIFICATION` → `TYPE_RINGTONE`). On ChromeOS, none of these resolve reliably, so all three fail silently and the alarm fires with vibration only. The fix is to bundle a known-good audio asset as a guaranteed last resort.

## Goals / Non-Goals

**Goals:**
- Guarantee audible alarm on ChromeOS (and any device with no system alarm sound)
- Simplify the audio fallback chain — one system URI attempt, one bundled fallback
- Add Apache 2.0 attribution for the bundled asset

**Non-Goals:**
- User-selectable alarm sound (out of scope for this change)
- Detecting the absence of a system sound before alarm fires
- Changing the fade-in/fade-out sequence or any other alarm behavior

## Decisions

### Use `TYPE_ALARM` URI only, drop notification/ringtone fallbacks

**Decision**: Remove `TYPE_NOTIFICATION` and `TYPE_RINGTONE` from the fallback chain.

**Rationale**: These were defensive fallbacks from before the bundled asset existed. With `R.raw.helium` as the guaranteed final fallback, there is no need to reach for notification or ringtone sounds, which are semantically wrong audio streams for an alarm and played at the wrong volume level.

**Alternative considered**: Keep the full chain before the bundled fallback. Rejected — it adds noise, can play a notification ding before the bundled sound kicks in, and the behavior is unpredictable.

### Use `openRawResourceFd` for the bundled asset, not `MediaPlayer.create()`

**Decision**: Use the same `MediaPlayer()` constructor with `setAudioAttributes()` + `setDataSource(afd, ...)` pattern as the URI path.

**Rationale**: `MediaPlayer.create(context, resId)` calls `prepare()` internally but does not accept audio attributes before that call, which means the stream type is not set to `USAGE_ALARM`. Using `openRawResourceFd` keeps the audio routing correct and the code consistent.

### Helium.ogg from AOSP material alarms

**Decision**: Use `frameworks/base/data/sounds/alarms/ogg/Helium.ogg`.

**Rationale**: Apache 2.0 licensed (same as existing Material icons in the project). Short enough to loop cleanly within the 55-second alarm window. Gentle tone appropriate for a nap timer.

## Risks / Trade-offs

- **APK size** — OGG file adds ~100–200 KB. Acceptable for the guarantee it provides.
- **Bundled sound preference** — If a user has a system alarm sound configured, they get that. If not, they get Helium. There is no way to opt out of Helium once it's the fallback. Acceptable given no user-selectable sound is in scope.
- **`openRawResourceFd` can throw** — Wrapped in a try/catch like the URI path. If the bundled asset is somehow corrupt or missing from the APK build, the alarm continues with vibration only (same safe behavior as today).
