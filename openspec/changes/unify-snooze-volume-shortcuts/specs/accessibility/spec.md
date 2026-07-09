## MODIFIED Requirements

### Requirement: Alarm snooze buttons have descriptive labels
Snooze option buttons on the alarm screen SHALL expose a content description of the form "Snooze N minute(s)" or "Snooze N second(s)" so their purpose is clear to screen readers. When a snooze button has a visible hardware shortcut label, its accessible description SHALL also communicate the corresponding localized shortcut. The visual duration label SHALL use the same treatment as main-screen duration tiles: a prominent number with `min` or `sec` on the second line, without a leading `+`. Localized `Vol-` and `Vol+` shortcut labels SHALL be shown below the first and second snooze buttons respectively. The "SNOOZE" section header is a decorative grouping element and SHALL be hidden from accessibility traversal.

#### Scenario: Snooze button is announced with action and duration
- **WHEN** a screen-reader user focuses a snooze button without a hardware shortcut label
- **THEN** it is announced as "Snooze N minute(s), button" or "Snooze N second(s), button", not as separate visual label fragments

#### Scenario: Snooze shortcut button announces shortcut
- **WHEN** a screen-reader user focuses a snooze button with a visible volume shortcut label
- **THEN** it is announced with the snooze action, duration, and localized volume shortcut

#### Scenario: SNOOZE header is skipped by TalkBack
- **WHEN** a screen-reader user traverses the alarm screen
- **THEN** the "SNOOZE" label is not announced as a separate item
