## Why

The app currently stores timer state and duration history in `SharedPreferences` and reads it synchronously from UI/service paths such as `loadHistory()` and `getNapDurationMinutes()`. Android recommends DataStore over SharedPreferences for this pattern because DataStore provides asynchronous, coroutine-friendly access and avoids disk reads on the main thread.

## What Changes

- Add DataStore Preferences as the app's persistence layer for timer end time, nap duration, and recent duration history.
- Replace synchronous `SharedPreferences` reads in UI-facing APIs with Flow/suspend-based DataStore access.
- Migrate existing SharedPreferences values into DataStore so existing users keep active timer and history state where possible.
- Update services and activities to consume cached state, Flow state, or suspend reads rather than direct main-thread disk reads.
- Preserve all current user-facing timer behavior, including duration padding, recent duration rules, foreground notification behavior, and alarm behavior.

## Capabilities

### New Capabilities
- `timer-persistence`: Defines how Easy Nap persists timer state and duration history without synchronous main-thread disk reads.

### Modified Capabilities
- `timer-setup`: Duration history loading becomes asynchronous/DataStore-backed while preserving displayed duration behavior.
- `countdown-timer`: Active timer restoration and foreground service state loading become asynchronous/DataStore-backed while preserving timer behavior.
- `nap-alarm`: Alarm duration display reads persisted nap duration through DataStore-backed state rather than synchronous SharedPreferences access.

## Impact

- Affected app code: Gradle dependencies, timer persistence/controller code, setup UI history collection, countdown service startup, alarm UI duration lookup, and tests.
- Migration: existing `easynap_prefs` SharedPreferences values for `end_at_millis`, `nap_duration_minutes`, and `duration_history` should be migrated to DataStore.
- No intended changes to user-visible behavior.
