## Context

The current alarm can be stopped or time out automatically. The handoff introduces snooze chips that should re-arm the timer while keeping the alarm's existing full-screen, sound, vibration, and auto-dismiss mechanics intact.

## Goals / Non-Goals

**Goals:**
- Add `+1 min`, `+5 min`, and `+10 min` snooze actions to the alarm screen.
- Stop the current alarm sound and vibration immediately when snoozing.
- Start a new countdown for the selected snooze duration and show the running countdown screen.
- Treat system back navigation from the alarm activity as Stop so it does not leave alarm playback or haptics running.
- Preserve foreground-service and notification behavior for the snoozed countdown.

**Non-Goals:**
- Do not change alarm auto-dismiss timing.
- Do not change the alarm fade/vibration sequence.
- Do not add configurable snooze durations.
- Do not update README content in this change.

## Decisions

- Treat snooze as a fresh countdown start from the alarm screen. This avoids keeping a completed timer active and lets the existing timer service own the new end time.
- Use fixed snooze durations: 1, 5, and 10 minutes.
- Stop alarm playback/vibration before starting the snooze countdown so the device does not continue alerting under the new timer.
- Navigate to the running countdown screen after snooze so the user sees the new remaining time immediately.
- Register explicit back handling in the alarm activity/Compose content and route it through the same stop path as the Stop button. This keeps gesture back, button back, and explicit Stop behavior consistent.

## Risks / Trade-offs

- Starting a countdown from a full-screen lock-screen alarm crosses activity/service boundaries. Mitigation: route snooze through the same controller/service API used by normal starts.
- Android background execution restrictions can affect service starts from alarm UI. Mitigation: initiate from the foreground alarm activity and reuse existing foreground-service start patterns.
- Back handling can diverge between platform back and Compose back handling. Mitigation: cover both the activity back dispatcher and Compose `BackHandler` path as appropriate for the implementation.
- Repeated snoozes could make recent-duration history noisy if tracked. Mitigation: decide during implementation whether snooze durations update recents; default to not updating recents unless product explicitly requires it.
