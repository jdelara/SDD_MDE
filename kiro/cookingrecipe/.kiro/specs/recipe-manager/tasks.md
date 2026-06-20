# Implementation Plan: Recipe Manager

## Overview

Implement a Java/Swing desktop recipe management application following MVC architecture. The implementation progresses from domain models and persistence, through services and controllers, to UI components, with property-based tests validating correctness at each layer. The project uses a standard Eclipse structure with Gson as the sole external runtime dependency.

## Tasks

- [x] 1. Set up project structure and domain model
  - [x] 1.1 Create Eclipse project structure and domain model classes
    - Create directory layout: `src/com/recipemanager/{model,service,controller,ui,util}/`
    - Create `lib/` directory and add `gson-2.10.1.jar`
    - Create `.classpath` and `.project` files for Eclipse
    - Implement `Recipe.java` with fields: id (UUID), name, description, ingredients list, servings, instructions, tags list, annotations list
    - Implement `Ingredient.java` with fields: name, quantity, unit
    - Implement `Annotation.java` with fields: text, createdAt (LocalDateTime)
    - Add proper equals/hashCode, getters, setters, and constructors
    - _Requirements: 1.1, 1.3, 6.2, 10.3_

  - [x] 1.2 Create exception classes and ValidationService
    - Implement `PersistenceException` extending Exception
    - Implement `DuplicateTagException` extending Exception
    - Implement `ValidationException` extending RuntimeException with a list of error messages
    - Create `ValidationService` interface with `validateRecipe`, `validateIngredient`, `validateAnnotation` methods
    - Implement `DefaultValidationService`: name 1-100 chars, servings 1-100 whole number, ingredients 1-50 count, ingredient name 1-50 chars, ingredient quantity 0.01-99999, annotation text 1-2000 chars and not whitespace-only
    - _Requirements: 1.2, 1.3, 1.4, 1.6, 6.4_

  - [x] 1.3 Write property test for validation bounds (Property 2)
    - **Property 2: Recipe validation accepts valid and rejects invalid**
    - Generate random recipe data with names of various lengths, servings in/out of range, ingredient counts in/out of bounds, ingredient names/quantities at boundary values
    - Assert ValidationService accepts if and only if all bounds are satisfied
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.6**

  - [x] 1.4 Write property test for whitespace annotation rejection (Property 11)
    - **Property 11: Whitespace-only annotations are rejected**
    - Generate strings composed entirely of whitespace (spaces, tabs, newlines, empty string)
    - Assert ValidationService rejects all such inputs
    - **Validates: Requirements 6.4**

- [x] 2. Implement persistence layer
  - [x] 2.1 Implement ErrorLogger utility
    - Create `ErrorLogger.java` in `util/` package
    - Log to `{user.home}/.recipe-manager/error.log`
    - Log format: `[ISO-8601 timestamp] [LEVEL] message`
    - Support WARN and ERROR levels
    - Create parent directories if they don't exist
    - _Requirements: 7.6_

  - [x] 2.2 Implement FileSystemRecipeStore
    - Create `RecipeStore` interface with `loadAll()`, `save(Recipe)`, `delete(UUID)` methods
    - Implement `FileSystemRecipeStore` using Gson for JSON serialization/deserialization
    - Storage directory: `{user.home}/.recipe-manager/recipes/`
    - Create directories if they don't exist on first write
    - File naming: `{uuid}.json`
    - On `loadAll()`: read all `.json` files, deserialize each; skip malformed files with WARN log
    - Handle `LocalDateTime` serialization with a custom Gson TypeAdapter
    - Throw `PersistenceException` on write/delete I/O failures
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [x] 2.3 Write property test for serialization round-trip (Property 1)
    - **Property 1: Recipe serialization round-trip**
    - Generate random valid Recipe objects (with ingredients, annotations, tags)
    - Serialize to JSON file, read back, deserialize
    - Assert the deserialized Recipe equals the original (all fields preserved including ingredient order)
    - Use `@TempDir` for filesystem isolation
    - **Validates: Requirements 1.1, 7.5, 8.1**

  - [x] 2.4 Write property test for malformed JSON handling (Property 15)
    - **Property 15: Malformed JSON files are skipped without crash**
    - Generate random invalid JSON strings and valid JSON missing required fields
    - Write them as `.json` files alongside valid recipe files
    - Assert `loadAll()` skips malformed files and returns all valid recipes
    - **Validates: Requirements 7.4**

  - [x] 2.5 Write property test for delete removes recipe (Property 14)
    - **Property 14: Delete removes recipe from storage and listing**
    - Generate a collection of valid recipes, save them all, delete one
    - Assert the deleted recipe's file no longer exists on disk and `loadAll()` no longer returns it
    - **Validates: Requirements 9.2**

- [x] 3. Checkpoint - Core persistence verified
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement tag service and search engine
  - [x] 4.1 Implement FileSystemTagService
    - Create `TagService` interface with `getAllTags()`, `createTag(String)`, `deleteTag(String)`, `assignTag(UUID, String)`, `removeTag(UUID, String)` methods
    - Implement `FileSystemTagService` persisting to `{user.home}/.recipe-manager/tags.json`
    - On first use when `tags.json` doesn't exist, create it with defaults: vegetarian, vegan, dessert, main course, appetizer
    - Case-insensitive duplicate detection on `createTag`
    - `deleteTag` removes from tags list AND removes from all recipes' tag lists via RecipeStore, persists both
    - Throw `DuplicateTagException` when tag name already exists (case-insensitive)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 4.2 Write property test for tag duplicate rejection (Property 7)
    - **Property 7: Tag creation rejects case-insensitive duplicates**
    - Generate tag names and their case permutations (e.g., "Vegan" → "VEGAN", "vEgAn")
    - Assert that creating a case variant of an existing tag throws DuplicateTagException
    - **Validates: Requirements 4.2**

  - [x] 4.3 Write property test for tag deletion cascade (Property 8)
    - **Property 8: Tag deletion cascades to all recipes**
    - Generate recipes with shared tags, delete one tag
    - Assert the tag is absent from all recipes' tag lists and from the available tags list
    - **Validates: Requirements 4.4**

  - [x] 4.4 Implement SimpleSearchEngine
    - Create `SearchEngine` interface with `search(String query, List<Recipe> recipes)` method
    - Implement `SimpleSearchEngine` performing case-insensitive substring matching
    - Search across: recipe name, description, ingredient names, instructions
    - Whitespace-only queries return all recipes (treated as empty query)
    - Return matching recipes (ordering handled by caller)
    - _Requirements: 5.1, 5.4, 5.6_

  - [x] 4.5 Write property test for search matching (Property 5)
    - **Property 5: Search performs case-insensitive substring matching across fields**
    - Generate recipes with known content, derive queries from that content
    - Assert results contain exactly those recipes where query is a substring of name, description, any ingredient name, or instructions
    - **Validates: Requirements 5.1, 5.4**

  - [x] 4.6 Write property test for whitespace search (Property 6)
    - **Property 6: Whitespace-only search returns all recipes**
    - Generate whitespace-only strings (spaces, tabs, newlines, mixed)
    - Assert search returns the complete recipe list
    - **Validates: Requirements 5.6**

- [x] 5. Implement controllers
  - [x] 5.1 Implement RecipeController
    - Create `RecipeController.java` coordinating between UI and RecipeStore/ValidationService
    - Methods: `createRecipe(Recipe)`, `updateRecipe(Recipe)`, `deleteRecipe(UUID)`, `getAllRecipes()`, `getRecipesSorted()`, `filterByTag(String tag)`, `addAnnotation(UUID, String)`
    - `createRecipe`: validate via ValidationService, persist via RecipeStore, return success/failure
    - `updateRecipe`: validate, preserve existing annotations, persist
    - `deleteRecipe`: delete via RecipeStore, return success/failure
    - `getRecipesSorted`: return recipes sorted alphabetically (case-insensitive)
    - `filterByTag`: return sorted subset matching the tag
    - `addAnnotation`: validate text, create Annotation with current timestamp, append to recipe, persist
    - _Requirements: 1.1, 1.7, 2.1, 2.2, 6.1, 6.2, 8.1, 8.3, 9.2_

  - [x] 5.2 Write property test for sorted listing (Property 3)
    - **Property 3: Recipe list is sorted case-insensitively**
    - Generate lists of recipes with random names (various cases)
    - Assert `getRecipesSorted()` returns list where every adjacent pair satisfies `a.name.compareToIgnoreCase(b.name) <= 0`
    - **Validates: Requirements 2.1, 2.5, 5.2**

  - [x] 5.3 Write property test for tag filter correctness (Property 4)
    - **Property 4: Tag filter returns only matching recipes**
    - Generate recipes with random tag assignments, filter by a specific tag
    - Assert result contains exactly the recipes whose tags list contains the selected tag
    - **Validates: Requirements 2.2**

  - [x] 5.4 Write property test for annotation persistence round-trip (Property 9)
    - **Property 9: Annotation persistence round-trip**
    - Generate valid annotation text (1-2000 chars, not whitespace-only)
    - Add annotation to a recipe, reload from disk, assert annotation text and non-null createdAt
    - **Validates: Requirements 6.1, 6.2**

  - [x] 5.5 Write property test for annotation ordering (Property 10)
    - **Property 10: Annotations are ordered newest-first**
    - Generate recipes with multiple annotations at random timestamps
    - Assert displayed ordering satisfies `a.createdAt >= b.createdAt` for every adjacent pair
    - **Validates: Requirements 6.3**

  - [x] 5.6 Implement TagController and SearchController
    - Create `TagController.java` bridging UI to TagService: `createTag`, `deleteTag`, `assignTag`, `removeTag`, `getAllTags`
    - Create `SearchController.java` bridging SearchPanel to SearchEngine: `search(String query)` returns sorted results
    - SearchController uses RecipeController for sorted full list when query is empty/whitespace
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.5_

  - [x] 5.7 Write property test for annotations preserved during edit (Property 12)
    - **Property 12: Annotations are preserved during recipe edit**
    - Generate recipes with annotations, apply random edits to name/description/ingredients/servings/instructions/tags
    - Assert annotations list is identical after save
    - **Validates: Requirements 8.3**

  - [x] 5.8 Write property test for cancel discards changes (Property 13)
    - **Property 13: Cancel edit discards all changes**
    - Generate a recipe, apply random modifications in editor, cancel
    - Assert persisted recipe state is identical to pre-edit state
    - **Validates: Requirements 8.4**

- [x] 6. Checkpoint - Business logic verified
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement UI layer
  - [x] 7.1 Implement MainFrame and RecipeListPanel
    - Create `MainFrame.java` as top-level JFrame with CardLayout for panel switching
    - Create `RecipeListPanel.java` with JList displaying recipe names and tags
    - Add tag filter JComboBox that triggers `RecipeController.filterByTag`
    - Display "No recipes match the selected tag" when filter yields empty results
    - "Clear filter" option restores full sorted list
    - Wire to RecipeController for data loading on startup
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 7.2 Implement RecipeDetailPanel
    - Create `RecipeDetailPanel.java` displaying full recipe: name, description (omitted if empty), ingredients with quantities/units in original order, servings, instructions, tags
    - Display annotations section with text and creation date, ordered newest-first
    - Hide annotations section when recipe has no annotations
    - Add "Add Annotation" input area with submit button
    - Validate annotation (non-empty, non-whitespace, 1-2000 chars) before submission
    - Display error messages for invalid annotations
    - Retain annotation text in input field on persistence failure
    - Add "Edit" and "Delete" buttons for the recipe
    - _Requirements: 3.1, 3.2, 3.3, 6.1, 6.2, 6.3, 6.4, 6.5_

  - [x] 7.3 Implement RecipeEditorPanel
    - Create `RecipeEditorPanel.java` with form fields: name, description, servings, instructions, tag assignment checkboxes
    - Implement dynamic ingredient list (add/remove/modify rows) with name, quantity, unit fields
    - Display inline validation error messages for: empty/long name, invalid servings, invalid ingredients
    - Confirmation message on successful save
    - "Cancel" button discards changes and returns to previous view
    - Unsaved changes detection: prompt to save/discard on navigation away
    - Support both create-new and edit-existing modes
    - In edit mode, populate fields from existing recipe; preserve annotations on save
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x] 7.4 Implement SearchPanel and TagManagerDialog
    - Create `SearchPanel.java` with JTextField for query input
    - Wire to SearchController; update RecipeListPanel on each keystroke or submit
    - Display "No results found" message when search yields empty results
    - Clearing the search field restores full sorted recipe list
    - Create `TagManagerDialog.java` as JDialog for creating/deleting tags
    - Display existing tags in a list with "Delete" button per tag
    - "Create tag" field with validation (1-30 chars, unique case-insensitive)
    - Display "tag already exists" error on duplicate
    - Confirmation before tag deletion
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 4.1, 4.2, 4.4_

  - [x] 7.5 Implement delete confirmation and App entry point
    - Create `ConfirmationDialogs` utility with reusable confirmation dialog (shows recipe name)
    - Wire delete flow: detail view → confirm dialog → RecipeStore.delete → update list
    - Display error message if deletion fails; retain recipe in list
    - Create `App.java` with `main` method: initialize RecipeStore, load recipes, create controllers, launch MainFrame on Swing EDT
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 10.1, 10.2_

- [x] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases
- All filesystem tests should use `@TempDir` for isolation
- Gson TypeAdapter needed for `LocalDateTime` serialization (ISO-8601 format)
- UI tests may require mocking controllers; focus property tests on service/model layers

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "2.1"] },
    { "id": 2, "tasks": ["1.3", "1.4", "2.2"] },
    { "id": 3, "tasks": ["2.3", "2.4", "2.5", "4.1", "4.4"] },
    { "id": 4, "tasks": ["4.2", "4.3", "4.5", "4.6", "5.1"] },
    { "id": 5, "tasks": ["5.2", "5.3", "5.4", "5.5", "5.6"] },
    { "id": 6, "tasks": ["5.7", "5.8", "7.1"] },
    { "id": 7, "tasks": ["7.2", "7.3", "7.4"] },
    { "id": 8, "tasks": ["7.5"] }
  ]
}
```
