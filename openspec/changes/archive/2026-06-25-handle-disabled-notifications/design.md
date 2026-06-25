## Context

EasyNap currently requests `POST_NOTIFICATIONS` from `MainActivity`, but the result is ignored and the setup UI does not reflect app or channel notification state. Notification channels are created lazily in `NapTimerService.onCreate()`, which is too late for setup-screen detection on a fresh install.

The app supports API 26 and newer, so notification channels are always available. Alarm reliability depends on both app-level notifications and the alarm channel being enabled.

## Goals / Non-Goals

**Goals:**

- Detect whether alarm notifications are effectively available before the user starts a nap.
- Show a low-friction setup-screen prompt when notifications are unavailable.
- Send users to the relevant Android notification settings screen from that prompt.
- Create notification channels early enough that channel state can be checked while idle.

**Non-Goals:**

- Blocking nap start when notifications are disabled.
- Reworking full-screen intent permission handling.
- Changing alarm audio, vibration, snooze, or timeout behavior.
- Supporting Android versions below API 26.

## Decisions

### Use an effective notification availability check

The app should consider alarm notifications available only when app notifications are enabled and the alarm channel is not blocked. `NotificationManagerCompat.areNotificationsEnabled()` covers app-level blocking and Android 13+ `POST_NOTIFICATIONS` denial. The API 26+ alarm channel importance check covers Android O+ channel blocking.

Alternative considered: checking only `POST_NOTIFICATIONS`. This misses Android 8-12 app/channel blocking and does not match the supported device range.

### Create notification channels before setup UI checks status

Move channel creation into a reusable notification helper that can run during app or main activity startup. Services should still be safe if they call the helper again, because channel creation is idempotent.

Alternative considered: leave channel creation in `NapTimerService`. That cannot reliably detect alarm-channel blocking before the first timer service has ever started.

### Open system notification settings from the setup prompt

Clicking the warning should open Android notification settings for EasyNap. If the alarm channel exists, channel settings are the most direct target; otherwise app notification settings is the fallback.

Alternative considered: relaunching the runtime notification permission request. That only handles Android 13+ runtime denial and does not help with app-level or channel-level blocking.

### Keep the warning lightweight and non-blocking

The setup screen should show a small bottom-end text prompt rather than a modal or blocking banner. Users can still start a timer, but the risk is visible before they rely on the alarm.

Alternative considered: preventing timer start while notifications are disabled. That is stricter but could frustrate users who intentionally rely on sound/vibration or are testing the app.

## Risks / Trade-offs

- Settings screen availability varies by OEM -> Use standard settings intents and fall back from channel settings to app notification settings where needed.
- Notification state can change while the app is backgrounded -> Re-check when the setup screen/activity resumes, not only at initial composition.
- Very small warning text can be missed -> Use clear wording and color/placement that is visible without dominating the setup flow.
- Channel creation may preserve an already-modified channel -> This is expected Android behavior; creating channels early must not attempt to override user choices.
