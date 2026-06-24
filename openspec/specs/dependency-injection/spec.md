# Dependency Injection

## Purpose

Defines dependency injection, persistence abstraction, and package organization requirements for the app architecture.

## Requirements

### Requirement: Hilt is the DI framework
The app SHALL use Hilt (via `hilt-android` and `hilt-android-testing`) as its dependency injection framework. `EasyNapApp` SHALL be annotated `@HiltAndroidApp`. All Activities and Services SHALL be annotated `@AndroidEntryPoint`.

#### Scenario: Application class is annotated
- **WHEN** the app process starts
- **THEN** `EasyNapApp` is annotated `@HiltAndroidApp` and Hilt's component hierarchy is initialized

#### Scenario: MainActivity is an entry point
- **WHEN** `MainActivity` is launched
- **THEN** it is annotated `@AndroidEntryPoint` and its `@Inject` fields are populated by Hilt

#### Scenario: AlarmActivity is an entry point
- **WHEN** `AlarmActivity` is started
- **THEN** it is annotated `@AndroidEntryPoint` and its `@Inject` fields are populated by Hilt

#### Scenario: NapTimerService is an entry point
- **WHEN** `NapTimerService` is started
- **THEN** it is annotated `@AndroidEntryPoint` and its `@Inject` fields are populated by Hilt

#### Scenario: AlarmService is an entry point
- **WHEN** `AlarmService` is started
- **THEN** it is annotated `@AndroidEntryPoint` and its `@Inject` fields are populated by Hilt

### Requirement: TimerController is a Hilt singleton
`TimerController` SHALL be a class (not a Kotlin `object`) annotated `@Singleton`. Its dependencies (`TimerStore` and `@ApplicationContext Context`) SHALL be injected through the constructor. There SHALL be no static `init(context)` method.

#### Scenario: TimerController is injected where needed
- **WHEN** any Activity or Service declares `@Inject lateinit var timerController: TimerController`
- **THEN** Hilt provides the same singleton instance across all injection sites

#### Scenario: TimerController has no manual init call
- **WHEN** the app is initialized
- **THEN** `EasyNapApp.onCreate` does not call `TimerController.init(this)` or any equivalent setup method

#### Scenario: TimerController initializes its state from the store on construction
- **WHEN** the Hilt component creates the `TimerController` singleton
- **THEN** `TimerController` reads the persisted timer state from `TimerStore` and sets its initial `StateFlow` value

### Requirement: TimerStore interface decouples dependents from DataStore
A `TimerStore` interface SHALL be defined in `me.easynap.data`. It SHALL expose exactly the methods and flows that `TimerController` needs. `TimerPreferenceStore` SHALL implement `TimerStore`. All injection sites SHALL depend on `TimerStore`, not `TimerPreferenceStore`.

#### Scenario: TimerController depends on TimerStore abstraction
- **WHEN** `TimerController` is constructed
- **THEN** its constructor parameter is typed `TimerStore`, not `TimerPreferenceStore`

#### Scenario: AppModule binds TimerPreferenceStore to TimerStore
- **WHEN** Hilt resolves a `TimerStore` dependency
- **THEN** it provides the singleton `TimerPreferenceStore` instance via an `@Binds` binding in `AppModule`

#### Scenario: Fake TimerStore is injectable in tests
- **WHEN** a Hilt test uninstalls `AppModule` and binds a fake `TimerStore` via `@BindValue`
- **THEN** the test's `TimerController` instance uses the fake store with no changes to production code

### Requirement: Source files are organized into feature/layer sub-packages
All Kotlin source files SHALL be organized under `me.easynap` into sub-packages according to the layout defined in the design document: `alarm/`, `timer/`, `data/`, `service/`, `ui/`, `theme/`. `EasyNapApp` SHALL remain at the root package because `@HiltAndroidApp` requires the application class to be in a package that the annotation processor can locate.

#### Scenario: Alarm classes are in alarm sub-package
- **WHEN** the source tree is examined
- **THEN** `AlarmActivity`, `AlarmReceiver`, and `AlarmService` declare `package me.easynap.alarm`

#### Scenario: Timer core classes are in timer sub-package
- **WHEN** the source tree is examined
- **THEN** `TimerController`, `TimerHelpers`, and `TimerState` declare `package me.easynap.timer`

#### Scenario: Data classes are in data sub-package
- **WHEN** the source tree is examined
- **THEN** `TimerPreferenceStore` and `TimerStore` declare `package me.easynap.data`

#### Scenario: Service is in service sub-package
- **WHEN** the source tree is examined
- **THEN** `NapTimerService` declares `package me.easynap.service`
