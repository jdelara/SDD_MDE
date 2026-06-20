---
description: "Task list for Cooking Recipe Manager implementation"
---

# Tasks: Cooking Recipe Manager

**Input**: Design documents from `specs/001-recipe-manager/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | data-model.md ✅ | contracts/json-schema.md ✅ | research.md ✅

**Tests**: Not requested — manual validation via quickstart.md scenarios.

**Organization**: Tasks are grouped by user story to enable independent implementation and
testing of each story. No external libraries — all Java SE only.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no shared state dependencies)
- **[Story]**: Which user story this task belongs to (US1–US4)
- Exact file paths included in every task description

## Path Conventions

All source paths are relative to the Eclipse project root (`CookingRecipies/`):

- Java source: `src/com/cookingrecipes/<subpackage>/<ClassName>.java`
- Storage data: `data/recipes.json`
- Eclipse config: `.project`, `.classpath`

---

## Phase 1: Setup

**Purpose**: Initialize the Eclipse project structure so the project opens and builds cleanly.

- [x] T001 Create Eclipse project descriptor at `.project` (projectDescription with name `CookingRecipies`, nature `org.eclipse.jdt.core.javanature`)
- [x] T002 Create Eclipse classpath file at `.classpath` (source entry `src`, output entry `bin`, JRE container for JavaSE-11)
- [x] T003 Create Java package directory tree: `src/com/cookingrecipes/model/`, `src/com/cookingrecipes/storage/`, `src/com/cookingrecipes/service/`, `src/com/cookingrecipes/ui/`
- [x] T004 [P] Create `data/` directory placeholder (empty; `RecipeStore` auto-creates `recipes.json` on first save)
- [x] T005 [P] Create `.gitignore` excluding `bin/` and `data/recipes.json` from version control

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Model POJOs, JSON serializer, storage layer, and service layer must exist before
any UI can be built. All user stories depend on this phase.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T006 [P] Create `Recipe.java` (fields: `id` String, `name` String, `description` String, `serves` int, `instructions` String, `tags` List\<Tag\>, `ingredients` List\<Ingredient\>, `annotations` List\<Annotation\>; getters/setters) in `src/com/cookingrecipes/model/Recipe.java`
- [x] T007 [P] Create `Ingredient.java` (fields: `name` String, `quantity` String, `unit` String; getters/setters) in `src/com/cookingrecipes/model/Ingredient.java`
- [x] T008 [P] Create `Tag.java` (field: `name` String; getters/setter; `equals`/`hashCode` on name, case-insensitive) in `src/com/cookingrecipes/model/Tag.java`
- [x] T009 [P] Create `Annotation.java` (fields: `text` String, `timestamp` String ISO-8601; getters/setters) in `src/com/cookingrecipes/model/Annotation.java`
- [x] T010 Create `JsonSerializer.java` with `serialize(List<Recipe>, List<Tag>): String` and `deserialize(String): RecipeStore` methods using manual string building and a simple character-by-character JSON parser (no external library); follow schema in `specs/001-recipe-manager/contracts/json-schema.md` in `src/com/cookingrecipes/storage/JsonSerializer.java`
- [x] T011 Create `RecipeStore.java` holding `List<Recipe> recipes` and `List<Tag> tags` in memory; `load()` reads `data/recipes.json` via `JsonSerializer` (creates empty store if file absent); `save()` writes atomically via temp file + `Files.move(REPLACE_EXISTING)` in `src/com/cookingrecipes/storage/RecipeStore.java`
- [x] T012 Create `RecipeService.java` with methods: `createRecipe(Recipe)`, `updateRecipe(Recipe)`, `deleteRecipe(String id)`, `getAllRecipes(): List<Recipe>`, `getAllTags(): List<Tag>`, `addAnnotation(String recipeId, String text)`, `getRecipesByTag(Tag): List<Recipe>`, `searchRecipes(String query): List<Recipe>`; each mutating method calls `RecipeStore.save()` in `src/com/cookingrecipes/service/RecipeService.java`

**Checkpoint**: Foundational phase complete — all user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 — Create a New Recipe (Priority: P1) 🎯 MVP

**Goal**: User can create, edit, and delete recipes; all data persists in `data/recipes.json`.

**Independent Test**: Launch app, create a recipe with every field filled, save, verify it
appears in the list with correct data, close app, reopen — recipe still present.

- [x] T013 [P] [US1] Create `RecipeFormDialog.java` (extends `JDialog`): fields for name (`JTextField`), description (`JTextArea`), serves (`JSpinner`), instructions (`JTextArea`), a tag entry field with "Add Tag" button populating a `JList`, and a scrollable ingredient table (rows: name, quantity, unit) with Add/Remove row buttons; Save and Cancel buttons in `src/com/cookingrecipes/ui/RecipeFormDialog.java`
- [x] T014 [P] [US1] Create `RecipeListPanel.java` (extends `JPanel`): `JList<Recipe>` with a custom `ListCellRenderer` showing recipe name and tag names on each row; exposes `setRecipes(List<Recipe>)` refresh method and `addSelectionListener()` in `src/com/cookingrecipes/ui/RecipeListPanel.java`
- [x] T015 [US1] Create `MainWindow.java` (extends `JFrame`): constructs `JSplitPane` layout (left: tag filter placeholder, center: `RecipeListPanel`; right: recipe detail placeholder); toolbar with "New Recipe", "Edit Recipe", "Delete Recipe" buttons; wires to `RecipeService` passed in constructor in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T016 [US1] Wire `RecipeFormDialog` Save button to call `RecipeService.createRecipe()` (new recipe mode) or `RecipeService.updateRecipe()` (edit mode) using a `UUID.randomUUID()` id for new recipes; close dialog on success in `src/com/cookingrecipes/ui/RecipeFormDialog.java`
- [x] T017 [US1] Add input validation in `RecipeFormDialog`: disable Save / show red border on name field when name is blank; clear error state when name is non-empty in `src/com/cookingrecipes/ui/RecipeFormDialog.java`
- [x] T018 [US1] Wire "New Recipe" button in `MainWindow` to open empty `RecipeFormDialog`; refresh `RecipeListPanel` on dialog close with updated `RecipeService.getAllRecipes()` in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T019 [US1] Wire "Edit Recipe" button in `MainWindow` to open `RecipeFormDialog` pre-filled with the selected recipe; refresh `RecipeListPanel` on dialog close in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T020 [US1] Wire "Delete Recipe" button in `MainWindow` to show `JOptionPane.showConfirmDialog`; on confirm call `RecipeService.deleteRecipe(id)` and refresh `RecipeListPanel` in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T021 [US1] Update `Main.java` with `main()` method: call `RecipeStore.load()`, construct `RecipeService`, launch `MainWindow` via `SwingUtilities.invokeLater()`; set `JFrame.EXIT_ON_CLOSE` in `src/com/cookingrecipes/Main.java`

**Checkpoint**: User Story 1 complete — create, edit, delete, and persist recipes all work
independently. Run quickstart.md Scenarios 1 and 5 to validate.

---

## Phase 4: User Story 2 — Browse and Filter by Tag (Priority: P2)

**Goal**: A tag filter panel lists all known tags; selecting one narrows the recipe list
instantly; clearing it restores all recipes.

**Independent Test**: Create 3 recipes with different tags; click a tag in the filter panel;
verify only tagged recipes appear; click "All" to restore full list.

- [x] T022 [P] [US2] Create `TagFilterPanel.java` (extends `JPanel`): `JList<String>` with "All" as first entry followed by alphabetically sorted tag names from `RecipeService.getAllTags()`; exposes `setTags(List<Tag>)` refresh method and `addFilterListener()` in `src/com/cookingrecipes/ui/TagFilterPanel.java`
- [x] T023 [US2] Wire `TagFilterPanel` into `MainWindow` left pane (replace placeholder); connect selection event to filter `RecipeListPanel` via `RecipeService.getRecipesByTag()` (or `getAllRecipes()` when "All" selected) in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T024 [US2] Refresh `TagFilterPanel` after every create, edit, or delete operation in `MainWindow` so newly created tags appear immediately in `src/com/cookingrecipes/ui/MainWindow.java`

**Checkpoint**: User Story 2 complete — tag filtering works alongside all US1 operations.
Run quickstart.md Scenario 2 to validate.

---

## Phase 5: User Story 3 — Search Recipes (Priority: P3)

**Goal**: A search bar filters the recipe list by a free-text query matched case-insensitively
across name, description, ingredient names, and instructions; combined with an active tag filter.

**Independent Test**: Add a recipe with a unique word only in its instructions; type that word
in the search bar; verify the recipe appears; combine with a tag filter.

- [x] T025 [P] [US3] Create `SearchBar.java` (extends `JPanel`): `JTextField` with placeholder text "Search recipes…"; fires a search action on Enter key and on a "Search" button click; exposes `addSearchListener(Consumer<String>)` in `src/com/cookingrecipes/ui/SearchBar.java`
- [x] T026 [US3] Implement `RecipeService.searchRecipes(String query)`: returns all recipes where `query` (trimmed, lowercased) appears in name, description, instructions, or any ingredient name; empty query returns all recipes in `src/com/cookingrecipes/service/RecipeService.java`
- [x] T027 [US3] Wire `SearchBar` into `MainWindow` above the recipe list; connect search event to refresh `RecipeListPanel` via `RecipeService.searchRecipes()` in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T028 [US3] Combine tag filter + search: `MainWindow` applies both conditions — first filter by selected tag (or all), then apply search query within that result set in `src/com/cookingrecipes/ui/MainWindow.java`

**Checkpoint**: User Story 3 complete — search works independently and combined with tag filter.
Run quickstart.md Scenario 3 to validate.

---

## Phase 6: User Story 4 — Annotate a Recipe (Priority: P4)

**Goal**: Selecting a recipe shows its full detail including all annotations in chronological
order; user can add a new annotation that persists with a timestamp.

**Independent Test**: Select a recipe, add annotation, close app, reopen, select recipe — annotation present with correct timestamp.

- [x] T029 [P] [US4] Create `RecipeDetailPanel.java` (extends `JPanel`): read-only display of all recipe fields (name, description, serves, instructions, ingredients as a table, tags as a comma list); scrollable `JTextArea` or `JList` showing annotations (text + timestamp); "Add Annotation" button in `src/com/cookingrecipes/ui/RecipeDetailPanel.java`
- [x] T030 [US4] Wire `RecipeListPanel` selection listener in `MainWindow` to call `RecipeDetailPanel.setRecipe(Recipe)` so the detail panel updates whenever a different recipe is selected in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T031 [US4] Implement "Add Annotation" button in `RecipeDetailPanel`: show `JOptionPane.showInputDialog` for text; call `RecipeService.addAnnotation(id, text)` with `LocalDateTime.now()` timestamp; refresh detail panel in `src/com/cookingrecipes/ui/RecipeDetailPanel.java`
- [x] T032 [US4] Integrate `RecipeDetailPanel` into `MainWindow` layout (right pane of the main `JSplitPane`; or horizontal split below `RecipeListPanel`); ensure it is visible and scrollable in `src/com/cookingrecipes/ui/MainWindow.java`

**Checkpoint**: User Story 4 complete — recipe detail view and annotations work end-to-end.
Run quickstart.md Scenario 4 to validate.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Small improvements that apply across all user stories.

- [x] T033 [P] Add empty-state label "No recipes found." in `RecipeListPanel` when the displayed list is empty (no recipes exist, or no search/filter match) in `src/com/cookingrecipes/ui/RecipeListPanel.java`
- [x] T034 [P] Disable "Edit Recipe" and "Delete Recipe" buttons in `MainWindow` when no recipe is selected in the list; re-enable on selection in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T035 [P] Disable "Add Annotation" button in `RecipeDetailPanel` when no recipe is displayed in `src/com/cookingrecipes/ui/RecipeDetailPanel.java`
- [x] T036 [P] Clear `RecipeDetailPanel` when the selected recipe is deleted (set to empty/null state) in `src/com/cookingrecipes/ui/MainWindow.java`
- [x] T037 Run all five quickstart.md manual validation scenarios; fix any regressions or UI inconsistencies found

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — **BLOCKS all user stories**.
- **US1 (Phase 3)**: Depends on Foundational. No dependency on US2/US3/US4.
- **US2 (Phase 4)**: Depends on Foundational + US1 (reuses `RecipeListPanel`, `MainWindow`).
- **US3 (Phase 5)**: Depends on Foundational + US1. May run in parallel with US2 after US1.
- **US4 (Phase 6)**: Depends on Foundational + US1. May run in parallel with US2/US3 after US1.
- **Polish (Phase 7)**: Depends on all user stories being complete.

### User Story Dependencies

| Story | Depends on | Can parallelize with |
|-------|-----------|----------------------|
| US1 (P1) | Foundational | — |
| US2 (P2) | US1 (shares MainWindow) | US3, US4 after US1 done |
| US3 (P3) | US1 (shares MainWindow) | US2, US4 after US1 done |
| US4 (P4) | US1 (shares MainWindow) | US2, US3 after US1 done |

### Within Each User Story

- All tasks marked `[P]` within a story can start in parallel.
- Non-`[P]` UI wiring tasks depend on the component tasks completing first.
- Patterns per story: **Create component(s) `[P]`** → **Wire into MainWindow** → **Validate independently**.

### Parallel Opportunities

```bash
# Phase 2: all model POJOs in parallel
T006 Recipe.java  &  T007 Ingredient.java  &  T008 Tag.java  &  T009 Annotation.java

# Phase 3: form dialog and list panel in parallel
T013 RecipeFormDialog.java  &  T014 RecipeListPanel.java

# After US1: US2, US3, US4 can all proceed in parallel
T022 TagFilterPanel.java  &  T025 SearchBar.java  &  T029 RecipeDetailPanel.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational — CRITICAL, blocks everything
3. Complete Phase 3: User Story 1 (T013–T021)
4. **STOP and VALIDATE**: Run quickstart.md Scenarios 1 and 5
5. App is usable: recipes can be created, edited, deleted, and persist

### Incremental Delivery

1. Setup + Foundational → project compiles and runs (empty window)
2. US1 → MVP: create, edit, delete recipes ✅
3. US2 → add tag filtering ✅
4. US3 → add full-text search ✅
5. US4 → add annotations ✅
6. Polish → production-ready

---

## Notes

- `[P]` = task creates or modifies a different file from other `[P]` tasks in the same phase — safe to run concurrently.
- `[USn]` label maps every task to exactly one user story for traceability.
- Each story phase ends with a Checkpoint: verify that story works independently before moving to the next.
- No external libraries — all Java SE 11 standard library.
- `RecipeService` is the single integration point; UI panels never access `RecipeStore` directly.
- Run `quickstart.md` Scenario 5 (Edit + Delete regression) after each story phase to catch regressions.
