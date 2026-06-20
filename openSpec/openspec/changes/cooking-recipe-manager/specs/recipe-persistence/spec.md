## ADDED Requirements

### Requirement: Recipe serialization to JSON
The system SHALL serialize each recipe to a JSON object containing all recipe fields. Ingredients and instruction steps SHALL be stored as JSON arrays. Tags SHALL be stored as a JSON array of strings. Annotations SHALL be stored as a JSON array of objects, each with `id`, `text`, and `createdAt` fields.

#### Scenario: Recipe round-trips through JSON
- **WHEN** a recipe is serialized to JSON and then deserialized
- **THEN** the resulting recipe object SHALL be equal to the original (same fields and values)

#### Scenario: Missing optional fields on deserialization
- **WHEN** a JSON file lacks the `description`, `tags`, or `annotations` fields
- **THEN** the system SHALL deserialize successfully, defaulting to empty string, empty list, and empty list respectively

### Requirement: One JSON file per recipe
The system SHALL store each recipe as an individual file named `<recipe-uuid>.json` inside the configured recipes directory.

#### Scenario: Saving a new recipe creates a file
- **WHEN** a new recipe is saved
- **THEN** a file named `<uuid>.json` SHALL appear in the recipes directory

#### Scenario: Saving an existing recipe updates its file
- **WHEN** an existing recipe is modified and saved
- **THEN** the corresponding `<uuid>.json` file SHALL be overwritten with the updated content

#### Scenario: Deleting a recipe removes its file
- **WHEN** a recipe is deleted
- **THEN** its `<uuid>.json` file SHALL be removed from the recipes directory

### Requirement: Recipes directory configuration
The system SHALL use a configurable recipes directory. The default SHALL be `<user.home>/recipes/`. The directory SHALL be created on first use if it does not exist.

#### Scenario: Default directory is used when none is configured
- **WHEN** the application starts with no custom directory configured
- **THEN** recipes SHALL be loaded from and saved to `<user.home>/recipes/`

#### Scenario: Missing directory is created automatically
- **WHEN** the configured recipes directory does not exist
- **THEN** the system SHALL create it before any read or write operation

### Requirement: Malformed JSON files are skipped gracefully
The system SHALL catch JSON parse errors when loading individual recipe files, log a warning, and continue loading remaining recipes.

#### Scenario: One corrupt file does not block the rest
- **WHEN** one recipe file contains invalid JSON
- **THEN** that recipe SHALL be skipped and the rest of the library SHALL load normally
- **THEN** the UI SHALL display a warning indicating the skipped file
