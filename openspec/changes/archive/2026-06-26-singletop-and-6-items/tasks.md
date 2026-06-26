## 1. AlarmActivity launch mode (multi-window fix)

- [x] 1.1 In `app/src/main/AndroidManifest.xml`, change `AlarmActivity` `android:launchMode` from `singleInstance` to `singleTop`

## 2. Duration grid display cap

- [x] 2.1 In `SetupScreen.kt`, change the `DurationGrid` call site to pass `history.take(5)` instead of `history`
- [x] 2.2 Verify that existing previews still compile and display correctly (no preview uses more than 5 durations with a Custom tile)
