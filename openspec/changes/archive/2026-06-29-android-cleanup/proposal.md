## Why

Three independent maintenance concerns have surfaced that are best addressed together as a cleanup pass:

1. **Strings need a full review** — user-facing copy is split between `res/values/strings.xml` and hardcoded literals in Compose and the grammar-formatting helpers in `TimerHelpers.kt`. The resource file has also drifted: the brand is spelled two ways ("Easy Nap" vs "EasyNap") and the `alarm_body` format string produces broken grammar for `mm:ss` durations. This blocks localization and risks inconsistent or incorrect copy.

2. **Accessibility gaps** — the app has no dedicated accessibility audit and uses minimal semantic annotations in Compose. Several interactive elements (duration tiles, the delete badge, the backspace key) lack the content descriptions and roles screen-reader users need, and the countdown needs non-noisy progress semantics.

3. **Edge-to-edge warnings from Google Play** — the app targets SDK 35 and already calls `enableEdgeToEdge()`, but Google Play reports two warnings:
   - "Edge-to-edge may not display for all users" — apps targeting SDK 35 must handle insets correctly.
   - "Your app uses deprecated APIs or parameters for edge-to-edge" — a specific deprecated API/parameter needs identification and migration.

## What Changes

### 1. Review all strings — full localization (Option A)

Make the app translation-ready: every **user-facing** string lives in `res/values/strings.xml` (or `res/values/plurals.xml`), and grammar/pluralization is expressed with plural resources rather than Kotlin string concatenation. Preview-only literals (in `@Preview`/`*Stateless` helpers) are developer-facing and out of scope for translation, but should still read with the canonical brand for visual accuracy.

**a. Extract the remaining hardcoded user-facing literals.** Audited offenders in shipping (non-preview) code:

| Location | Hardcoded text |
|---|---|
| `SetupScreen` snackbar | message `"<label> timer deleted"` and action `"Undo"` (`SetupScreen.kt:121-122`) |
| `SetupScreen` custom tile | `"Custom"` (`SetupScreen.kt:433`) |
| `TimerHelpers.formatDurationUnit` | `"min"` / `"sec"` |
| `TimerHelpers.formatDurationCaption` | `"<n>-minute nap"` / `"<n>-second nap"` |
| `TimerHelpers.formatNapDescription` | `"<n> minutes <s> seconds"` |
| `TimerHelpers.SNOOZE_OPTIONS` | `"+1 min"`, `"+5 min"`, `"+10 min"` |

Note: the home header (`home_title`, `home_headline`, `home_subtitle`) is **already** sourced from resources in the real `SetupScreen` (`SetupScreen.kt:156-171`); the hardcoded "EasyNap"/"Take a nap"/"Choose your duration" appear **only** in the preview-only `SetupScreenStateless` helper. So those resources are *not* orphaned — no extraction is needed there.

Because grammar is involved, the caption/description/notification-prefix helpers move from raw Kotlin to plural resources (`plurals.xml`) and resource format strings, with the duration value injected at the call site.

**b. Fix existing `strings.xml` defects:**
- **Brand consistency** — apply **"Easy Nap"** (with a space) everywhere. Change `notif_app_name` from "EasyNap" to "Easy Nap". (`app_name`/`home_title` already use "Easy Nap".)
- **`alarm_body` grammar bug** — `alarm_body` = "Your %1$s-minute nap is done." is fed `formatDurationLabel()`, which returns `"0:05"` for sub-minute durations, producing "Your 0:05-minute nap is done." Replace with grammar-correct forms: a plural for whole minutes and a seconds plural for sub-minute durations.
- **Keep `notif_app_name`** — it is still referenced (2 usages: `NapTimerService.kt:119`, `AlarmService.kt:108`) as the fallback notification title when duration is 0. Not orphaned; just rebrand it.
- **Review newly added notification strings** — `notif_timer_title`, `notif_alarm_title`, `notif_action_stop`, `notif_action_snooze`, `notif_duration_min` (added by the `notification-buttons` change) as part of the same pass; align them with the plural approach where they carry grammar.

**c. RTL & text-expansion readiness.** `supportsRtl="true"` is already set and spacing/alignment are direction-aware, so screens mirror correctly. Two fixes for RTL locales: pin the numeric keypad + value display to LTR (so digits don't mirror), and verify `mm:ss` time strings aren't reordered. Includes a digit-locale decision (Western vs locale-native digits) — see design.md. Also verify layouts tolerate ~30%-longer translated labels (pseudolocale `en-XA`) without truncation.

### 2. Accessibility

Audit all screens and fix the concrete gaps found:
- **Duration tiles** (`SetupScreen.DurationTile`) use `combinedClickable` with no semantic action label — add a content description / click-action label such as "Start 20-minute nap".
- **Delete badge** icon has `contentDescription = null` (`SetupScreen.kt:401`) — give it a meaningful label ("Delete 20-minute nap").
- **Custom tile** ("+ Custom") should expose a clear description ("Custom duration").
- **Backspace key** in the numeric keypad reads as the raw "⌫" glyph — add a content description ("Delete").
- **Countdown progress** (`RunningScreen`) should expose progress semantics without announcing every one-second update as a live region.
- **Enable-notifications prompt** is clickable `Text` without a button role — annotate it.
- Decorative-only glyphs (alarm "✓", custom "+", input cursor "|") are hidden from accessibility (`clearAndSetSemantics`/`hideFromAccessibility`; they are `Text`, so a null `contentDescription` does not apply).

Plus the audited cross-cutting concerns:
- **Contrast** — computed WCAG ratios; all pass except the custom-duration **error text (2.51:1)** and the Cancel button outline (1.88:1). Fix the dark-theme error color roles (the error red is the light-theme value) and re-verify the delete badge.
- **Font scaling** — all text uses `sp` (respects up to Android 14's 200% non-linear scaling), but fixed-`dp` containers (countdown ring, keypad/Start/Cancel/alarm buttons) risk clipping enlarged text; free their heights and verify at 200%.
- **Reduced motion** — honor the system "Remove animations" setting (skip the blinking cursor and other looping animations).
- **Verified OK:** alarm pairs sound with vibration + full-screen visual (deaf/HoH coverage); delete/error states use icon+text, not color alone.

### 3. Edge-to-edge

**Deprecated APIs/parameters (the actionable Play warning) — root cause validated against the shipped AAB's R8 mapping:**

The Play report for release 1001 flags `Window.setStatusBarColor`, `Window.setNavigationBarColor`, and `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` at `b.r.b`, `b.u.b`, `b.w.b`, `b.s.m`. Deobfuscating with `mapping-1000/mapping.txt`:

- `b.r` = `androidx.activity.EdgeToEdgeApi26`, `b.u` = `EdgeToEdgeApi29`, `b.w` = `EdgeToEdgeApi35`, `b.s` = the API28 cutout `$$ExternalSyntheticApiModelOutline0`.
- `EdgeToEdgeApi26.setUp` / `EdgeToEdgeApi29.setUp` call `window.setStatusBarColor` / `setNavigationBarColor` (`EdgeToEdge.kt:281-284` / `314-321`) to paint the system-bar scrim on API 26–34. The cutout-mode constant comes from `EdgeToEdgeBase.adjustLayoutInDisplayCutoutMode`.

These calls live **inside `androidx.activity` and are reached only through `enableEdgeToEdge()`** — not in app code and not in the theme. (Earlier I wrongly suspected the framework theme's bar colors; the mapping disproves that.) Play's static scan walks bundled library bytecode, so it reports them regardless. The `@Suppress("DEPRECATION")` window-flag branch in `AlarmActivity` is unrelated and is **not** among the flagged APIs.

Fix options (decision pending — see design.md): (a) replace `enableEdgeToEdge()` with a manual, non-deprecated setup (`WindowCompat.setDecorFitsSystemWindows(window, false)` + `WindowInsetsControllerCompat` for icon appearance) and let R8 strip the unused `EdgeToEdge*` classes so the deprecated calls leave the DEX; or (b) accept it as a known, no-user-impact `androidx.activity` issue and wait for an upstream fix.

**Inset handling ("may not display for all users"):**
- Validated against the `edge-to-edge` skill checklist: `MainActivity` and `AlarmActivity` both call `enableEdgeToEdge()`, all content sits in `Scaffold`s that consume insets, and there are **no** `TextField`/`OutlinedTextField`/`BasicTextField` (custom keypad only), so `adjustResize`/IME handling is not required. This warning is the generic SDK-35-target advisory, not a code defect. Verify on an Android 15 device.

**Out of scope:** the `@Suppress("DEPRECATION")` in `AlarmActivity` is a correctly guarded API-26 (`FLAG_SHOW_WHEN_LOCKED`/`FLAG_TURN_SCREEN_ON`) branch. Removing it would require raising `minSdk` to 27 (dropping Android 8.0); deferred unless explicitly requested.

## Capabilities

No user-visible capabilities change. This is a maintenance and compliance change. It strengthens the existing `android-compliance` localization and edge-to-edge requirements and introduces an `accessibility` capability.

## Affected Code

- `res/values/strings.xml` — brand consistency ("Easy Nap"), fix `alarm_body`, new entries for extracted literals
- `res/values/plurals.xml` (new) — plural resources for nap duration/caption/description grammar
- `MainActivity.kt`, `AlarmActivity.kt` — (if fix option (a) chosen) replace `enableEdgeToEdge()` with `WindowCompat.setDecorFitsSystemWindows(window, false)` + insets-controller icon appearance, so R8 strips the `androidx.activity` `EdgeToEdge*` classes that call the deprecated APIs
- `TimerHelpers.kt` — caption/description/unit/snooze helpers become resource-backed (or take a `Context`/`Resources`)
- `SetupScreen.kt` — snackbar + custom-tile literals; accessibility semantics on tiles, delete badge, keypad
- `RunningScreen.kt` — caption call site; progress semantics for remaining time
- `AlarmActivity.kt` — `alarm_body` call site; snooze-chip labels; accessibility labels
- `NapTimerService.kt`, `AlarmService.kt` — notification title prefix via plural resources
- All Compose screens — accessibility semantics
