## Context

This change is a three-part maintenance pass: full string localization, an accessibility audit, and resolution of two Google Play edge-to-edge warnings. The findings were validated against the current source, actual `R.string.*` usage counts, the shipped `app/release/app-release.aab` R8 mapping (`mapping-1000/mapping.txt`), and the `edge-to-edge` skill checklist. Two claims in the original proposal were disproven during validation and have been corrected (see Decisions).

The app has no system `TextField` (custom numeric keypad only), uses Material 3 `Scaffold` on every screen, `minSdk = 26` / `targetSdk = 35` / `compileSdk = 36`, and `androidx.activity` per `libs.versions.toml`.

## Goals / Non-Goals

**Goals:**
- Every user-facing string is in `res/values/strings.xml` / `res/values/plurals.xml` and translation-ready (Option A: full i18n).
- One consistent brand spelling — **"Easy Nap"** — everywhere.
- Grammar/pluralization expressed with plural resources, not Kotlin string concatenation; `alarm_body` reads correctly for whole-minute, sub-minute, and fractional durations.
- Concrete accessibility gaps (tile actions, delete badge, keypad, countdown progress semantics) fixed.
- Edge-to-edge: confirm inset compliance and record the deprecated-API warning's true origin and accepted status.

**Non-Goals:**
- No translation files (`values-xx/`) are authored in this change — only the app becomes translation-*ready*.
- No edge-to-edge code change; `enableEdgeToEdge()` stays (decision below).
- No `minSdk` bump; the `AlarmActivity` API-26 window-flag `@Suppress("DEPRECATION")` branch stays.
- Preview-only literals in `@Preview` / `*Stateless` helpers are developer-facing and are not localized (brand spelling there may be aligned for visual accuracy only).

## Decisions

### Localization mechanics — keep helpers pure, resolve strings at call sites
`TimerHelpers` formatters stay Context-free and JVM-unit-testable: they keep the *numeric* decomposition (whole-vs-fractional, minutes/seconds split, `formatDurationLabel` for `mm:ss`). The localized text is resolved at the call site with `pluralStringResource` (Compose) or `resources.getQuantityString` (services). This keeps `R` out of the pure logic and puts i18n where the framework expects it.

- **`formatDurationCaption`** (`RunningScreen`): whole minutes (≥ 1 min) → `pluralStringResource(R.plurals.nap_caption_minutes, n, n)` → "20-minute nap"; sub-minute → `pluralStringResource(R.plurals.nap_caption_seconds, secs, secs)` → "30-second nap".
- **`formatNapDescription`** (custom-duration preview, `CustomDurationSheetContent`, already `@Composable`): compose `R.plurals.nap_desc_minutes` and/or `R.plurals.nap_desc_seconds`, joined by `R.string.nap_desc_minutes_seconds` ("%1$s %2$s") when both are present. Sub-minute entries (0:SS) produce seconds only ("30 seconds").
- **`formatDurationUnit`** (tile unit label): `R.string.duration_unit_min` for whole-minute tiles; `R.plurals.duration_unit_sec` ("second"/"seconds") for sub-minute tiles.
- **`SNOOZE_OPTIONS`**: change from `List<Pair<String, Float>>` to `List<Float> = [1f, 5f, 10f]`; render labels in `AlarmActivity` via `pluralStringResource(R.plurals.snooze_option_minutes, n, n)` → "+1 min".
- **Notification title prefix** (`NapTimerService` / `AlarmService`, non-Composable): `resources.getQuantityString(R.plurals.notif_duration_min, n, n)` for whole minutes; `resources.getQuantityString(R.plurals.notif_duration_sec, secs, secs)` for sub-minute. Existing `notif_timer_title` / `notif_alarm_title` keep their `%1$s` prefix slot.
- **Snackbar** (`SetupScreen`): `R.string.snackbar_timer_deleted` ("%1$s timer deleted") and `R.string.snackbar_undo` ("Undo").
- **Custom tile**: `R.string.tile_custom` ("Custom").

### Brand = "Easy Nap"
`app_name` and `home_title` already use "Easy Nap". Change `notif_app_name` "EasyNap" → "Easy Nap". `notif_app_name` is **kept** (2 call sites: `NapTimerService.kt:119`, `AlarmService.kt:108` — the fallback title when duration is 0).

### `alarm_body` grammar fix
Replace the single `alarm_body` = "Your %1$s-minute nap is done. …" (broken for sub-minute durations) with:
- `R.plurals.alarm_body_minutes` (whole minutes, ≥ 1 min) — "Your %d-minute nap is done. Hope you feel refreshed." (one/other; English identical, but plural-ready).
- `R.plurals.alarm_body_seconds` (sub-minute) — "Your %d-second nap is done. Hope you feel refreshed."
`AlarmActivity` chooses based on whole-minutes-vs-sub-minute.

### `home_headline` / `home_subtitle` are NOT orphaned — disproven claim
The real `SetupScreen` renders all three home resources (`SetupScreen.kt:156-171`); the hardcoded "Take a nap" / "Choose your duration" live only in the preview-only `SetupScreenStateless` helper. No extraction or deletion there.

### Custom-duration input — allowed formats
The keypad accepts two formats only:
- **Whole minutes**: a plain integer ("25" → 25 minutes).
- **Whole seconds via `0:SS`**: the colon is available only when the minute field is exactly zero — i.e., the ":" key is disabled whenever the buffer contains at least one non-zero leading digit before the colon. This constrains `mm:ss` entries to `0:05`–`0:59`, which map to whole seconds (5–59 s). The existing rule (colon disabled once already present) is replaced by this stricter rule.

As a result, every stored duration is either a whole number of minutes (≥ 1) or a whole number of seconds (< 60 s). Fractional minute values that are not whole seconds are no longer enterable. This eliminates the `mm:ss` display format from all UI surfaces outside the custom-sheet input itself.

### RTL support (companion to full i18n)
`android:supportsRtl="true"` is already set, so the system mirrors layouts under RTL locales. Audit findings: spacing/alignment are all direction-aware (`start`/`end`, `Alignment.*End`, RTL-aware `Modifier.offset`), so the home, countdown, and alarm screens mirror correctly with no code change. Two things must be pinned **LTR** so they don't mirror:
- **The numeric keypad** (`NumericKeypad`) and the **`ValueDisplay`** it feeds — wrap in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`. Dial/numeric pads conventionally keep 1-2-3 order regardless of locale; mirroring to 3-2-1 would be wrong.
- Bare **`mm:ss`** countdown text in `RunningScreen` if BiDi reorders it inside a translated caption (verify first; pin LTR only if needed). Note: `mm:ss` no longer appears in tiles or captions; only the countdown ring and the custom-sheet input display use it.

**Digit-locale sub-decision (open):** `TimerHelpers` formats time with `"%02d:%02d".format(...)`, which uses the default locale — Arabic/Farsi would then show Arabic-Indic digits. That is technically correct localization but conflicts with the `tnum` tabular-figure styling and the app's visual design. **Recommendation:** force `Locale.ROOT` (Western digits) for the timer's `mm:ss` to preserve `tnum` alignment and a consistent clock look; revisit if a future localization pass wants locale-native digits. Confirm during implementation.

### Accessibility approach
Use Compose `Modifier.semantics` / `clearAndSetSemantics`:
- **Duration tile**: `semantics { contentDescription = <"Start 20-minute nap">; role = Role.Button }` (or `onClick` action label). Numeric label + unit currently read as two fragments; a single descriptive action label is clearer.
- **Delete badge** (`SetupScreen.kt:401`): replace `contentDescription = null` with a "Delete <duration> nap" description on the actionable badge.
- **Custom tile**: `contentDescription = "Custom duration"`; the "+" glyph stays decorative.
- **Backspace key**: `contentDescription = "Delete"` (today reads the raw "⌫"). The ":" key reads acceptably.
- **Countdown progress** (`RunningScreen` `CountdownRing`): expose `ProgressBarRangeInfo` progress semantics on the ring. Do not use a live region for the one-second countdown tick because Android Compose accessibility guidance warns that frequently updating live regions can overwhelm users.
- **Enable-notifications prompt** & other clickable `Text`: add `role = Role.Button`.
- **Decorative graphics** (alarm "✓", custom "+"): remain `contentDescription = null`.

### Contrast — fix dark-theme error color roles (validated by computed ratios)
WCAG ratios were computed for every text/background pair in the theme. All pass except two:
- **Error helper text** (`error #B3261E` on `surfaceContainer #1A211F`) = **2.51:1** (need 4.5). The theme overrides `error` with the *light*-theme red, which is too dark on dark surfaces. Because `error` is also reused as the delete-badge fill (white-on-it passes 6.5:1), the fix is to define proper dark-theme error roles: a lighter `error` (≈ M3 dark default `#F2B8B5`) for foreground text, and `errorContainer`/`onError` for the badge fill so both the helper text and the badge pass. Re-verify the badge after the change.
- **Cancel button outline** (`outline #3A4642` on `#0F1513`) = **1.88:1** (need 3.0 for UI boundaries). Low severity — the button's label is high-contrast (`#76F3E1`, well above 4.5:1) so the control is identifiable; bumping the border toward `outline`-with-more-contrast (or a subtle fill) is a nice-to-have, not a blocker.

### Font scaling — respect sp, free fixed-height containers
All text already uses `sp`, so user font scaling (including Android 14 non-linear to 200%) is honored. The risk is scaled text inside **fixed-`dp` containers**, the documented anti-pattern. Convert fixed `height(...)` to `heightIn(min = ...)` on the keypad, Start, Cancel, and alarm buttons; let the countdown ring's number grow or verify it does not clip at 200%; ensure a scroll fallback exists on the compact-landscape custom sheet (portrait already scrolls). Add `@Preview(fontScale = 2f)` coverage. Do **not** cap or override `fontScale`.

### Reduced motion
The app uses infinite/looping animations (the blinking cursor `infiniteRepeatable`, the progress tween, the notification-prompt fade). Honor the system "Remove animations" setting: when animations are disabled (animator duration scale 0), skip the infinite cursor blink and any non-essential motion, showing the end state directly. Vestibular-safety concern.

### Text expansion (localization robustness)
Translated strings run ~30% longer. Fixed-width duration tiles and single-line button labels can truncate. Verify with the `en-XA` pseudolocale (text expansion + accent marks) and an `ar-XB` (RTL pseudolocale) `@Preview`; allow labels to wrap or ellipsize gracefully rather than clip.

### Edge-to-edge — accept the deprecated-API warning (validated)
Deobfuscating the Play report (release 1001) with `mapping-1000/mapping.txt` proved the flagged `setStatusBarColor` / `setNavigationBarColor` / `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` calls live in `androidx.activity` (`EdgeToEdgeApi26/29/35`, `EdgeToEdgeBase.adjustLayoutInDisplayCutoutMode`), reached only via `enableEdgeToEdge()` — **not** app code and **not** the framework theme (an earlier hypothesis, now disproven). These setters are how `enableEdgeToEdge()` paints transparent/scrimmed bars on API 26–34 by design.

**Decision: keep `enableEdgeToEdge()` and accept the warning** as expected, no-user-impact library behavior. Replacing it with `WindowCompat.setDecorFitsSystemWindows(window, false)` would clear the warning (R8 strips the unused `EdgeToEdge*` classes) but regress system-bar transparency to opaque theme defaults on API 26–34 — not worth it. Inset handling is already compliant (validated against the `edge-to-edge` skill checklist; no `TextField` → no `adjustResize`/IME work). Action is limited to a one-time Android-15 visual verification.

## Risks / Trade-offs

- **Plural correctness** — wrong `quantity` args produce mis-pluralized copy. Mitigation: unit-test the numeric decomposition; keep one/other forms identical in English so behavior is unchanged today.
- **Helper signature churn** (`SNOOZE_OPTIONS` → `List<Float>`, formatters resolved at call sites) touches `AlarmActivity`, `RunningScreen`, `SetupScreen`, both services, and their tests. Mitigation: pure helpers stay independently testable; call-site changes are mechanical.
- **Live-region chattiness** — announcing every second could be noisy. Mitigation: `Polite` mode coalesces; only the remaining-time node is a live region.
- **Edge-to-edge warning persists** in Play after this change (by accepted decision) and a version bump cannot fix it: the project is already on `androidx.activity` **1.13.0**, confirmed as the current latest stable via the AndroidX Stable Release Channel (Android Knowledge Base, `android docs`). The shipped build's mapping already contains `EdgeToEdgeApi35`, yet the warning fired — the deprecated `setStatusBarColor`/`setNavigationBarColor` calls remain in the API 26–34 paths by design (no non-deprecated alternative exists below API 35) and `minSdk = 26` keeps those classes in the DEX, so Play's static scan flags them regardless. Effectively permanent while `minSdk < 35`. Mitigation: documented origin + rationale so it isn't re-investigated.
