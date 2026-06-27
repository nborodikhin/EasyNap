## Why

Three independent maintenance concerns have surfaced that are best addressed together as a cleanup pass:

1. **Untranslated UI strings** — several formatting functions in `TimerHelpers.kt` return hardcoded English strings that are rendered directly in the UI. These strings are not in `res/values/strings.xml` and cannot be translated, which blocks future localization.

2. **Accessibility gaps** — the app has no dedicated accessibility audit and uses minimal semantic annotations in Compose. Interactive elements may lack content descriptions that screen-reader users need.

3. **Edge-to-edge warnings from Google Play** — the app targets SDK 35 and already calls `enableEdgeToEdge()`, but Google Play reports two warnings:
   - "Edge-to-edge may not display for all users" — apps targeting SDK 35 must handle insets correctly.
   - "Your app uses deprecated APIs or parameters for edge-to-edge" — specific deprecated API(s) need identification and migration.

## What Changes

### 1. Untranslated strings

The following functions produce English UI text outside of string resources:

| Function / constant | Hardcoded output | Used in |
|---|---|---|
| `formatDurationUnit(minutes)` | `"min"` / `"min:sec"` | `SetupScreen` (tile labels, unit display) |
| `formatDurationCaption(minutes)` | `"20-minute nap"` / `"1:30 nap"` | `RunningScreen` (countdown caption) |
| `formatNapDescription(seconds)` | `"X minutes Y seconds"` | `SetupScreen` (custom duration preview) |
| `SNOOZE_OPTIONS` labels | `"+1 min"`, `"+5 min"`, `"+10 min"` | `AlarmActivity` (snooze chip labels) |

**Decision options:**
- **Option A — Full i18n:** Move each to string resources; `formatDurationCaption` and `formatNapDescription` need plural resources and grammar-aware string formats.
- **Option B — Mark English-only for now:** Annotate the functions clearly as non-translatable and defer i18n to a future localization initiative.

Recommendation TBD based on whether translation is on the roadmap.

### 2. Accessibility

Audit all screens and interactive elements in Compose:
- **Duration tiles** in `SetupScreen`: do they have meaningful content descriptions for TalkBack (e.g., "Start 20-minute nap")?
- **Countdown screen** (`RunningScreen`): is the remaining time announced as it updates (live region)?
- **Alarm screen** (`AlarmActivity`): are Stop/Snooze buttons labeled clearly?
- **Custom duration keypad** (`SetupScreen`): are digit and backspace keys accessible?
- **Notification actions** — already use text labels; label text should be audited for clarity.

Fix any gaps found.

### 3. Edge-to-edge

**SDK 35 inset handling:**
- `MainActivity` already calls `enableEdgeToEdge()` and all Compose screens use `consumeWindowInsets(padding)` inside Scaffolds. This should be compliant. The warning may be triggered by the API level declaration alone; verify by testing on Android 15.

**Deprecated APIs:**
- `AlarmActivity` uses `@Suppress("DEPRECATION")` for `FLAG_SHOW_WHEN_LOCKED` and `FLAG_TURN_SCREEN_ON` on API 26 (`minSdk = 26`). These are superseded by `setShowWhenLocked()` / `setTurnScreenOn()` on API 27+, which the code already calls for API 27+. The else-branch is needed only for API 26; if the API 26 path can be dropped, the suppression goes away.
- Investigate which specific API(s) triggered the Play "deprecated parameters" warning. The app does not use `window.statusBarColor`, `FLAG_TRANSLUCENT_STATUS`, or `SYSTEM_UI_FLAG_*`. Confirm via lint or by reviewing the Play deprecation report.

## Capabilities

No user-visible capabilities change. This is a maintenance and compliance change.

## Affected Code

- `TimerHelpers.kt` — string format functions
- `SetupScreen.kt`, `RunningScreen.kt`, `AlarmActivity.kt` — callers of the format functions
- `res/values/strings.xml` — new entries if Option A is chosen
- `AlarmActivity.kt` — edge-to-edge / deprecated flags
- All Compose screens — accessibility semantics
