## Context

`TimerController` currently owns `SharedPreferences` and exposes synchronous methods such as `loadHistory()` and `getNapDurationMinutes()`. Compose setup code calls `loadHistory()` from `remember`, alarm UI calls `getNapDurationMinutes()`, and `NapTimerService` reads `end_at_millis` directly from SharedPreferences during `onStartCommand`. These reads are small, but they are disk-backed synchronous access on main-thread-adjacent paths.

## Goals / Non-Goals

**Goals:**
- Move timer persistence from SharedPreferences to Jetpack DataStore Preferences.
- Expose app state as coroutine-friendly `Flow` and/or suspend APIs.
- Migrate existing SharedPreferences values into DataStore.
- Preserve existing timer, history, alarm, and notification behavior.

**Non-Goals:**
- Do not change countdown padding semantics.
- Do not change recent duration ordering/deduplication rules.
- Do not change alarm auto-dismiss, sound/vibration sequence, or snooze behavior.
- Do not introduce a database or Proto DataStore unless Preferences DataStore proves insufficient.

## Decisions

- Use Preferences DataStore rather than Proto DataStore. The persisted state is a small set of primitive values and a simple duration-history string/list, so schema overhead is not justified.
- Centralize persistence behind a timer preference store abstraction. UI, services, and alarm screens should not access DataStore keys directly.
- Expose duration history as a `Flow<List<Float>>` or equivalent state stream for Compose collection. This avoids blocking the setup screen while still rendering seeded defaults when no history exists.
- Expose nap duration and active timer restoration through suspend or state-backed APIs. Callers that need one value should use coroutine scope/lifecycle-aware collection rather than direct synchronous reads.
- Use DataStore's SharedPreferences migration support or an explicit one-time migration to carry over existing keys.

## Risks / Trade-offs

- Service startup needs timer state quickly. Mitigation: start foreground service work in a coroutine and stop the service if the loaded DataStore state is missing or expired.
- Refactoring sync APIs can cascade into UI and service call sites. Mitigation: introduce the persistence abstraction first, then migrate call sites one by one.
- Existing tests may assume synchronous state reads. Mitigation: add coroutine test coverage and use a temporary DataStore/test scope.
