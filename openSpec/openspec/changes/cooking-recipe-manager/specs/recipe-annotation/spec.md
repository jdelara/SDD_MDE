## ADDED Requirements

### Requirement: View annotations on a recipe
The system SHALL display the annotations for the selected recipe in a dedicated annotations panel, showing each annotation's text and creation timestamp in chronological order.

#### Scenario: Annotations panel shows all annotations
- **WHEN** a recipe with existing annotations is selected
- **THEN** all annotations SHALL be listed in the annotations panel in order of creation

#### Scenario: Recipe with no annotations shows empty panel
- **WHEN** a recipe with no annotations is selected
- **THEN** the annotations panel SHALL display a "No annotations" placeholder message

### Requirement: Add annotation to a recipe
The system SHALL provide an "Add Annotation" action that appends a new free-text annotation to the currently selected recipe.

#### Scenario: Annotation is saved and displayed
- **WHEN** the user submits a non-empty annotation text
- **THEN** the annotation SHALL be appended to the recipe's annotations list, persisted to disk, and displayed in the annotations panel

#### Scenario: Empty annotation text is rejected
- **WHEN** the user submits an empty or whitespace-only annotation
- **THEN** the system SHALL prevent saving and display an inline error

### Requirement: Edit annotation
The system SHALL allow the user to edit the text of an existing annotation in place.

#### Scenario: Edited annotation text is persisted
- **WHEN** the user edits an annotation's text and confirms
- **THEN** the updated text SHALL be saved to disk and shown in the annotations panel

### Requirement: Delete annotation
The system SHALL allow the user to delete an annotation from a recipe after confirmation.

#### Scenario: Annotation is removed after confirmation
- **WHEN** the user deletes an annotation and confirms the dialog
- **THEN** the annotation SHALL be removed from the recipe's annotations list and persisted to disk

#### Scenario: Deletion cancelled leaves annotation intact
- **WHEN** the user initiates deletion but cancels the confirmation
- **THEN** no change SHALL occur
