# Countdown Timer

## Purpose

Manages the active countdown from timer start through completion, including the foreground service, live notification, and reliable on-time firing even when the device is idle.

## Requirements

### Requirement: Start a countdown
When a timer is started, the system SHALL record a completion time at the requested number of minutes in the future and begin tracking the remaining time toward it. When the requested duration is 1 minute or longer, the system SHALL add 5 seconds of padding to the completion time, so that a freshly started whole‑minute timer does not immediately tick down below the entered value (e.g. entering 1 minute does not jump straight to 0:59). Durations shorter than 1 minute SHALL NOT be padded.

#### Scenario: Countdown begins with padding for a one‑minute‑or‑longer duration
- **WHEN** a timer is started with a duration D of 1 minute or more
- **THEN** the system records a completion timestamp D minutes plus 5 seconds ahead and begins counting down toward it

#### Scenario: Sub‑minute duration is not padded
- **WHEN** a timer is started with a duration shorter than 1 minute
- **THEN** the system records a completion timestamp exactly that duration ahead, with no added padding

#### Scenario: Only one active timer
- **WHEN** a timer is already active
- **THEN** the system does not present controls to start a second concurrent timer; the existing timer remains the single source of truth

### Requirement: Foreground service with live countdown notification
While a timer is active, the system SHALL run a foreground service that maintains an ongoing (non‑dismissable) notification displaying the remaining time, updated at least once per second.

#### Scenario: Notification shows remaining time
- **WHEN** a timer is active
- **THEN** a foreground service is running and its notification displays the remaining time (e.g. mm:ss), refreshed at least every second

#### Scenario: Timer survives backgrounding and app close
- **WHEN** the app is backgrounded or its task is closed while a timer is active
- **THEN** the foreground service and its countdown notification continue running, and the timer keeps counting down

#### Scenario: Tapping the notification opens the app
- **WHEN** the user taps the countdown notification
- **THEN** the app opens and shows the running‑timer screen

### Requirement: Running‑timer screen
While a timer is active and the app is in the foreground, the system SHALL show a running‑timer screen displaying the live remaining time and a "Cancel" button.

#### Scenario: Open the app while a timer is active
- **WHEN** the user opens or reopens the app while a timer is active
- **THEN** the app lands directly on the running‑timer screen showing the live remaining time and a Cancel button

#### Scenario: Remaining time updates live
- **WHEN** the running‑timer screen is visible
- **THEN** the displayed remaining time decreases in real time toward zero

### Requirement: Cancel a running timer
The system SHALL allow the user to cancel an active timer, which stops the countdown and clears all associated system state.

#### Scenario: Cancel from the running screen
- **WHEN** the user taps "Cancel" on the running‑timer screen
- **THEN** the countdown stops, the foreground service and its notification are removed, the scheduled completion is cancelled, and the app returns to the setup screen

### Requirement: Reliable on‑time completion
The system SHALL schedule the timer's completion so that it fires at the recorded completion time even when the device is idle (Doze), the screen is off, or the app is not running.

#### Scenario: Fires while device is idle
- **WHEN** the recorded completion time is reached while the device is in Doze or the screen is off
- **THEN** the completion fires on time and triggers the nap alarm

#### Scenario: Completion ends the countdown service
- **WHEN** the timer completes
- **THEN** the countdown foreground service stops maintaining the countdown notification and the nap alarm takes over
