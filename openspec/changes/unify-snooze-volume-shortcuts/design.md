## Context

The app already centralizes alarm-screen snooze durations in `SNOOZE_OPTIONS` as an ordered list. The alarm screen renders those values as duration buttons, using the same number-plus-unit visual treatment as the main setup screen and omitting the leading `+` because the Snooze section already communicates added time.

Hardware volume-key snooze handling currently lives in `AlarmActivity.onKeyDown`, not in `AlarmService`. Both volume up and volume down are hard-coded to a 60-second snooze. The foreground notification Snooze action lives in `AlarmService` and also uses a 60-second snooze; this proposal keeps that notification behavior unchanged.

## Goals / Non-Goals

**Goals:**

- Make alarm-screen volume-key snooze shortcuts derive from the ordered `SNOOZE_OPTIONS` list.
- Map volume down to the first snooze option and volume up to the second snooze option.
- Show localized `Vol-` and `Vol+` labels below the first and second visible alarm snooze buttons.
- Keep snooze options without hardware shortcuts from reserving blank shortcut-label space.
- Preserve existing snooze countdown semantics: selecting snooze stops the current alarm and starts a new snooze countdown from the moment snooze is selected.

**Non-Goals:**

- Changing the contents or order of `SNOOZE_OPTIONS`.
- Changing notification Snooze behavior.
- Adding user-configurable hardware shortcuts.
- Adjusting button height/alignment to reserve second-line space for the third button; that is intentionally deferred for post-implementation discussion.

## Decisions

1. Use list position rather than duplicated constants for hardware shortcuts.

   Volume down uses `SNOOZE_OPTIONS[0]`; volume up uses `SNOOZE_OPTIONS[1]`. This keeps the shortcuts tied to the same ordered options shown on the alarm screen. The alternative was naming separate constants such as `VOLUME_DOWN_SNOOZE_SECONDS`, but that would preserve the drift risk this change is meant to remove.

2. Keep shortcut labels as presentation metadata outside `SNOOZE_OPTIONS`.

   `SNOOZE_OPTIONS` remains a list of durations. The first and second rendered options receive localized shortcut labels based on their index, displayed below the button rather than inside it. The alternative was introducing a richer snooze-option data type, but that is larger than needed while there are only two hardware shortcuts and no persistence/API boundary.

3. Do not reserve blank shortcut-label space for the third option.

   The third option has no shortcut label beneath its button, per the requested initial implementation. This may create uneven total option height, but it keeps blank alignment space out of the UI unless visual review proves it is needed.

4. Keep notification Snooze fixed at the first snooze option only if implementation naturally touches it; otherwise leave it as current 1-minute behavior.

   The user request is specifically about alarm-screen hardware keys and visible button labels. Notification Snooze is service-side and has no visible mapping to volume shortcuts.

## Risks / Trade-offs

- If `SNOOZE_OPTIONS` ever has fewer than two items, direct indexed access could fail or shortcuts could become unavailable. Mitigation: implement defensively or assert/test the existing invariant that the list has at least three options.
- Adding shortcut labels below only two of three buttons may look uneven at some font scales. Mitigation: keep the requested initial behavior and include a follow-up discussion task after implementation.
- Shortcut labels such as `Vol-` and `Vol+` may not be self-explanatory to all users. Mitigation: localize visible labels and include shortcut-aware accessibility descriptions.
- Physical volume keys may still be intercepted by OS/device-specific behavior in some environments. Mitigation: preserve the existing key-handling path and add focused tests around the app-level behavior.
