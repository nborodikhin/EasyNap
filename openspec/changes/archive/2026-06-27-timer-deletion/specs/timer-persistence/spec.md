## ADDED Requirements

### Requirement: Duration removal from history
The system SHALL support removing a specific duration value from the recent history list. The removal SHALL be persisted through DataStore. If the duration is not present in the list, the operation SHALL be a no-op.

#### Scenario: Removing a present duration persists the change
- **WHEN** a duration that exists in the recent history list is removed
- **THEN** the updated history without that duration is persisted through DataStore and the history flow emits the new list

#### Scenario: Removing an absent duration is a no-op
- **WHEN** a duration that does not exist in the recent history list is removed
- **THEN** the history list is unchanged and no DataStore write occurs

### Requirement: Position-aware history insertion
The system SHALL support inserting a duration into the recent history list at a specified zero-based position. Before inserting, any existing occurrence of the same duration SHALL be removed (deduplication). The insertion position SHALL be clamped to the current list size if it exceeds it. The resulting list SHALL be capped at 5 entries. The insertion SHALL be persisted through DataStore.

#### Scenario: Inserting at position 0 prepends to history
- **WHEN** a duration is inserted at position 0
- **THEN** it appears as the first entry in the persisted history list

#### Scenario: Inserting deduplicates existing occurrence
- **WHEN** a duration that already exists in the history is inserted at a given position
- **THEN** the existing occurrence is removed first and the duration is inserted at the specified position

#### Scenario: Position is clamped when list is shorter
- **WHEN** a duration is inserted at a position greater than the current list size
- **THEN** the duration is appended at the end of the list

#### Scenario: List is capped at 5 after insertion
- **WHEN** a duration is inserted into a history list that already contains 5 entries
- **THEN** the oldest entry (last in the list) is dropped and the new duration is inserted at the specified position
