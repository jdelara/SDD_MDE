## ADDED Requirements

### Requirement: Extensible tag vocabulary
The system SHALL NOT enforce a fixed set of tags. Any non-empty string SHALL be a valid tag. Tags are created implicitly when assigned to a recipe and are discovered by aggregating tags across all loaded recipes.

#### Scenario: New tag created by typing it in the editor
- **WHEN** the user types a new tag string in the recipe editor and saves the recipe
- **THEN** the tag SHALL appear in the global tag list without any additional configuration step

#### Scenario: Deleting the last recipe with a tag removes that tag from the global list
- **WHEN** the last recipe carrying a given tag is deleted
- **THEN** that tag SHALL no longer appear in the tag filter panel

### Requirement: Tag assignment on a recipe
The system SHALL allow assigning zero or more tags to a recipe via the recipe editor. Tags are stored as a list of strings on the recipe.

#### Scenario: Multiple tags can be assigned to a recipe
- **WHEN** the user assigns tags "vegan" and "quick" to a recipe and saves
- **THEN** both tags SHALL appear on the recipe's list entry and detail panel

#### Scenario: Tag can be removed from a recipe
- **WHEN** the user removes a tag from a recipe in the editor and saves
- **THEN** that tag SHALL no longer appear on the recipe

### Requirement: Case-insensitive tag matching
The system SHALL treat tags as case-insensitive for filtering purposes (e.g., "Vegetarian" and "vegetarian" match the same tag filter). Tags SHALL be stored in lowercase.

#### Scenario: Tag stored in lowercase
- **WHEN** a user enters "VEGAN" as a tag
- **THEN** it SHALL be stored and displayed as "vegan"

#### Scenario: Tag filter is case-insensitive
- **WHEN** the tag filter "vegetarian" is selected
- **THEN** recipes tagged "Vegetarian" or "VEGETARIAN" SHALL also match
