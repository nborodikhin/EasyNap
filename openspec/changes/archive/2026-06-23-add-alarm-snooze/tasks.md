## 1. Alarm UI

- [x] 1.1 Add snooze controls for `+1 min`, `+5 min`, and `+10 min` to the alarm screen.
- [x] 1.2 Preserve the existing Stop control and alarm presentation.
- [x] 1.3 Add alarm back handling so gesture/button back routes through the same behavior as Stop.

## 2. Snooze Behavior

- [x] 2.1 Add a snooze action path that stops current alarm sound and vibration.
- [x] 2.2 Start a new countdown for the selected snooze duration through the existing timer start/service path.
- [x] 2.3 Clear completed alarm state before or while starting the snooze countdown.
- [x] 2.4 Navigate to or reveal the running countdown screen after snooze.
- [x] 2.5 Ensure back navigation from the active alarm stops sound/vibration, finishes the alarm activity, and returns to idle setup state.

## 3. Validation

- [x] 3.1 Add tests or focused coverage for snooze duration mapping.
- [x] 3.2 Add tests or focused coverage for alarm back handling where feasible.
- [x] 3.3 Build the debug Android app.
- [ ] 3.4 Manually verify `+1`, `+5`, and `+10` snooze flows from the full-screen alarm.
- [ ] 3.5 Manually verify gesture/button back on the full-screen alarm behaves the same as Stop.
