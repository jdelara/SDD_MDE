# Feature Specification: Cooking Recipe Manager

**Feature Branch**: `001-recipe-manager`

**Created**: 2026-06-05

**Status**: Draft

**Input**: User description: "I am building an application to create and manage cooking recipies. The application should list recipies, organised by different tags, like vegetarian, or vegan; but the tags should be extensible. Each recipy has a name, description, ingredients, number of peoples and instructions. The application should allow flexible search for recipies, create new recipies, and enable annotations of the recipy. The application is mono-user, and recipies are stored locally."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a New Recipe (Priority: P1)

A user wants to record a new recipe. They open the application, choose to create a new recipe, fill in the name, description, list of ingredients, the number of people the recipe serves, and step-by-step cooking instructions. They assign one or more tags to categorize it (e.g., "vegetarian", "Italian"). They save the recipe and it appears in the recipe list.

**Why this priority**: Recipe creation is the foundational action — no other feature has value until recipes exist in the system. It is the core MVP.

**Independent Test**: Can be fully tested by launching the app, creating a recipe with all fields filled, saving it, and verifying it appears in the list with correct data retained.

**Acceptance Scenarios**:

1. **Given** the application is open, **When** the user fills all recipe fields and saves, **Then** the recipe appears in the main list with its name and tags visible.
2. **Given** the user is creating a recipe, **When** they attempt to save without a name, **Then** the system prevents saving and highlights the missing required field.
3. **Given** the user is creating a recipe, **When** they type a new tag name that did not previously exist, **Then** the tag is created and immediately available for future recipes.

---

### User Story 2 - Browse and Filter Recipes by Tag (Priority: P2)

A user has accumulated many recipes and wants to find all vegetarian options for tonight's dinner. They open the application, see all their recipes in a list, and select the "vegetarian" tag to filter. The list narrows to show only recipes carrying that tag.

**Why this priority**: Tag-based browsing is the primary organizational feature and delivers immediate value once multiple recipes exist.

**Independent Test**: Can be fully tested by creating several recipes with different tags, filtering by a specific tag, and verifying only correctly-tagged recipes appear.

**Acceptance Scenarios**:

1. **Given** multiple recipes with different tags exist, **When** the user selects a tag to filter by, **Then** only recipes assigned that tag are shown.
2. **Given** a tag filter is active, **When** the user clears the filter, **Then** all recipes are shown again.
3. **Given** the tag list, **When** the user views it, **Then** all tags ever created are listed, including user-defined ones.

---

### User Story 3 - Search Recipes (Priority: P3)

A user remembers cooking something with "lentils" but cannot recall the recipe name. They type "lentils" in the search box and the application returns all recipes mentioning lentils anywhere — in the name, description, ingredients, or instructions.

**Why this priority**: Flexible search becomes essential as the collection grows beyond what tag browsing alone can handle.

**Independent Test**: Can be fully tested by creating recipes with varying content, searching by a keyword present in different fields, and verifying all matching recipes appear regardless of where the keyword occurs.

**Acceptance Scenarios**:

1. **Given** a search term is entered, **When** the search executes, **Then** all recipes containing that term in name, description, ingredients, or instructions are returned.
2. **Given** a search term matches no recipes, **When** the search executes, **Then** an empty-state message is shown.
3. **Given** an active tag filter and a search term are both set, **When** both are applied together, **Then** results are limited to recipes satisfying both conditions simultaneously.

---

### User Story 4 - Annotate a Recipe (Priority: P4)

A user cooked a recipe and wants to note that reducing the salt by half improved the result. They open the recipe, add a text annotation, and save it. The note is stored alongside the recipe with its creation date and is visible every time they view that recipe.

**Why this priority**: Annotations are a personal refinement feature — valuable for experienced cooks but not blocking basic recipe management.

**Independent Test**: Can be fully tested by opening a saved recipe, adding an annotation, saving, closing the app, reopening it, and verifying the annotation appears with its timestamp.

**Acceptance Scenarios**:

1. **Given** an existing recipe, **When** the user adds a text annotation and saves, **Then** the annotation appears in the recipe view with its creation date.
2. **Given** a recipe with annotations, **When** the user views it, **Then** all annotations are listed in chronological order.
3. **Given** a recipe with one annotation, **When** the user adds a second annotation, **Then** both annotations are saved and displayed.

---

### Edge Cases

- What happens when the user saves a recipe with no ingredients?
- How does the system handle a search query containing only whitespace?
- What happens if the user attempts to create a tag with the same name as an existing one?
- What happens when no recipes match the combined tag filter and search query?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: User MUST be able to create a new recipe with the following fields: name (required), description, ingredients list, number of people served, and cooking instructions.
- **FR-002**: Each ingredient entry MUST include at minimum a name; quantity and unit are optional per ingredient.
- **FR-003**: User MUST be able to assign zero or more tags to a recipe at creation time or when editing it.
- **FR-004**: User MUST be able to define new tags on the fly — the tag list is not fixed and MUST be user-extensible.
- **FR-005**: System MUST display all saved recipes in a browsable list showing at minimum the recipe name and its assigned tags.
- **FR-006**: User MUST be able to filter the recipe list by selecting a tag; only recipes carrying that tag are shown.
- **FR-007**: User MUST be able to search recipes using a free-text query matched against name, description, ingredients, and instructions.
- **FR-008**: User MUST be able to view the full detail of a selected recipe, including all fields and annotations.
- **FR-009**: User MUST be able to edit any field of an existing recipe, including name, description, ingredients, instructions, number of people, and tags.
- **FR-010**: User MUST be able to add free-text annotations to any existing recipe.
- **FR-011**: Annotations MUST be stored with a creation timestamp and displayed in chronological order within the recipe detail view.
- **FR-012**: All recipe, tag, and annotation data MUST be persisted locally and survive application restarts without data loss.
- **FR-013**: User MUST be able to delete a recipe from the list.

### Key Entities

- **Recipe**: name, description, number of people served, instructions (ordered steps), tags (many), ingredients (ordered list), annotations (ordered list)
- **Ingredient**: name, quantity (optional), unit (optional)
- **Tag**: name (user-defined string, unique, case-insensitive)
- **Annotation**: text content, creation timestamp

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create a complete recipe with all fields filled in under 3 minutes.
- **SC-002**: The recipe list displays all saved recipes in under 2 seconds for a typical home collection of up to 500 recipes.
- **SC-003**: Selecting a tag filter narrows the recipe list with no perceptible delay — the user sees filtered results immediately.
- **SC-004**: A search query returns all matching recipes across all searchable fields within 1 second.
- **SC-005**: An annotation added to a recipe is present and correct on the next application launch with no data loss.
- **SC-006**: All recipes created in one session are fully available in a subsequent session after closing and reopening the application.

## Assumptions

- The application serves a single user — no multi-user support, authentication, or data sharing is required or planned.
- Data is stored exclusively on the user's local machine; no network connectivity is required or used at any point.
- "Flexible search" means free-text search across all recipe text fields: name, description, ingredients, and instructions.
- Recipe editing (modifying any field of an existing recipe) is treated as an implicit requirement of recipe management and is included.
- Annotations are personal notes tied to a recipe. They are plain text with an automatic creation timestamp. Editing or deleting individual annotations is out of scope for this version.
- Tags are case-insensitive and unique. Attempting to add a tag with the same name as an existing one (case-insensitively) reuses the existing tag.
- Deleting a recipe removes the recipe and its annotations. Tags are not deleted when a recipe using them is removed — they remain available for other recipes.
- Saving a recipe with no ingredients is permitted; the ingredient list may be empty.
- Up to 500 recipes is the target scale for performance expectations; larger collections are not an explicit design goal.
