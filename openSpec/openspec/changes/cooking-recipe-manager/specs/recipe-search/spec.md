## ADDED Requirements

### Requirement: Text search across recipe fields
The system SHALL provide a search bar that filters the recipe list in real time. The search SHALL be case-insensitive and SHALL match against the recipe name, description, ingredient strings, and tags.

#### Scenario: Search by name substring
- **WHEN** the user types "pasta" in the search bar
- **THEN** only recipes whose name contains "pasta" (case-insensitive) SHALL be shown

#### Scenario: Search by ingredient
- **WHEN** the user types "garlic" in the search bar
- **THEN** recipes that list "garlic" (or any string containing "garlic") as an ingredient SHALL be shown

#### Scenario: Search by tag text
- **WHEN** the user types "vegan" in the search bar
- **THEN** recipes tagged "vegan" SHALL be shown even if the word does not appear in the name or description

#### Scenario: Empty search shows all recipes
- **WHEN** the search bar is cleared
- **THEN** the full (unfiltered, or tag-filtered) recipe list SHALL be restored

### Requirement: Combined search and tag filter
The system SHALL apply both the text search and the active tag filter simultaneously (AND semantics).

#### Scenario: Search within a tag-filtered list
- **WHEN** the tag filter "vegetarian" is active and the user types "soup" in the search bar
- **THEN** only recipes that are tagged "vegetarian" AND contain "soup" in a searchable field SHALL appear

### Requirement: No-results feedback
The system SHALL display a message (e.g., "No recipes found") when the combined search and filter produces an empty result set.

#### Scenario: Empty result set is communicated
- **WHEN** the current search and tag filter match no recipes
- **THEN** the list panel SHALL display a "No recipes found" message instead of a blank list
