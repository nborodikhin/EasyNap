## 1. String resources & brand

- [x] 1.1 Rebrand `notif_app_name` "EasyNap" → "Easy Nap" in `strings.xml` (verify `app_name` / `home_title` already read "Easy Nap").
- [x] 1.2 Add `res/values/plurals.xml` with: `nap_caption_minutes`, `nap_desc_minutes`, `nap_desc_seconds`, `snooze_option_minutes`, `notif_duration_min` (plural form), `alarm_body_minutes`.
- [x] 1.3 Add string resources: `nap_desc_minutes_seconds` ("%1$s %2$s"), `duration_unit_min` ("min"), `snackbar_timer_deleted` ("%1$s timer deleted"), `snackbar_undo` ("Undo"), `tile_custom` ("Custom"). Add plurals: `nap_caption_seconds` ("%d-second nap"), `duration_unit_sec` ("second"/"seconds"), `notif_duration_sec` ("%d sec"), `alarm_body_seconds` ("Your %d-second nap is done. Hope you feel refreshed."). Remove `duration_unit_min_sec`, `nap_caption_mmss`, and `alarm_body_duration` (replaced by the seconds plurals).
- [x] 1.4 Fix `alarm_body`: remove the single broken string; `AlarmActivity` selects `alarm_body_minutes` (whole minutes) vs `alarm_body_seconds` (sub-minute).

## 2. Localize formatters & call sites (full i18n)

- [x] 2.1 `TimerHelpers`: keep formatters Context-free — retain numeric decomposition and `formatDurationLabel`; remove the hardcoded English from `formatDurationUnit`, `formatDurationCaption`, `formatNapDescription`. Change `SNOOZE_OPTIONS` to `List<Float> = listOf(1f, 5f, 10f)`.
- [x] 2.2 `RunningScreen`: build the caption via `pluralStringResource(nap_caption_minutes)` (≥ 1 min) / `pluralStringResource(nap_caption_seconds)` (sub-minute).
- [x] 2.3 `SetupScreen`: tile unit label via `duration_unit_min` (whole minutes) / `pluralStringResource(duration_unit_sec)` (sub-minute); snackbar via `snackbar_timer_deleted` + `snackbar_undo`; custom tile via `tile_custom`; custom-duration description via `nap_desc_*` plurals composed in `CustomDurationSheetContent`.
- [x] 2.3a `SetupScreen` keypad: tighten the ":" enable rule — colon is available only when the buffer's leading digit(s) are all zero (i.e., minute field is 0). Replace the current `':' !in inputBuffer` guard with a check that the integer value before `:` is zero.
- [x] 2.4 `AlarmActivity`: render snooze labels via `pluralStringResource(snooze_option_minutes)` from `SNOOZE_OPTIONS` floats; select the correct `alarm_body_*`.
- [x] 2.5 `NapTimerService` & `AlarmService`: build the notification duration prefix via `resources.getQuantityString(notif_duration_min, …)` for whole minutes, `resources.getQuantityString(notif_duration_sec, secs, secs)` for sub-minute.
- [x] 2.6 Align brand spelling in the preview-only `SetupScreenStateless` helper to "Easy Nap" (cosmetic; not localized).

## 2b. RTL support (part of i18n)

- [x] 2b.1 Pin the numeric keypad + its `ValueDisplay` to LTR: wrap in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` so digit order and cursor don't mirror.
- [x] 2b.2 Verify `mm:ss` displays (countdown ring only — tiles and captions no longer use mm:ss) render correctly in RTL; pin the bare countdown text LTR if BiDi reorders it.
- [x] 2b.3 Decide digit locale for `%02d:%02d` formatting — localized digits (default locale → Arabic-Indic) vs forced Western (`Locale.ROOT`) for `tnum` consistency; apply the chosen `Locale` in `TimerHelpers`. See design.md.
- [x] 2b.4 Confirm no `left`/`right`/`absoluteOffset` regressions; verify `Alignment.*End` + `offset` elements (notif prompt, delete badge) still mirror correctly.
- [x] 2b.5 Add an RTL preview (`@Preview(locale = "ar")` or `LayoutDirection.Rtl`) for the home, countdown, alarm, and custom-sheet screens.

## 3. Accessibility (audited against Android KB Compose a11y docs)

- [x] 3.1 `DurationTile` `combinedClickable`: add `Role.Button`, `onClickLabel = "Start <duration> nap"`, and `onLongClickLabel = "Delete <duration> nap"` so the long-press delete is discoverable to TalkBack (which cannot perform long-press).
- [x] 3.2 `DurationTile` delete badge: add a `contentDescription = "Delete <duration> nap"` on the clickable `Box` (keep the inner `Icon` non-announced).
- [x] 3.3 `CustomTile`: `clearAndSetSemantics`/`contentDescription = "Custom duration"`; the "+" is a `Text` glyph, so hide it via the merged semantics (not a null `contentDescription`).
- [x] 3.4 Keypad backspace key: pass a `contentDescription = "Delete"` for "⌫" via `KeypadButton` (special keys); verify ":" announces acceptably.
- [x] 3.5 `RunningScreen` countdown ring: expose `ProgressBarRangeInfo` progress semantics instead of a live region, so accessibility services can query timer progress without announcing every second.
- [x] 3.6 Add `Role.Button` to clickable `Text` controls (enable-notifications prompt); optionally mark the home headline a `heading()`.
- [x] 3.7 Hide decorative glyphs from accessibility — alarm "✓" (`AlarmCheckIcon`), custom "+", and the `ValueDisplay` cursor "|" — via `clearAndSetSemantics {}` / `hideFromAccessibility()` (they are `Text`, not `Icon`, so a null `contentDescription` does not apply).
- [x] 3.8 Confirm all interactive targets are ≥48dp (delete badge is 48dp; tiles ≥72dp; keypad ≥48dp) — no resize needed, just verify.

## 3c. Contrast, font scaling & motion

- [x] 3c.1 Fix dark-theme error roles in `Color.kt`/`Theme.kt`: use a light `error` (≈ `#F2B8B5`) for foreground text (custom-duration error 2.51:1 → ≥4.5:1); switch the delete badge to `errorContainer`/`onError` (or keep a dark fill) so its icon still passes ≥3:1. Re-run the contrast check.
- [x] 3c.2 (Optional) Raise the Cancel button outline contrast (1.88:1) toward ≥3:1, or accept since the label is high-contrast. Decision: accepted — label (#76F3E1) is well above 4.5:1.
- [x] 3c.3 Convert fixed `height(...)` to `heightIn(min = ...)` on keypad buttons, Start, Cancel, and alarm buttons so scaled text isn't clipped.
- [x] 3c.4 Ensure the countdown ring number does not clip at 200% font scale (let it grow / constrain text); add a scroll fallback to the compact-landscape custom sheet.
- [x] 3c.5 Honor reduce-motion: when animator duration scale is 0, skip the blinking cursor and non-essential animations and show the end state.
- [x] 3c.6 Add `@Preview(fontScale = 2f)` and pseudolocale previews (`@Preview(locale = "en-XA")`, `"ar-XB"`) for the home, countdown, alarm, and custom-sheet screens.

## 4. Edge-to-edge (no code change)

- [x] 4.1 Confirm app sources contain no direct deprecated edge-to-edge calls (`setStatusBarColor`/`setNavigationBarColor`/`SYSTEM_UI_FLAG_*`); only `enableEdgeToEdge()`.
- [x] 4.2 Manually verify all screens display edge-to-edge with no obscured content on an Android 15 (SDK 35) device/emulator. Verified on connected Pixel 6a API 36 with home, custom sheet, running, and alarm screenshots/layout dumps.
- [x] 4.3 (Done in design.md) Record that the Play "deprecated edge-to-edge APIs" warning originates in `androidx.activity`'s `EdgeToEdge*` (via `enableEdgeToEdge()`) and is accepted.

## 5. Validation

- [x] 5.1 Update unit tests for `TimerHelpers` (numeric decomposition; `SNOOZE_OPTIONS` shape change) and any tests asserting old hardcoded strings.
- [x] 5.2 Build the debug app; run `lint` and confirm no new localization/accessibility regressions.
- [x] 5.3 Manual TalkBack pass: duration tiles (start + long-press delete action), delete badge, keypad backspace, countdown progress semantics, alarm Stop/Snooze; spot-check with Accessibility Scanner and/or `android layout` for missing labels and <48dp targets. Verified with `android layout` accessibility tree and target bounds.
- [x] 5.4 Verify notification titles, alarm body, captions, and snooze labels render correctly for whole-minute and sub-minute (whole-second) durations.
- [x] 5.5 RTL pass: switch the device to an RTL locale (or force-RTL developer option) and confirm screens mirror, the keypad stays LTR, and `mm:ss` is not reversed.
- [x] 5.6 Contrast/scaling/motion pass: re-run the contrast check after the error-color fix; view all screens at 200% font scale and under `en-XA` for clipping/truncation; toggle "Remove animations" and confirm motion is suppressed. Verified contrast by WCAG calculation and device font-scale/force-RTL/remove-animations pass.
