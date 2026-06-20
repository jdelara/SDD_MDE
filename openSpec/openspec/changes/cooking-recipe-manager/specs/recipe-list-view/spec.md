## ADDED Requirements

### Requirement: Recipe list panel
The system SHALL display a scrollable list of all loaded recipes in a left-side panel. Each list entry SHALL show the recipe name and its tags.

#### Scenario: All recipes appear on startup
- **WHEN** the application finishes loading
- **THEN** all recipes from the recipes directory SHALL be displayed in the list panel

#### Scenario: Selecting a recipe shows its details
- **WHEN** the user clicks a recipe in the list
- **THEN** the recipe's full details SHALL be displayed in the detail/editor panel on the right

### Requirement: Tag-based grouping and filtering
The system SHALL provide a tag filter panel listing all known tags. Selecting one or more tags SHALL filter the recipe list to show only recipes that have ALL selected tags (AND semantics). Selecting no tag SHALL show all recipes.

#### Scenario: No tag selected shows all recipes
- **WHEN** no tag filter is active
- **THEN** all recipes SHALL appear in the recipe list

#### Scenario: Single tag selected filters the list
- **WHEN** the user selects the tag "vegetarian"
- **THEN** only recipes tagged "vegetarian" SHALL appear in the recipe list

#### Scenario: Multiple tags narrow the list
- **WHEN** the user selects tags "vegetarian" and "quick"
- **THEN** only recipes tagged with both "vegetarian" AND "quick" SHALL appear

#### Scenario: Tag list updates when a recipe is saved with a new tag
- **WHEN** a recipe is saved with a previously unseen tag
- **THEN** that tag SHALL appear in the tag filter panel

### Requirement: Recipe count indicator
The system SHALL display the number of recipes currently shown in the list (e.g., "12 of 34 recipes").

#### Scenario: Count reflects active filters
- **WHEN** a tag filter or search is active
- **THEN** the count SHALL reflect the number of matching recipes, not the total
