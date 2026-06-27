# Duration Deletion

## Purpose

Defines how the user removes a saved duration from the setup screen grid via a long-press → badge-tap flow with snackbar undo.

## Requirements

### Requirement: Long-press enters pending-delete mode
Long-pressing a duration tile SHALL enter a pending-delete mode for that tile. The long-pressed tile SHALL display a red outline and an × badge. All other tiles (duration and Custom) SHALL be visually dimmed and SHALL NOT perform their primary action when tapped. Only one tile may be in pending-delete mode at a time.

#### Scenario: Long-press activates pending-delete on the tile
- **WHEN** the user long-presses a duration tile
- **THEN** that tile gains a red outline and displays an × badge, and all other tiles are dimmed

#### Scenario: Tapping the pending-delete tile itself does nothing
- **WHEN** the user is in pending-delete mode and taps the pending-delete tile (not the × badge)
- **THEN** pending-delete mode remains active and no duration is removed

#### Scenario: Custom tile is never in pending-delete mode
- **WHEN** the user long-presses the Custom tile
- **THEN** no pending-delete mode is entered and normal behavior applies

### Requirement: Tapping × confirms deletion
Tapping the × badge on the pending-delete tile SHALL immediately remove that duration from history and return the grid to normal mode.

#### Scenario: Deletion removes tile from grid
- **WHEN** the user taps the × badge on the pending-delete tile
- **THEN** the duration is removed from history, the grid reflows without that tile, and all tiles return to normal interactive state

### Requirement: Tapping outside cancels pending-delete mode
Any tap on a non-pending-delete tile, on empty space within the grid, or on any area of the screen outside the grid while in pending-delete mode SHALL cancel the mode and return the grid to normal state without modifying history.

#### Scenario: Tapping another tile cancels
- **WHEN** the user is in pending-delete mode and taps a different duration tile
- **THEN** pending-delete mode is cancelled, no duration is removed, and the tapped tile does NOT start a timer

#### Scenario: Tapping empty space cancels
- **WHEN** the user is in pending-delete mode and taps empty space anywhere on the screen
- **THEN** pending-delete mode is cancelled and no duration is removed

### Requirement: Deletion snackbar with undo
After a duration is deleted, a snackbar SHALL appear displaying the deleted duration and an "Undo" action. The snackbar SHALL auto-dismiss after a short delay.

#### Scenario: Snackbar appears after deletion
- **WHEN** the user confirms deletion by tapping ×
- **THEN** a snackbar is shown with a message identifying the deleted duration and an "Undo" button

#### Scenario: Snackbar auto-dismisses
- **WHEN** the deletion snackbar is visible and the user takes no action
- **THEN** the snackbar dismisses automatically after a short delay

### Requirement: Undo restores duration at original position
Tapping "Undo" in the deletion snackbar SHALL re-insert the deleted duration at the position it occupied before deletion, without starting a timer.

#### Scenario: Undo restores item at original position
- **WHEN** the deletion snackbar is visible and the user taps "Undo"
- **THEN** the deleted duration is re-inserted into history at its original position and no timer is started

#### Scenario: Undo survives device rotation
- **WHEN** a duration has been deleted, the snackbar is visible, and the device is rotated
- **THEN** the snackbar reappears after rotation and tapping "Undo" still restores the duration at its original position
