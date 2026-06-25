## 1. Prompt Animation

- [x] 1.1 Update `EnableNotificationsPrompt` to preserve immediate initial visibility state.
- [x] 1.2 Add fade-in behavior when alarm notifications become unavailable while the setup screen is already visible.
- [x] 1.3 Add fade-out behavior when alarm notifications become available while the setup screen is already visible.
- [x] 1.4 Keep bottom-end alignment, prompt styling, and settings click behavior unchanged.

## 2. Tests

- [x] 2.1 Update Robolectric Compose tests to assert initial unavailable state is visible immediately.
- [x] 2.2 Add Robolectric Compose coverage for prompt fade-in after visible state change.
- [x] 2.3 Add Robolectric Compose coverage for prompt fade-out after visible state change.
- [x] 2.4 Confirm connected tests remain focused on device-only flows and do not duplicate the prompt animation behavior tests.

## 3. Verification

- [x] 3.1 Run focused `EnableNotificationsPromptTest`.
- [x] 3.2 Run `testDebugUnitTest`.
- [x] 3.3 Run `connectedDebugAndroidTest` if a device is available.
- [x] 3.4 Validate the OpenSpec change.
