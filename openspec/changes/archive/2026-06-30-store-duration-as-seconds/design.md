## Context

Easy Nap stores and passes nap durations as `Float` minutes across every layer: DataStore keys, `PersistedTimer`, `TimerState.Running`, `TimerStore`, `TimerController`, helper functions, and UI. The custom-duration input already parses to integer seconds internally (`parseCustomDurationSeconds → Int`), but then immediately converts back to `Float` minutes (`seconds / 60f`) before handing off to the controller. This round-trip forces every consumer to undo the conversion, and creates float-precision edge cases for sub-minute durations (e.g. 80 s → 1.3333…f → round-trip to 80 back only with `roundToInt`).

The app has no live users, so a breaking persistence change is acceptable.

## Goals / Non-Goals

**Goals:**
- Replace `Float` minutes with `Int` seconds as the canonical duration unit across `TimerStore`, `PersistedTimer`, `TimerState`, `TimerController`, helper functions, UI, and tests.
- Introduce new DataStore preference keys for the changed fields so upgraded installs behave like fresh installs for those values.
- Keep all user-visible behavior identical (tile labels, custom duration entry, countdown math, alarm copy, snooze, deletion/undo).

**Non-Goals:**
- Migrating existing persisted float-minute values to the new integer-second keys.
- Changing anything about `end_at_millis` (it stays `Long` milliseconds, unchanged).
- UI or string resource changes.

## Decisions

### Use `Int` seconds as the canonical duration representation

The product model treats durations as whole seconds — the custom input parses to `Int`, display math uses `seconds / 60` for minute labels and `seconds % 60` for the remainder. Storing as `Int` seconds removes the float-minutes wrapper entirely, making every layer unambiguous and eliminating the `roundToInt` call on read-back.

**Alternative considered:** Store as `Long` seconds for forward-compatibility. Rejected — nap durations fit comfortably in `Int` (max 7200 s = 2 h), and `Int` matches `parseCustomDurationSeconds`'s return type exactly.

### New DataStore keys, no migration of duration values

Introduce `intPreferencesKey("nap_duration_seconds")` and `stringPreferencesKey("duration_history_seconds")`. The old `floatPreferencesKey("nap_duration_minutes")` and `stringPreferencesKey("duration_history")` are simply not read by any new code — they stay in the DataStore file but are ignored. An upgraded install will see the new keys as absent and fall back to defaults, which is the correct behavior.

**Alternative considered:** Read old keys and convert on first access. Rejected — adds complexity and the proposal explicitly rules it out.

### History stored as comma-separated integers

The new history string format is `"300,600,1800"` (integer seconds) instead of `"5.0,10.0,30.0"` (float minutes). The parsing function changes from `toFloatOrNull()` to `toIntOrNull()`. The separator (`,`) and cap (5 entries) are unchanged.

### `DEFAULT_HISTORY` changes to `listOf(300, 600, 1800)`

5 min, 10 min, 30 min expressed as seconds. The constant type changes from `List<Float>` to `List<Int>`.

### Remove `durationTotalSeconds(minutes: Float): Int`

This helper existed solely to convert a stored `Float` minutes value into `Int` seconds for display. With storage already in seconds, every call site can use the `Int` value directly. Removing it simplifies the helper surface.

**Callers to update:** `DurationTile` and `SetupScreen` (snackbar label), `RunningScreen`, `AlarmActivity`, `NapTimerService`.

### Rename `durationDisplayMinutesOrNull` and `formatDurationLabel` to accept `Int`

- `durationDisplayMinutesOrNull(seconds: Int): Int?` — returns `seconds / 60` when `seconds >= 60`, else `null`.
- `formatDurationLabel(seconds: Int): String` — shows whole minutes when `seconds % 60 == 0`, otherwise `mm:ss`.
- `SNOOZE_OPTIONS: List<Int>` — `listOf(60, 300, 600)` (1, 5, 10 minutes in seconds).

### Padding threshold expressed in seconds

`TimerController.startInternal` currently checks `durationMinutes >= 1f` for the 5-second padding. The equivalent check after the change is `durationSeconds >= 60`.

### `TimerState.Running` field renamed

`durationMinutes: Float` → `durationSeconds: Int`. All consumers (RunningScreen, TimerController, NapTimerService, tests) update accordingly.

## Risks / Trade-offs

**Persisted state loss on upgrade** → Acceptable — no live users. Upgraded installs fall back to the same defaults as fresh installs.

**Snooze duration type change** (`SNOOZE_OPTIONS: List<Int>`) → `AlarmActivity` currently calls `timerController.startSnooze(minutes: Float)`. After the change, `startSnooze(seconds: Int)` takes the value from `SNOOZE_OPTIONS` directly without conversion. The volume-key snooze hard-codes `1f` today; it will hard-code `60` after the change.

**Two `FakeTimerStore` copies** (src/test and src/androidTest) — both must be updated in sync. They are identical in content; both change in the same way.

## Open Questions

None — the proposal is explicit about scope, migration policy, and backward-compatibility expectations.
