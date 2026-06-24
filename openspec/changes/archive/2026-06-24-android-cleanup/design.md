## Context

EasyNap2 is a single-module Android app (~17 Kotlin source files) that lets users set a nap timer with alarm, snooze, and custom duration support. The codebase is functional but has accumulated several Android-specific compliance gaps and code-quality issues that were deferred from the polish change:

- `TimerController` is a Kotlin `object` with a manual `init(context)` call in `EasyNapApp.onCreate`, making it untestable and creating a hidden initialization order dependency.
- All source files live in the root `me.easynap` package, with only `ui/` and `theme/` sub-packages, making the flat list hard to navigate.
- No `@Preview` annotations exist on any composable, blocking design review in Android Studio.
- Notification icons use stock system drawables (`android.R.drawable.ic_lock_idle_alarm`) instead of the app's own icon.
- Several user-visible strings are hardcoded in Kotlin rather than `strings.xml`.
- JVM unit tests exist for `TimerHelpers` and `TimerPreferenceStore`, but no Robolectric or instrumentation tests exist.
- No DI framework is in place; Hilt is the agreed-upon choice for this change.

## Goals / Non-Goals

**Goals:**
- Introduce Hilt; convert `TimerController` to a `@Singleton` injectable class.
- Extract a `TimerStore` interface from `TimerPreferenceStore` so dependents aren't coupled to the DataStore implementation.
- Restructure the source tree into feature/layer sub-packages.
- Add `@Preview` annotations to all public and private composables.
- Fix concrete Android compliance gaps: notification icons, hardcoded strings, localization.
- Add pure JVM unit tests, Robolectric tests, and instrumentation tests as specified in the proposal.
- Write three capability specs: `android-compliance`, `code-review-quality`, `dependency-injection`.

**Non-Goals:**
- Changing any user-visible behavior, timer logic, or persisted data format.
- Migrating to a ViewModel architecture (TimerController stays the top-level state holder for now).
- Any UI redesign or feature addition.
- Enabling minification/R8 (already off in release).

## Decisions

### KSP over KAPT for annotation processing
The project uses Kotlin 2.3.20 and AGP 9, both of which require KSP. KAPT is unsupported on AGP 9. All Hilt annotation processing (`hilt-compiler`, `hilt-android-testing`) must use `ksp`/`kspAndroidTest` configurations, not `kapt`.

### Hilt over Koin
Hilt is the Android-team-recommended DI library with first-class Jetpack integration (ViewModel injection, `HiltAndroidTest`). Koin would be lighter-weight but requires manual test wiring. Since the proposal names Hilt explicitly, we adopt it without further evaluation.

`TimerController` becomes a class with `@Inject constructor(private val store: TimerStore, private val context: @ApplicationContext Context)`, annotated `@Singleton`, provided from a `@Module`-annotated `AppModule`. `EasyNapApp` gets `@HiltAndroidApp`, all Activities and Services get `@AndroidEntryPoint`.

### TimerStore interface — minimal surface
Only the methods and flows that `TimerController` actually calls become part of the interface: `history`, `napDurationMinutes`, `loadActiveTimer`, `getNapDurationMinutes`, `startTimer`, `clearActiveTimer`. `parseHistory`, `withSeedDurations`, `updatedHistory`, and the companion object helpers stay on `TimerPreferenceStore` as implementation details (they're already unit-tested there).

### Package layout — feature-first with shared data layer
```
me.easynap/
  EasyNapApp.kt
  alarm/
    AlarmActivity.kt
    AlarmReceiver.kt
    AlarmService.kt
  timer/
    TimerController.kt
    TimerHelpers.kt
    TimerState.kt
  data/
    TimerPreferenceStore.kt
    TimerStore.kt           ← new interface
  service/
    NapTimerService.kt
  ui/
    RunningScreen.kt
    SetupScreen.kt
  theme/
    Color.kt
    Theme.kt
    Type.kt
```
`EasyNapApp` stays at the root because Hilt requires it at the application class root (annotation processing).

### Hilt test runner for instrumented tests
Instrumented tests using `@HiltAndroidTest` require a custom `HiltTestRunner` class (extending `AndroidJUnitRunner`, delegating to `HiltTestApplication`) set as `testInstrumentationRunner` in `build.gradle.kts`. The Robolectric Hilt tests in `src/test/` use `@RunWith(RobolectricTestRunner::class)` instead.

### Three-tier testing — by Android dependency
| Tier | Tool | When |
|------|------|------|
| Pure logic | JVM (JUnit 4) | No Android dependency |
| Android-coupled, UI-free | Robolectric | Needs Context/framework but no real device |
| Full UI / system services | Instrumentation (`androidTest/`) | Needs real framework + DI via `HiltAndroidRule` |

### Composable Previews — representative states
Each composable gets at least one `@Preview`. Composables with meaningful state variants get multiple: `CustomDurationSheet` covers empty buffer and filled buffer states; `RunningScreen` covers normal and snooze states; `DurationGrid` uses the default seed history.

### SetupScreen edge-to-edge modifier order fix
`SetupScreen` currently applies `.verticalScroll()` *before* `.padding(innerPadding)` in the `Scaffold` content column. This is incorrect — with the modifier in that order, the scroll area spans the full screen height and content can scroll behind the navigation bar. The fix:
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)           // shrink content area first
        .consumeWindowInsets(innerPadding) // prevent double-padding in nested components
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
)
```
All three Scaffold screens also need `consumeWindowInsets(innerPadding)` after their `padding(innerPadding)` call (RunningScreen and AlarmActivity don't scroll but the pattern prevents double-padding from any nested inset-aware component).

### ModalBottomSheet scrim color
`CustomDurationSheet` sets `scrimColor = Color(0x8C080C0B)` which hardcodes the color outside the M3 color system. This should be replaced with `MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)` so it adapts to the theme.

### Hardcoded strings to extract
- `NapTimerService`: `"EasyNap"`, `"Remaining: "` (notification title/text)
- `AlarmService`: `"EasyNap"`, `"Time to wake up!"` (notification title/text)
- `AlarmActivity`: `"Time to wake up"`, `"Your $durationLabel-minute nap is done. Hope you feel refreshed."`, `"Stop"`, `"SNOOZE"`

These move to `strings.xml` where the format string `"Your %s-minute nap is done."` uses a `%s` argument.

### Notification icon
Replace `android.R.drawable.ic_lock_idle_alarm` with a reference to the app's own adaptive icon monochrome or a dedicated `ic_notification` vector drawable. The adaptive icon already exists (`@mipmap/ic_launcher`); a small 24dp vector will be added as `@drawable/ic_notification`.

## Risks / Trade-offs

- **Hilt annotation processing adds build time.** Acceptable; this is a dev tool concern not a release concern.
- **Package rename is a one-shot mechanical change.** No logic changes, but every file's package declaration and imports update. Risk of merge conflicts if any concurrent work touches source files — mitigated by doing the rename as a single focused commit.
- **Robolectric tests for `TimerController` require injecting a fake `TimerStore`.** With Hilt test rules (`@HiltAndroidTest` / `@UninstallModules` / `@BindValue`) this is straightforward, but adds dependency on `hilt-android-testing`.
- **`AlarmActivity` uses `TimerController.napDurationMinutes` directly.** After DI, activities annotated `@AndroidEntryPoint` can use `@Inject lateinit var timerController: TimerController` instead of the object reference; UI reads through the injected reference.

## Migration Plan

1. Add Hilt Gradle dependencies and `kapt`/`ksp` plugin.
2. Create `TimerStore` interface and make `TimerPreferenceStore` implement it.
3. Convert `TimerController` from `object` to class; add `AppModule` providing `TimerStore`.
4. Annotate `EasyNapApp`, activities, and services.
5. Rename/move files into the new package layout.
6. Extract hardcoded strings and swap notification icon.
7. Add `@Preview` annotations to all composables.
8. Add JVM unit tests, Robolectric tests, instrumentation tests.
9. Write capability specs.

Rollback: any step can be reverted independently; no schema/data migrations are involved.

## Open Questions

- None currently. DI library, interface scope, package layout, and test tiers are all decided.
