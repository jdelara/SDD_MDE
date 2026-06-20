# Requirements Document

## Introduction

A desktop recipe management application built with Java/Swing. The application allows a single user to create, organize, search, and annotate recipes. Recipes are persisted locally as JSON files with no external database dependency. The project follows a standard Eclipse project structure with minimal third-party dependencies.

## Glossary

- **Application**: The[design.md](design.md) Recipe Manager desktop application built with Java/Swing
- **Recipe**: A culinary recipe containing a name, description, ingredients, number of people, instructions, tags, and annotations
- **Tag**: A user-defined label used to categorize and organize recipes (e.g., vegetarian, vegan, dessert)
- **Ingredient**: A component of a recipe, including a name, quantity, and unit of measurement
- **Annotation**: A user-added note, tip, or modification attached to an existing recipe
- **Recipe_Store**: The persistence layer responsible for reading and writing recipe data as JSON files on the local filesystem
- **Search_Engine**: The component responsible for matching recipes against user queries across multiple fields
- **Tag_Manager**: The component responsible for creating, listing, and assigning tags to recipes
- **Recipe_List_View**: The UI component displaying the list of recipes, with filtering by tags
- **Recipe_Editor**: The UI component for creating and editing recipe details
- **Recipe_Detail_View**: The UI component displaying full recipe information including annotations

## Requirements

### Requirement 1: Create Recipe

**User Story:** As a user, I want to create new recipes, so that I can store my culinary creations for future reference.

#### Acceptance Criteria

1. WHEN the user submits a new recipe with a name, description, ingredients, number of people, and instructions, THE Recipe_Editor SHALL create a new Recipe and persist it via the Recipe_Store
2. IF the user submits a recipe without a name or with a name that is empty or exceeds 100 characters, THEN THE Recipe_Editor SHALL display an error message indicating the valid name requirements and SHALL NOT persist the recipe
3. WHEN the user adds an ingredient to a recipe, THE Recipe_Editor SHALL accept a name (1 to 50 characters), a quantity (a positive number from 0.01 to 99999), and a unit of measurement for the ingredient
4. THE Recipe_Editor SHALL allow the user to add between 1 and 50 ingredients to a recipe
5. THE Recipe_Editor SHALL allow the user to assign zero or more tags to a recipe during creation
6. IF the user submits a recipe with a number of people value that is not a whole number between 1 and 100, THEN THE Recipe_Editor SHALL display an error message indicating the valid range and SHALL NOT persist the recipe
7. WHEN the Recipe_Store successfully persists a new recipe, THE Recipe_Editor SHALL display a confirmation message indicating the recipe was saved

### Requirement 2: List and Browse Recipes

**User Story:** As a user, I want to browse my recipes in a list view, so that I can quickly find and select a recipe to view.

#### Acceptance Criteria

1. WHEN the Application starts, THE Recipe_List_View SHALL display all stored recipes sorted alphabetically by name using case-insensitive ordering
2. WHEN the user selects a single tag filter, THE Recipe_List_View SHALL display only recipes associated with the selected tag, sorted alphabetically by name using case-insensitive ordering
3. WHEN no recipes match the selected tag filter, THE Recipe_List_View SHALL display an empty list with a message indicating that no recipes match the selected tag
4. THE Recipe_List_View SHALL display the recipe name and associated tags for each entry in the list
5. WHEN the user clears the tag filter, THE Recipe_List_View SHALL display all stored recipes sorted alphabetically by name using case-insensitive ordering

### Requirement 3: View Recipe Details

**User Story:** As a user, I want to view the full details of a recipe, so that I can follow its instructions while cooking.

#### Acceptance Criteria

1. WHEN the user selects a recipe from the list, THE Recipe_Detail_View SHALL display the recipe name, description, ingredients with quantities and units in their original order, number of people, instructions, tags, and annotations
2. WHEN a recipe has no annotations, THE Recipe_Detail_View SHALL display the recipe details without an annotations section
3. WHEN a recipe has an empty description, THE Recipe_Detail_View SHALL omit the description field from the display

### Requirement 4: Tag Management

**User Story:** As a user, I want to create and manage tags, so that I can organize my recipes into flexible categories.

#### Acceptance Criteria

1. WHEN the user creates a new tag with a unique name of 1 to 30 characters, THE Tag_Manager SHALL add the tag to the list of available tags and persist it via the Recipe_Store
2. WHEN the user creates a tag with a name that already exists (case-insensitive comparison), THE Tag_Manager SHALL display an error message indicating the tag already exists
3. THE Tag_Manager SHALL allow the user to assign or remove tags from an existing recipe
4. WHEN the user explicitly deletes a tag, THE Tag_Manager SHALL remove the tag from the available tags list, remove the tag from all associated recipes, and persist the changes via the Recipe_Store
5. THE Tag_Manager SHALL provide a predefined set of default tags (vegetarian, vegan, dessert, main course, appetizer) on first use when no tags file exists

### Requirement 5: Search Recipes

**User Story:** As a user, I want to search my recipes across multiple fields, so that I can find recipes by name, ingredient, description, or instruction content.

#### Acceptance Criteria

1. WHEN the user enters a search query of at least 1 character, THE Search_Engine SHALL perform substring matching against recipe names, descriptions, ingredient names, and instructions
2. WHEN the search query matches one or more recipes, THE Recipe_List_View SHALL display the matching recipes sorted alphabetically by name
3. WHEN the search query matches no recipes, THE Recipe_List_View SHALL display an empty list with an informational message indicating no results found
4. THE Search_Engine SHALL perform case-insensitive substring matching
5. WHEN the user clears the search query, THE Recipe_List_View SHALL display all recipes sorted alphabetically by name
6. IF the search query contains only whitespace characters, THEN THE Search_Engine SHALL treat it as an empty query and THE Recipe_List_View SHALL display all recipes

### Requirement 6: Annotate Recipes

**User Story:** As a user, I want to add annotations to my recipes, so that I can record notes, tips, and modifications discovered over time.

#### Acceptance Criteria

1. WHEN the user adds an annotation to a recipe, THE Recipe_Detail_View SHALL accept annotation text of 1 to 2000 characters and persist the annotation via the Recipe_Store
2. WHEN the user adds an annotation, THE Recipe_Store SHALL record the annotation text and the date it was created
3. WHILE the user is viewing a recipe that has one or more annotations, THE Recipe_Detail_View SHALL display each annotation with its text and creation date in reverse chronological order (newest first)
4. WHEN the user submits an annotation that is empty or contains only whitespace characters, THE Recipe_Detail_View SHALL display an error message indicating that annotation text is required and SHALL NOT persist the annotation
5. IF the Recipe_Store fails to persist an annotation, THEN THE Recipe_Detail_View SHALL display an error message indicating the annotation was not saved and SHALL retain the entered text in the input field

### Requirement 7: JSON Persistence

**User Story:** As a user, I want my recipes to be saved as JSON files on my local filesystem, so that my data is portable and accessible without a database.

#### Acceptance Criteria

1. WHEN a recipe is created or modified, THE Recipe_Store SHALL persist the recipe data as a JSON file in a designated local directory located at `{user.home}/.recipe-manager/recipes/`
2. WHEN the Application starts, THE Recipe_Store SHALL load all recipe data from existing JSON files (files with `.json` extension) in the designated directory
3. IF the designated storage directory does not exist, THEN THE Recipe_Store SHALL create the directory (including parent directories) before writing any files
4. IF a JSON file is malformed during loading (invalid JSON syntax or missing required fields), THEN THE Recipe_Store SHALL log a warning and skip the malformed file without crashing the Application
5. THE Recipe_Store SHALL use a single JSON file per recipe, named using the recipe's UUID (format: `{uuid}.json`)
6. IF the Recipe_Store fails to write a recipe file to the filesystem, THEN THE Application SHALL display an error message to the user indicating the save operation failed and SHALL log the failure details to a log file located at `{user.home}/.recipe-manager/error.log`

### Requirement 8: Edit Recipe

**User Story:** As a user, I want to edit existing recipes, so that I can correct mistakes or update instructions over time.

#### Acceptance Criteria

1. WHEN the user modifies a recipe's name, description, ingredients, number of people, instructions, or tags and saves the changes, THE Recipe_Editor SHALL update the existing recipe data and persist the changes via the Recipe_Store
2. WHEN the user modifies a recipe name to an empty value and attempts to save, THE Recipe_Editor SHALL display an error message indicating that the name is required and SHALL NOT persist the changes
3. THE Recipe_Editor SHALL preserve existing annotations when a recipe is edited
4. WHEN the user cancels editing a recipe, THE Recipe_Editor SHALL discard all unsaved modifications and retain the original recipe data
5. WHEN the user edits a recipe's ingredients, THE Recipe_Editor SHALL allow the user to add, remove, or modify individual ingredients
6. WHEN the user attempts to navigate away from or close the Recipe_Editor with unsaved modifications, THE Recipe_Editor SHALL prompt the user to save or discard changes before allowing navigation away

### Requirement 9: Delete Recipe

**User Story:** As a user, I want to delete recipes I no longer need, so that I can keep my collection organized.

#### Acceptance Criteria

1. WHEN the user requests to delete a recipe, THE Application SHALL display a confirmation dialog that includes the recipe name before proceeding
2. WHEN the user confirms the deletion, THE Recipe_Store SHALL remove the recipe JSON file from the filesystem and THE Recipe_List_View SHALL remove the recipe from the displayed list
3. WHEN the user cancels the deletion, THE Application SHALL retain the recipe without modification and return the user to the previous view
4. IF the Recipe_Store fails to delete the recipe file from the filesystem, THEN THE Application SHALL display an error message indicating the deletion failed and SHALL retain the recipe in the Recipe_List_View

### Requirement 10: Minimal Dependencies

**User Story:** As a user, I want the application to use minimal external dependencies, so that the project remains lightweight and easy to build.

#### Acceptance Criteria

1. THE Application SHALL use only the Java standard library for UI rendering (Swing/AWT)
2. THE Application SHALL use exactly one external JSON library (such as Gson or Jackson) for serialization and deserialization, with no other external runtime dependencies
3. THE Application SHALL follow a standard Eclipse project structure with source files in a `src` directory and compiled output in a `bin` directory, regardless of whether other requirements could be met with a non-standard layout
