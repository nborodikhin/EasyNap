## 1. Volume Shortcut Behavior

- [x] 1.1 Update `AlarmActivity` volume-key handling so volume down snoozes using `SNOOZE_OPTIONS[0]` and volume up snoozes using `SNOOZE_OPTIONS[1]`.
- [x] 1.2 Preserve existing Escape/Stop behavior and existing notification Snooze behavior.
- [x] 1.3 Add or update tests proving volume down starts the first configured snooze duration and volume up starts the second configured snooze duration.

## 2. Alarm Snooze Button Labels

- [x] 2.1 Add localized string resources for the visible `Vol-` and `Vol+` shortcut labels.
- [x] 2.2 Render the `Vol-` label as a second line on the first alarm snooze button and `Vol+` as a second line on the second alarm snooze button.
- [x] 2.3 Keep the third alarm snooze button as a one-line button without reserving blank shortcut-label space.
- [x] 2.4 Add or update UI tests/previews as needed to cover the visible shortcut labels.

## 3. Accessibility

- [x] 3.1 Update alarm snooze button content descriptions so shortcut-labeled buttons expose the snooze duration and the localized volume shortcut.
- [x] 3.2 Keep the `SNOOZE` section header hidden from accessibility traversal.
- [x] 3.3 Add or update accessibility-focused tests for shortcut-aware snooze button descriptions.

## 4. Validation

- [x] 4.1 Run the relevant unit/UI test subset for alarm snooze behavior and timer helpers.
- [x] 4.2 Run OpenSpec validation for `unify-snooze-volume-shortcuts`.
- [x] 4.3 After implementation, discuss with the user whether the third snooze button should reserve second-line space for visual alignment.
