## MODIFIED Requirements

### Requirement: Long-press enters pending-delete mode
Long-pressing a duration tile SHALL enter a pending-delete mode for that tile. The long-pressed tile SHALL display a red outline and an × badge. All other tiles (duration and Custom) SHALL be visually dimmed and SHALL NOT perform their primary action when tapped. Only one tile may be in pending-delete mode at a time.

The pending-delete mode SHALL track the duration as an integer number of seconds, matching the type used throughout duration history state.

#### Scenario: Long-press activates pending-delete on the tile
- **WHEN** the user long-presses a duration tile
- **THEN** that tile gains a red outline and displays an × badge, and all other tiles are dimmed

#### Scenario: Tapping the pending-delete tile itself does nothing
- **WHEN** the user is in pending-delete mode and taps the pending-delete tile (not the × badge)
- **THEN** pending-delete mode remains active and no duration is removed

#### Scenario: Custom tile is never in pending-delete mode
- **WHEN** the user long-presses the Custom tile
- **THEN** no pending-delete mode is entered and normal behavior applies

### Requirement: Undo restores duration at original position
Tapping "Undo" in the deletion snackbar SHALL re-insert the deleted duration at the position it occupied before deletion, without starting a timer. The undo operation SHALL compare and restore integer-second duration values.

#### Scenario: Undo restores item at original position
- **WHEN** the deletion snackbar is visible and the user taps "Undo"
- **THEN** the deleted duration (as integer seconds) is re-inserted into history at its original position and no timer is started

#### Scenario: Undo survives device rotation
- **WHEN** a duration has been deleted, the snackbar is visible, and the device is rotated
- **THEN** the snackbar reappears after rotation and tapping "Undo" still restores the duration at its original position
