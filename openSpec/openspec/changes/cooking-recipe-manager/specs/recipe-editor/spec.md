## ADDED Requirements

### Requirement: Recipe detail view
The system SHALL display the selected recipe's full content in a read-only detail panel, showing: name, description, tags, number of people, ingredients (as a bulleted list), and instructions (as a numbered list).

#### Scenario: Detail panel populates on selection
- **WHEN** the user selects a recipe in the list
- **THEN** all recipe fields SHALL be rendered in the detail panel

### Requirement: Create new recipe
The system SHALL provide a "New Recipe" action (button or menu item) that opens an empty editor form.

#### Scenario: New recipe form is empty
- **WHEN** the user activates "New Recipe"
- **THEN** an editor form SHALL open with all fields blank

#### Scenario: Saving a valid new recipe adds it to the list
- **WHEN** the user fills in all mandatory fields and saves
- **THEN** the recipe SHALL be persisted to disk and appear in the recipe list

#### Scenario: Saving with missing mandatory fields shows an error
- **WHEN** the user attempts to save a recipe with an empty name, no ingredients, or no instructions
- **THEN** the system SHALL highlight the invalid fields and prevent saving

### Requirement: Edit existing recipe
The system SHALL provide an "Edit" action on the detail panel that switches it into an editable form pre-populated with the current recipe's data.

#### Scenario: Edit form is pre-populated
- **WHEN** the user activates "Edit" on a recipe
- **THEN** all recipe fields SHALL be editable and pre-filled with current values

#### Scenario: Saving edits updates the recipe
- **WHEN** the user modifies fields and saves
- **THEN** the recipe's JSON file SHALL be updated and the list and detail panel SHALL reflect the changes

#### Scenario: Cancelling edit discards changes
- **WHEN** the user activates "Cancel" while editing
- **THEN** the original recipe data SHALL be restored in the detail panel without any file changes

### Requirement: Delete recipe
The system SHALL provide a "Delete" action that removes a recipe after user confirmation.

#### Scenario: Delete with confirmation removes the recipe
- **WHEN** the user activates "Delete" and confirms the dialog
- **THEN** the recipe file SHALL be deleted and the recipe SHALL be removed from the list

#### Scenario: Delete cancelled leaves the recipe intact
- **WHEN** the user activates "Delete" but cancels the confirmation dialog
- **THEN** no file changes SHALL occur
