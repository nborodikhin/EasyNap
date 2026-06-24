## 1. Add Hilt Dependencies

- [x] 1.1 Add `hilt-android` and `hilt-android-testing` to `libs.versions.toml` and `gradle/libs.versions.toml`
- [x] 1.2 Add KSP plugin and Hilt Gradle plugin to root `build.gradle.kts` and `app/build.gradle.kts` (KAPT is not supported on AGP 9; use `ksp`/`kspAndroidTest` configurations throughout)
- [x] 1.3 Add Hilt (`hilt-android`), Robolectric, and `hilt-android-testing` dependencies to `app/build.gradle.kts`; use `ksp` for `hilt-compiler` and `kspAndroidTest` for `hilt-android-testing` compiler
- [x] 1.4 Add Mockito (`mockito-core`, `mockito-kotlin`) to the `testImplementation` configuration in `app/build.gradle.kts`
- [x] 1.5 Create `app/src/androidTest/java/me/easynap/HiltTestRunner.kt` — a custom `AndroidJUnitRunner` subclass that calls `HiltTestApplication` for Hilt instrumented tests
- [x] 1.6 Set `testInstrumentationRunner = "me.easynap.HiltTestRunner"` in `app/build.gradle.kts` `defaultConfig`
- [x] 1.7 Verify the project builds cleanly with `./gradlew assembleDebug`

## 2. Extract TimerStore Interface and Update TimerPreferenceStore

- [x] 2.1 Create `app/src/main/java/me/easynap/data/TimerStore.kt` defining the `TimerStore` interface with `history`, `napDurationMinutes`, `loadActiveTimer`, `getNapDurationMinutes`, `startTimer`, and `clearActiveTimer`
- [x] 2.2 Move `TimerPreferenceStore` to `me.easynap.data` package (rename package declaration, update imports everywhere)
- [x] 2.3 Annotate `TimerPreferenceStore` with `@Singleton @Inject constructor` and make it implement `TimerStore`
- [x] 2.4 Update `TimerPreferenceStoreTest` package reference after the move

## 3. Convert TimerController to Hilt Singleton

- [x] 3.1 Move `TimerController` to `me.easynap.timer` package
- [x] 3.2 Convert `TimerController` from `object` to `class` with `@Singleton` annotation and `@Inject constructor(private val store: TimerStore, @ApplicationContext private val context: Context)`
- [x] 3.3 Remove the `init(context)` method; move initialization logic into `init {}` block (coroutine launch for state/history/napDuration collection)
- [x] 3.4 Create `app/src/main/java/me/easynap/AppModule.kt` with `@Module @InstallIn(SingletonComponent::class)` providing `TimerStore` via `@Binds` bound to `TimerPreferenceStore`, and `timerDataStore` via `@Provides`
- [x] 3.5 Verify `TimerController` no longer references `appContext` field (use injected `context`)

## 4. Annotate Application, Activities, and Services

- [x] 4.1 Add `@HiltAndroidApp` to `EasyNapApp` and remove `TimerController.init(this)` call
- [x] 4.2 Add `@AndroidEntryPoint` to `MainActivity` and inject `TimerController` via `@Inject`; replace all `TimerController.xxx` references with injected instance
- [x] 4.3 Add `@AndroidEntryPoint` to `AlarmActivity` and inject `TimerController`
- [x] 4.4 Add `@AndroidEntryPoint` to `NapTimerService` and inject `TimerPreferenceStore` (via `TimerStore`)
- [x] 4.5 Add `@AndroidEntryPoint` to `AlarmService` (no direct injection needed, but required for Hilt entry point)
- [x] 4.6 Add `@AndroidEntryPoint` to `AlarmReceiver` and inject `TimerController`

## 5. Restructure Source Packages

- [x] 5.1 Move `AlarmActivity`, `AlarmReceiver`, `AlarmService` to `me.easynap.alarm` package (update package declarations and all imports)
- [x] 5.2 Move `TimerState` and `TimerHelpers` to `me.easynap.timer` package (TimerController already moved in task 3.1)
- [x] 5.3 Move `NapTimerService` to `me.easynap.service` package
- [x] 5.4 Update `AndroidManifest.xml` component names to use fully-qualified class names (or verify they resolve from the renamed packages — usually `.ClassName` shorthand relies on `applicationId`)
- [x] 5.5 Verify `./gradlew assembleDebug` and `./gradlew test` pass after the restructure

## 6. Android Compliance Fixes

- [x] 6.1 Create `app/src/main/res/drawable/ic_notification.xml` — a 24dp monochrome vector drawable usable as notification small icon
- [x] 6.2 Replace `android.R.drawable.ic_lock_idle_alarm` with `R.drawable.ic_notification` in `NapTimerService` and `AlarmService`
- [x] 6.3 Extract hardcoded notification strings in `NapTimerService` (`"EasyNap"`, `"Remaining: "`) to `strings.xml`
- [x] 6.4 Extract hardcoded strings in `AlarmService` (`"EasyNap"`, `"Time to wake up!"`) to `strings.xml`
- [x] 6.5 Extract hardcoded strings in `AlarmActivity` (`"Time to wake up"`, the body copy, `"Stop"`, `"SNOOZE"`) to `strings.xml`; use format string for the body that takes `%s` for the duration label
- [x] 6.6 Fix `SetupScreen` Scaffold content modifier order: change `verticalScroll` to come AFTER `padding(innerPadding)` and `consumeWindowInsets(innerPadding)` so content cannot scroll behind the navigation bar
- [x] 6.7 Add `consumeWindowInsets(innerPadding)` to `RunningScreen` and `AlarmActivity` Scaffold content to prevent nested components from double-applying insets
- [x] 6.8 Replace hardcoded `scrimColor = Color(0x8C080C0B)` in `CustomDurationSheet` with `MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)`
- [x] 6.9 Make `CustomDurationSheet` adaptive for short landscape windows so the Start button remains visible
- [x] 6.10 Preserve `CustomDurationSheet` visibility and input across orientation changes
- [x] 6.11 Fix lint errors for API 26 alarm lock-screen handling and non-exported internal broadcasts
- [x] 6.12 Start `NapTimerService` in the foreground synchronously before asynchronous timer-store lookup
- [x] 6.13 Make RunningScreen compact-landscape aware so the countdown ring and Cancel button do not overlap
- [x] 6.14 Make AlarmActivity compact-landscape aware so Stop and Snooze controls remain visible

## 7. Add Composable @Preview Annotations

- [x] 7.1 Add `@Preview` for `SetupScreen` (default history state)
- [x] 7.2 Add `@Preview` for `DurationGrid` with seed durations
- [x] 7.3 Add `@Preview` for `DurationTile` — whole-minute and fractional variants
- [x] 7.4 Add `@Preview` for `CustomTile`
- [x] 7.5 Add `@Preview` for `CustomDurationSheet` — empty buffer state and filled buffer state (e.g. `"12:30"`)
- [x] 7.6 Add `@Preview` for `ValueDisplay` — empty and filled
- [x] 7.7 Add `@Preview` for `NumericKeypad` — colon enabled and colon disabled
- [x] 7.8 Add `@Preview` for `KeypadButton` — normal, special, and disabled variants
- [x] 7.9 Add `@Preview` for `RunningScreen` — normal running state and snooze state

## 8. JVM Unit Tests for Pure Logic

- [x] 8.1 Add tests for `appendToBuffer` in `TimerHelpersTest`: digit within limit, digit at limit, colon insert, colon rejected when present, backspace removes, backspace on empty, seconds field capped at two digits
- [x] 8.2 Verify existing `TimerHelpersTest` and `TimerPreferenceStoreTest` still pass after package moves (update import paths if needed)
- [x] 8.3 Verify coroutine exception handling rethrows `CancellationException`; add a regression test for `TimerPreferenceStore` DataStore flow cancellation

## 9. Robolectric Tests

- [x] 9.1 Add Robolectric dependency to `app/build.gradle.kts` (test scope); add `robolectric.properties` under `app/src/test/resources/` with `sdk=34` to target the correct SDK in shadow code
- [x] 9.2 Create `TimerControllerTest` with `@RunWith(RobolectricTestRunner::class)` using a fake `TimerStore`: test idle→running on `start()`, running→idle on `cancel()`, running with `isSnooze=true` on `startSnooze()`
- [x] 9.3 Create `AlarmReceiverTest` with `@RunWith(RobolectricTestRunner::class)` and `@HiltAndroidTest`: verify `AlarmService` is started when broadcast received
- [x] 9.4 Create `NapTimerServiceTest` with `@RunWith(RobolectricTestRunner::class)` and `@HiltAndroidTest`: verify service consults the timer store on start, and posts no notification when no active timer

## 10. Instrumentation Tests

- [x] 10.1 Create `EasyNapE2ETest` in `androidTest/` with `HiltAndroidRule`: test setup → start nap (duration tile tap → RunningScreen appears)
- [x] 10.2 Add running → cancel test case: RunningScreen cancel button → SetupScreen appears
- [x] 10.3 Add snooze test case: alarm state → snooze button tap → new running timer with `isSnooze = true`
- [x] 10.4 Add alarm dismiss test case: alarm state → stop button → AlarmService stops and SetupScreen is shown
- [x] 10.5 Make E2E tests deterministic by granting notification permission, dismissing keyguard, using an in-memory `TimerStore`, and waiting for UI state
