## ADDED Requirements

### Requirement: Project README describes the app
The repository SHALL contain a root `README.md` that describes EasyNap, including its purpose, the project motto "The nap, at its simplest", its core features (custom and quick-start durations, the foreground countdown notification, and the full-screen lock-screen alarm), and how to build and run it.

#### Scenario: README is present and descriptive
- **WHEN** the repository is viewed
- **THEN** a root `README.md` is present that states the app's purpose, displays the motto "The nap, at its simplest", and lists the core features

#### Scenario: README explains how to build and run
- **WHEN** a developer reads the README
- **THEN** it provides the steps to build and run the app on a device or emulator

### Requirement: README is kept up to date
The README SHALL be kept in sync with the app's behavior. Any change that adds, removes, or alters a user-facing capability SHALL update the README within that same change.

#### Scenario: A behavior change updates the README
- **WHEN** a change alters a user-facing capability (for example the quick-start range, the alarm duration, or the start-padding behavior)
- **THEN** the README is updated within that same change to reflect the new behavior

#### Scenario: README matches current behavior
- **WHEN** the README is read at any point
- **THEN** its described features and usage match the app's current behavior
