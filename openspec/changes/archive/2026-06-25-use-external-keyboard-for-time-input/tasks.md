## 1. Keyboard Input Mapping

- [x] 1.1 Add custom duration sheet key handling for top-row and numpad digit keys `0` through `9`.
- [x] 1.2 Map hardware colon and dot keys to the existing colon buffer action.
- [x] 1.3 Map hardware backspace and delete keys to the existing backspace buffer action.
- [x] 1.4 Ensure unsupported hardware keys do not change the custom duration buffer.

## 2. Start Action

- [x] 2.1 Map hardware Enter and numpad Enter to the existing custom duration start action.
- [x] 2.2 Ensure Enter starts a nap only when the current duration parses and is in range.
- [x] 2.3 Ensure invalid Enter input leaves the sheet open and preserves existing validation feedback.

## 3. Focus And Sheet Integration

- [x] 3.1 Make the custom duration sheet content eligible to receive key events while the sheet is visible.
- [x] 3.2 Request focus for the sheet's keyboard handler when the bottom sheet opens.
- [x] 3.3 Preserve touch keypad behavior and adaptive compact-landscape layout.

## 4. Validation

- [x] 4.1 Add unit coverage for the key-to-buffer mapping, including digit, dot-to-colon, colon, backspace/delete, unsupported keys, and Enter.
- [x] 4.2 Add UI or instrumentation coverage for entering a custom duration with hardware keys and starting with Enter.
- [x] 4.3 Run the relevant JVM and Android test targets for timer setup behavior.
