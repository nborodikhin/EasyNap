## 1. Countdown Display Helpers

- [x] 1.1 Add or update helper logic for formatting remaining time rounded up to the nearest second for the foreground running screen.
- [x] 1.2 Add helper logic or inline calculation for anticipated progress remaining time using `remainingMs - minOf(syncInterval, remainingMs)`.

## 2. Running Screen Animation

- [x] 2.1 Update the running countdown screen to keep visible text on a one-second sync cadence.
- [x] 2.2 Update the progress ring target to animate toward the next sync position.
- [x] 2.3 Ensure final partial intervals clamp progress to zero without negative progress.
- [x] 2.4 Preserve existing timer scheduling, foreground notification behavior, and alarm transition behavior.

## 3. Main Screen Polish

- [x] 3.1 Update main-screen alignment, spacing, and font sizes to match `design_handoff_easy_nap`.
- [x] 3.2 Revise subtitle text to "Tap a length to start your nap" (no trailing period) backed by a string resource.
- [x] 3.3 Move user-visible strings touched by this change into Android string resources.

## 4. Alarm Volume-Key Snooze

- [x] 4.1 Override `onKeyDown` in the alarm activity to intercept `KEYCODE_VOLUME_UP` and `KEYCODE_VOLUME_DOWN` and invoke the 1-minute snooze path.
- [x] 4.2 Return `true` from the key handler to consume the event and prevent system volume from changing.

## 5. Validation

- [x] 5.1 Add unit coverage for rounded-up display formatting, including `1001ms`, `1000ms`, `999ms`, `1ms`, `0ms`, and negative values.
- [x] 5.2 Add unit coverage for anticipated progress target math, including full sync intervals and final partial intervals.
- [x] 5.3 Build and test the Android app.
- [x] 5.4 Manually verify that countdown text updates once per second and progress animates smoothly until alarm transition.
- [x] 5.5 Manually verify main-screen subtitle text, spacing, typography, and absence of hardcoded touched strings.
- [x] 5.6 Manually verify that pressing volume-up or volume-down during the alarm snoozes for 1 minute and does not change system volume.
