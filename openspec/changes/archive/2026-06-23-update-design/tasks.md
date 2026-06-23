## 1. Duration Selection

- [x] 1.1 Replace the long 5-60 minute quick-start list with a compact quick duration table capped at 6 distinct entries.
- [x] 1.2 Seed the quick duration table with 5, 10, and 30 minute entries when no saved history is available.
- [x] 1.3 Move the existing "Timer duration (minutes)" decimal input below the quick duration table and preserve its current parsing behavior.
- [x] 1.4 Save started quick/custom durations into the table history, dedupe by duration, and move repeated durations to the newest position without introducing keypad or `mm:ss` entry.

## 2. Countdown And Alarm Screens

- [x] 2.1 Update the running timer screen to show the chosen-duration caption, circular progress ring, live remaining time, "REMAINING" label, and "Cancel nap" button.
- [x] 2.2 Preserve the existing foreground service and ongoing countdown notification behavior.
- [x] 2.3 Update the alarm screen to show the completed-ring visual, "Time to wake up" title, duration-specific body copy, and "Stop" action.
- [x] 2.4 Wire "Stop" to the existing alarm stop behavior without changing auto-dismiss timing or sound/vibration sequencing.

## 3. Visual System And Assets

- [x] 3.1 Apply the Calm Teal Material 3 dark color scheme across Compose theme tokens.
- [x] 3.2 Update typography for Roboto-compatible styles and tabular timer/duration numerals.
- [x] 3.3 Apply handoff spacing, rounded shapes, button heights, and minimum touch targets.
- [x] 3.4 Ensure screens draw edge-to-edge with transparent system bars and legible dark surfaces.
- [x] 3.5 Replace launcher icon resources with the adaptive alarm clock plus `zzz` direction.

## 4. Validation

- [x] 4.1 Run unit tests for duration parsing/timer helpers.
- [x] 4.2 Build the debug Android app.
- [x] 4.3 Manually verify setup, countdown, alarm, and launcher icon visuals on an emulator or device.
