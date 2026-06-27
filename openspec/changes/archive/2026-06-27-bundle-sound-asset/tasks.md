## 1. Asset

- [x] 1.1 Download `Helium.ogg` from AOSP (`frameworks/base/data/sounds/alarms/ogg/Helium.ogg`) and place it at `app/src/main/res/raw/helium.ogg`
- [x] 1.2 Add an Apache 2.0 `LICENSE` file to the project root (licenses the project under Apache 2.0, which also satisfies attribution for all bundled AOSP assets including `Helium.ogg`)

## 2. AlarmService

- [x] 2.1 Replace the `listOfNotNull` + loop in `startAudioFadeIn()` with a try/catch: try the `TYPE_ALARM` URI first, then on exception release and try `openRawResourceFd(R.raw.helium)` as fallback
- [x] 2.2 Remove the now-unused `TYPE_NOTIFICATION` and `TYPE_RINGTONE` candidates

## 3. Verification

- [x] 3.1 Update the `AlarmServiceTest` scenario for "Audio unavailable" to reflect the two-step fallback (system URI fails → bundled plays; bundled also fails → vibration only)
- [x] 3.2 Run the app on a ChromeOS device or emulator and confirm the alarm sounds with `Helium.ogg`
