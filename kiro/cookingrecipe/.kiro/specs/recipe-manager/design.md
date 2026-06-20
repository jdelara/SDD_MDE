# Design Document: Recipe Manager

## Overview

The Recipe Manager is a single-user Java/Swing desktop application for creating, organizing, searching, and annotating culinary recipes. Recipes are persisted as individual JSON files on the local filesystem using a single external JSON library (Gson). The application follows a standard Eclipse project structure and operates entirely offline with no network or database dependencies.

The architecture follows the Model-View-Controller (MVC) pattern to separate concerns between data management, business logic, and UI presentation. Swing components serve as views and controllers, while plain Java objects represent the domain model.

## Architecture

```mermaid
graph TD
    subgraph UI Layer (Swing)
        RLV[RecipeListView]
        RDV[RecipeDetailView]
        RE[RecipeEditor]
        TM[TagManagerDialog]
        SearchBar[SearchBar]
    end

    subgraph Controller Layer
        RC[RecipeController]
        TC[TagController]
        SC[SearchController]
    end

    subgraph Model/Service Layer
        RS[RecipeStore]
        SE[SearchEngine]
        TMS[TagService]
        VM[ValidationService]
    end

    subgraph Persistence Layer
        FP[FileSystemPersistence]
        JSON[Gson Serialization]
    end

    RLV --> RC
    RDV --> RC
    RE --> RC
    TM --> TC
    SearchBar --> SC

    RC --> RS
    RC --> VM
    TC --> TMS
    SC --> SE

    RS --> FP
    TMS --> FP
    FP --> JSON
```

### Design Decisions

1. **MVC Pattern**: Chosen for clear separation between Swing UI components and business logic, making the core logic testable without UI dependencies.
2. **Gson over Jackson**: Gson is lighter-weight (single JAR, ~250KB), has no transitive dependencies, and is sufficient for simple JSON serialization of POJOs.
3. **One file per recipe**: Simplifies CRUD operations — creating a recipe writes one file, deleting removes one file, no index maintenance required.
4. **UUID-based filenames**: Eliminates filename conflicts and special character handling issues.
5. **Eager loading on startup**: Given single-user desktop usage with a reasonable recipe count (hundreds, not millions), loading all recipes into memory at startup provides the simplest search and browse implementation.

## Components and Interfaces

### Domain Model Classes

#### Recipe
```java
public class Recipe {
    private UUID id;
    private String name;              // 1-100 characters
    private String description;       // optional, may be empty
    private List<Ingredient> ingredients; // 1-50 items
    private int servings;             // 1-100 (number of people)
    private String instructions;      // required
    private List<String> tags;        // 0 or more tag names
    private List<Annotation> annotations; // 0 or more
}
```

#### Ingredient
```java
public class Ingredient {
    private String name;     // 1-50 characters
    private double quantity; // 0.01-99999
    private String unit;     // unit of measurement
}
```

#### Annotation
```java
public class Annotation {
    private String text;          // 1-2000 characters
    private LocalDateTime createdAt;
}
```

### Service Interfaces

#### RecipeStore
```java
public interface RecipeStore {
    List<Recipe> loadAll();
    void save(Recipe recipe) throws PersistenceException;
    void delete(UUID recipeId) throws PersistenceException;
}
```

#### SearchEngine
```java
public interface SearchEngine {
    List<Recipe> search(String query, List<Recipe> recipes);
}
```

#### TagService
```java
public interface TagService {
    List<String> getAllTags();
    void createTag(String tagName) throws DuplicateTagException;
    void deleteTag(String tagName);
    void assignTag(UUID recipeId, String tagName);
    void removeTag(UUID recipeId, String tagName);
}
```

#### ValidationService
```java
public interface ValidationService {
    ValidationResult validateRecipe(Recipe recipe);
    ValidationResult validateIngredient(Ingredient ingredient);
    ValidationResult validateAnnotation(String text);
}
```

### UI Components

| Component | Responsibility |
|-----------|---------------|
| `MainFrame` | Top-level JFrame, hosts card layout for switching panels |
| `RecipeListPanel` | JList with recipe names + tags, tag filter combo box |
| `RecipeDetailPanel` | Read-only display of full recipe with annotations |
| `RecipeEditorPanel` | Form for creating/editing recipes with validation feedback |
| `TagManagerDialog` | JDialog for creating/deleting tags |
| `SearchPanel` | JTextField for search input, triggers filtering |
| `ConfirmationDialogs` | Reusable delete/discard confirmation dialogs |

### Controller Classes

| Controller | Responsibility |
|------------|---------------|
| `RecipeController` | Coordinates CRUD operations between UI and RecipeStore |
| `TagController` | Manages tag lifecycle and recipe-tag associations |
| `SearchController` | Bridges search input to SearchEngine and updates list view |

## Data Models

### Recipe JSON Format

Each recipe is stored as `{uuid}.json` in `{user.home}/.recipe-manager/recipes/`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Spaghetti Carbonara",
  "description": "Classic Italian pasta dish",
  "ingredients": [
    {
      "name": "Spaghetti",
      "quantity": 400.0,
      "unit": "g"
    },
    {
      "name": "Guanciale",
      "quantity": 200.0,
      "unit": "g"
    }
  ],
  "servings": 4,
  "instructions": "1. Cook pasta...\n2. Prepare sauce...",
  "tags": ["main course"],
  "annotations": [
    {
      "text": "Try with pecorino instead of parmesan",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### Tags Persistence

Tags are stored in a separate file at `{user.home}/.recipe-manager/tags.json`:

```json
{
  "tags": ["vegetarian", "vegan", "dessert", "main course", "appetizer"]
}
```

On first launch when `tags.json` does not exist, the application creates it with the default tag set.

### Directory Structure

```
{user.home}/.recipe-manager/
├── recipes/
│   ├── {uuid1}.json
│   ├── {uuid2}.json
│   └── ...
├── tags.json
└── error.log
```

### Eclipse Project Structure

```
recipe-manager/
├── src/
│   └── com/
│       └── recipemanager/
│           ├── model/
│           │   ├── Recipe.java
│           │   ├── Ingredient.java
│           │   └── Annotation.java
│           ├── service/
│           │   ├── RecipeStore.java
│           │   ├── FileSystemRecipeStore.java
│           │   ├── SearchEngine.java
│           │   ├── SimpleSearchEngine.java
│           │   ├── TagService.java
│           │   ├── FileSystemTagService.java
│           │   ├── ValidationService.java
│           │   └── DefaultValidationService.java
│           ├── controller/
│           │   ├── RecipeController.java
│           │   ├── TagController.java
│           │   └── SearchController.java
│           ├── ui/
│           │   ├── MainFrame.java
│           │   ├── RecipeListPanel.java
│           │   ├── RecipeDetailPanel.java
│           │   ├── RecipeEditorPanel.java
│           │   ├── TagManagerDialog.java
│           │   └── SearchPanel.java
│           ├── util/
│           │   └── ErrorLogger.java
│           └── App.java
├── lib/
│   └── gson-2.10.1.jar
├── bin/
├── .classpath
└── .project
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Recipe serialization round-trip

*For any* valid Recipe object, serializing it to JSON and writing it to disk, then reading the file back and deserializing it, SHALL produce a Recipe object equal to the original (all fields preserved including id, name, description, ingredients in order, servings, instructions, tags, and annotations).

**Validates: Requirements 1.1, 7.5, 8.1**

### Property 2: Recipe validation accepts valid and rejects invalid

*For any* recipe input data, the ValidationService SHALL accept the recipe if and only if: the name is 1-100 characters, servings is a whole number 1-100, the ingredient count is 1-50, each ingredient name is 1-50 characters, and each ingredient quantity is 0.01-99999. Any input violating these bounds SHALL be rejected.

**Validates: Requirements 1.2, 1.3, 1.4, 1.6**

### Property 3: Recipe list is sorted case-insensitively

*For any* list of recipes, when displayed in the Recipe_List_View (unfiltered or after search), the resulting order SHALL be alphabetical by name using case-insensitive comparison — meaning for every adjacent pair (a, b) in the list, `a.name.compareToIgnoreCase(b.name) <= 0`.

**Validates: Requirements 2.1, 2.5, 5.2**

### Property 4: Tag filter returns only matching recipes

*For any* collection of recipes and any selected tag, filtering by that tag SHALL return exactly the subset of recipes whose tag list contains the selected tag, and no others.

**Validates: Requirements 2.2**

### Property 5: Search performs case-insensitive substring matching across fields

*For any* non-empty, non-whitespace search query and any collection of recipes, the search results SHALL contain exactly those recipes where the query (case-insensitive) appears as a substring in at least one of: name, description, any ingredient name, or instructions.

**Validates: Requirements 5.1, 5.4**

### Property 6: Whitespace-only search returns all recipes

*For any* string composed entirely of whitespace characters (spaces, tabs, newlines), performing a search SHALL return all recipes (equivalent to no filter applied).

**Validates: Requirements 5.6**

### Property 7: Tag creation rejects case-insensitive duplicates

*For any* existing tag name and any case variant of that name (same characters, different casing), attempting to create a new tag with the case variant SHALL be rejected as a duplicate.

**Validates: Requirements 4.2**

### Property 8: Tag deletion cascades to all recipes

*For any* tag that is associated with one or more recipes, deleting that tag SHALL result in the tag being absent from the tag list AND absent from the tags field of every recipe that previously contained it.

**Validates: Requirements 4.4**

### Property 9: Annotation persistence round-trip

*For any* valid annotation text (1-2000 non-whitespace-only characters), adding the annotation to a recipe and then reloading the recipe from disk SHALL produce an annotation with the same text and a non-null creation date.

**Validates: Requirements 6.1, 6.2**

### Property 10: Annotations are ordered newest-first

*For any* recipe with two or more annotations, the annotations SHALL be displayed in reverse chronological order — for every adjacent pair (a, b) in the display list, `a.createdAt >= b.createdAt`.

**Validates: Requirements 6.3**

### Property 11: Whitespace-only annotations are rejected

*For any* string composed entirely of whitespace characters (including empty string), the annotation validation SHALL reject it, and the recipe's annotation list SHALL remain unchanged.

**Validates: Requirements 6.4**

### Property 12: Annotations are preserved during recipe edit

*For any* recipe with existing annotations, editing any combination of name, description, ingredients, servings, instructions, or tags and saving SHALL result in the annotations list being identical to its pre-edit state.

**Validates: Requirements 8.3**

### Property 13: Cancel edit discards all changes

*For any* recipe and any set of modifications made in the editor, cancelling the edit SHALL result in the recipe's persisted state being identical to its state before the edit began.

**Validates: Requirements 8.4**

### Property 14: Delete removes recipe from storage and listing

*For any* recipe in the collection, confirming its deletion SHALL result in: the recipe's JSON file no longer existing on disk, AND the recipe no longer appearing in the loaded recipe list.

**Validates: Requirements 9.2**

### Property 15: Malformed JSON files are skipped without crash

*For any* file containing invalid JSON syntax or valid JSON missing required recipe fields, the Recipe_Store loading process SHALL skip that file, log a warning, and successfully load all other valid recipe files in the directory.

**Validates: Requirements 7.4**

## Error Handling

### Error Categories and Responses

| Error Category | Trigger | User Response | System Response |
|---------------|---------|---------------|-----------------|
| Validation Error | Invalid recipe/ingredient/annotation input | Inline error message near the offending field | Prevent persistence, retain form data |
| Persistence Write Failure | Filesystem I/O error during save | Error dialog with "save failed" message | Log details to `error.log`, retain in-memory state |
| Persistence Delete Failure | Filesystem I/O error during delete | Error dialog with "delete failed" message | Log details to `error.log`, retain recipe in list |
| Malformed JSON on Load | Corrupted/invalid file in recipes directory | None (transparent to user) | Log warning to `error.log`, skip file, continue loading |
| Directory Creation Failure | Cannot create `.recipe-manager/recipes/` | Error dialog on startup | Log error, application exits gracefully |
| Duplicate Tag | Case-insensitive tag name collision | Inline error "tag already exists" | Prevent creation, retain input |

### Error Logging

- Error log location: `{user.home}/.recipe-manager/error.log`
- Log format: `[ISO-8601 timestamp] [LEVEL] message`
- Log levels: WARN (malformed files skipped), ERROR (I/O failures)
- Log rotation: not required for v1 (single-user, infrequent errors)

### Exception Hierarchy

```java
public class PersistenceException extends Exception {
    // Wraps IOException for save/delete failures
}

public class DuplicateTagException extends Exception {
    // Tag already exists (case-insensitive)
}

public class ValidationException extends RuntimeException {
    private final List<String> errors;
    // Contains all validation errors for a single operation
}
```

### Graceful Degradation

- If recipe loading encounters malformed files, the application starts with whatever valid recipes were loaded
- If a single save fails, other operations remain available — the in-memory state is preserved
- If the tags file is malformed, the application recreates it with defaults

## Testing Strategy

### Testing Approach

The testing strategy uses a dual approach:

1. **Property-based tests** — Verify universal correctness properties across generated inputs (minimum 100 iterations per property)
2. **Unit tests** — Verify specific examples, edge cases, and error handling scenarios

### Property-Based Testing Framework

- **Library**: [jqwik](https://jqwik.net/) — a mature property-based testing framework for Java that integrates with JUnit 5
- **Minimum iterations**: 100 per property
- **Tag format**: `@Tag("Feature: recipe-manager, Property {N}: {title}")`

### Test Organization

```
src-test/
└── com/
    └── recipemanager/
        ├── model/
        │   └── RecipePropertyTest.java      (Properties 1, 2)
        ├── service/
        │   ├── SearchEnginePropertyTest.java (Properties 5, 6)
        │   ├── TagServicePropertyTest.java   (Properties 7, 8)
        │   ├── RecipeStorePropertyTest.java  (Properties 1, 14, 15)
        │   └── ValidationServiceTest.java    (Property 2, edge cases)
        ├── controller/
        │   ├── RecipeControllerTest.java     (Properties 12, 13, examples)
        │   └── TagControllerTest.java        (Examples, integration)
        └── integration/
            └── PersistenceIntegrationTest.java (File I/O scenarios)
```

### Property Test Implementation Plan

Each correctness property maps to a single property-based test:

| Property | Test Class | Key Generators |
|----------|-----------|----------------|
| P1: Serialization round-trip | `RecipeStorePropertyTest` | Random valid Recipe objects |
| P2: Validation bounds | `ValidationServiceTest` | Random strings (various lengths), random numbers (in/out of range) |
| P3: Sorting | `RecipeControllerTest` | Lists of recipes with random names (various cases) |
| P4: Tag filtering | `RecipeControllerTest` | Recipe collections with random tag assignments |
| P5: Search matching | `SearchEnginePropertyTest` | Recipes with known substrings, queries derived from content |
| P6: Whitespace search | `SearchEnginePropertyTest` | Whitespace-only strings of various compositions |
| P7: Tag duplicates | `TagServicePropertyTest` | Tag names and their case permutations |
| P8: Tag deletion cascade | `TagServicePropertyTest` | Recipe collections sharing a target tag |
| P9: Annotation round-trip | `RecipeStorePropertyTest` | Random valid annotation text strings |
| P10: Annotation ordering | `RecipeControllerTest` | Lists of annotations with random timestamps |
| P11: Whitespace annotation rejection | `ValidationServiceTest` | Whitespace-only strings |
| P12: Annotations preserved | `RecipeControllerTest` | Recipes with annotations, random edits to other fields |
| P13: Cancel discards | `RecipeControllerTest` | Recipes with random unsaved modifications |
| P14: Delete removes | `RecipeStorePropertyTest` | Random recipe in a collection |
| P15: Malformed JSON skipped | `RecipeStorePropertyTest` | Random invalid JSON strings |

### Unit Tests (Example-Based)

Unit tests cover:
- UI interaction behaviors (confirmation dialogs, error messages displayed, input field clearing)
- Specific error scenarios (write failure, delete failure)
- Feature smoke tests (default tags creation, directory creation)
- Integration points (file naming, directory paths)

### Test Dependencies

- jqwik 1.8+ (property-based testing)
- JUnit 5 (test framework, required by jqwik)
- Mockito (mocking for controller/UI tests)
- Temporary directory support via `@TempDir` for filesystem tests
