## ADDED Requirements

### Requirement: Recipe data structure
The system SHALL represent a recipe as an object with the following fields: a unique identifier (UUID string), name (non-empty string), description (string), ingredients (ordered list of strings), number of people served (positive integer), step-by-step instructions (ordered list of strings), a set of tags (list of strings), and a list of annotations (list of annotation objects). All fields except description, tags, and annotations are mandatory.

#### Scenario: Recipe created with all mandatory fields
- **WHEN** a recipe object is constructed with name, at least one ingredient, number of people, and at least one instruction step
- **THEN** the recipe is valid and can be persisted

#### Scenario: Recipe with empty name is rejected
- **WHEN** a recipe object is constructed with a blank or null name
- **THEN** the system SHALL throw an IllegalArgumentException

#### Scenario: Recipe with zero or negative serving count is rejected
- **WHEN** a recipe is given a number-of-people value less than 1
- **THEN** the system SHALL throw an IllegalArgumentException

### Requirement: Annotation data structure
The system SHALL represent an annotation as an object with: a unique identifier (UUID string), the annotation text (non-empty string), and a creation timestamp (ISO-8601 string).

#### Scenario: Annotation created with text
- **WHEN** an annotation is constructed with non-empty text
- **THEN** the annotation SHALL be assigned a UUID and a creation timestamp automatically

#### Scenario: Annotation with empty text is rejected
- **WHEN** an annotation is constructed with blank or null text
- **THEN** the system SHALL throw an IllegalArgumentException
