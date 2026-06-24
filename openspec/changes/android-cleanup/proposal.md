## Why

The codebase has accumulated Android-specific compliance gaps (Compose UI, edge-to-edge insets, localization, manifest/alarms, launcher resources) and code-quality risks that were initially scoped inside the polish change. Separating them keeps polish focused on UI fidelity while giving compliance and quality work its own tracking, scope, and review gate.

## What Changes

- Review the codebase with relevant Android skills covering Compose UI, edge-to-edge/insets, localization/string resources, manifest/components, notifications/alarms, and launcher resources.
- Fix concrete low-risk Android compliance issues found during the review.
- Review the codebase with a code-review mindset, prioritizing bugs, regressions, missing tests, and risky Android lifecycle/Compose behavior.
- Fix clear in-scope quality issues found during the review.
- Document any larger out-of-scope findings for separate OpenSpec changes.
- Introduce Hilt for dependency injection: convert `TimerController` from a Kotlin `object` singleton (with manual `init(context)`) to a `@Singleton`-scoped Hilt-injectable class; wire `TimerPreferenceStore` and other dependencies through constructor injection; add `@HiltAndroidApp` to `EasyNapApp` and `@AndroidEntryPoint` to Activities/Services.
- Extract interfaces from concrete implementations where testability or substitutability benefits: at minimum define a `TimerStore` interface backed by `TimerPreferenceStore`, so ViewModels and the timer controller depend on the abstraction rather than the DataStore implementation.
- Restructure the source tree to match Android convention: group files into feature/layer sub-packages (`timer/`, `alarm/`, `ui/`, `data/`) instead of leaving all classes in the root `me.easynap` package.
- Add `@Preview` annotations for every public and private `@Composable` in the UI layer: `SetupScreen`, `DurationGrid`, `DurationTile`, `CustomTile`, `CustomDurationSheet` (arbitrary-time keypad), `ValueDisplay`, `NumericKeypad`, `KeypadButton`, and `RunningScreen`. Previews should cover representative states (e.g. empty vs. filled input buffer, snooze vs. normal running state).
- Add pure JVM unit tests (with Mockito where fakes are needed) for logic that has no Android dependency: timer math, history deduplication, input-buffer parsing in the custom-duration keypad, and any new business logic introduced by the DI or interface-extraction work.
- Add Robolectric tests for Android-coupled components that don't require a real device: `TimerController` lifecycle (start/cancel/snooze state transitions), `AlarmReceiver` broadcast handling, `NapTimerService` foreground lifecycle, and `AlarmService` stop/dismiss behaviour.
- Add instrumentation tests (`androidTest/`) for the core end-to-end flows that require a real framework: setup → start nap, running → cancel, running → snooze, and alarm firing → dismiss. Use Hilt test rules (`HiltAndroidRule`) so DI is wired correctly in the test process.
- Establish a three-tier testing policy in the `code-review-quality` spec: pure logic → JVM unit test; Android-coupled but UI-free → Robolectric; full UI or system-service flows → instrumentation.

## Capabilities

### New Capabilities
- `android-compliance`: Defines the app-wide Android compliance standard (Compose, edge-to-edge, localization, manifest, notifications/alarms, launcher resources).
- `code-review-quality`: Defines the code-review quality gate (bug freedom, test coverage, safe Android lifecycle/Compose patterns).
- `dependency-injection`: Defines the Hilt DI setup — module structure, scoping conventions, and which types are injectable vs. provided directly.

### Modified Capabilities

## Impact

- Affected app code: any file flagged during the compliance or code-review passes — Compose screens, manifest, string resources, notification/alarm wiring, launcher icons, and tests; plus all files touched by the DI migration, interface extraction, and package restructure; plus all `@Composable` files for preview additions; plus new test files across all three tiers.
- Affected specs: new `android-compliance`, `code-review-quality`, and `dependency-injection` spec files.
- No intentional changes to user-visible behavior, timer logic, or persisted state.
