## 1. Project Setup

- [x] 1.1 Create Eclipse project structure: `src/`, `lib/`, `resources/` folders and `.project` / `.classpath` files
- [x] 1.2 Download `org.json` JAR and place it in `lib/`; add it to `.classpath`
- [x] 1.3 Create root package `com.recipes` with sub-packages `model`, `persistence`, `service`, `ui`

## 2. Data Model

- [x] 2.1 Implement `Annotation` class (id, text, createdAt) with validation and factory method
- [x] 2.2 Implement `Recipe` class (id, name, description, ingredients, servings, instructions, tags, annotations) with validation
- [x] 2.3 Add `equals`/`hashCode` to both model classes based on `id`

## 3. JSON Persistence

- [x] 3.1 Implement `RecipeSerializer`: `Recipe → JSONObject` and `JSONObject → Recipe` using org.json
- [x] 3.2 Implement `RecipeStore.save(Recipe)`: write `<uuid>.json` to the recipes directory
- [x] 3.3 Implement `RecipeStore.loadAll()`: scan directory, deserialize each `.json` file, skip and warn on malformed files
- [x] 3.4 Implement `RecipeStore.delete(String id)`: remove `<uuid>.json` from disk
- [x] 3.5 Implement recipes directory resolution (default `<user.home>/recipes/`) and auto-creation on first use

## 4. Service Layer

- [x] 4.1 Implement `RecipeService`: wraps `RecipeStore`, holds in-memory `List<Recipe>`, exposes add/update/delete
- [x] 4.2 Implement `RecipeService.search(String query, Set<String> tags)`: in-memory filter by text (name, description, ingredients, tags) and tag set (AND semantics), both case-insensitive
- [x] 4.3 Implement `RecipeService.getAllTags()`: aggregate unique tags across all loaded recipes (stored lowercase)
- [x] 4.4 Enforce tag lowercase normalization in `RecipeService.save()`

## 5. Main Window & Layout

- [x] 5.1 Create `MainFrame` (JFrame): define three-panel layout — tag/filter panel (left), recipe list panel (center-left), detail/editor+annotations panel (right) using `JSplitPane`
- [x] 5.2 Wire menu bar with "New Recipe" and "File > Exit" actions
- [x] 5.3 Add status bar at the bottom showing recipe count ("X of Y recipes")

## 6. Tag Filter Panel

- [x] 6.1 Implement `TagFilterPanel` (JPanel): `JCheckBox` list of all known tags; "Clear" button
- [x] 6.2 Fire filter-changed event to `MainFrame` when tag selection changes
- [x] 6.3 Refresh tag list when `RecipeService.getAllTags()` changes (after save/delete)

## 7. Recipe List Panel

- [x] 7.1 Implement `RecipeListPanel`: `JList<Recipe>` with custom cell renderer showing name and tags
- [x] 7.2 Bind list selection to show recipe detail in the detail panel
- [x] 7.3 Implement `refresh(List<Recipe>)` method to repopulate the list and update the count label

## 8. Search Bar

- [x] 8.1 Add `JTextField` search bar above the recipe list
- [x] 8.2 Attach `DocumentListener` to trigger `RecipeService.search()` on every keystroke
- [x] 8.3 Show "No recipes found" message in the list panel when results are empty

## 9. Recipe Detail & Editor Panel

- [x] 9.1 Implement `RecipeDetailPanel` (read-only view): renders name, description, tags, servings, ingredients (bulleted), instructions (numbered)
- [x] 9.2 Implement `RecipeEditorPanel` (editable form): `JTextField` for name, `JTextArea` for description, `JSpinner` for servings, list editors for ingredients/instructions, tag input field
- [x] 9.3 Add "Edit" button on detail view that switches to editor panel (pre-populated)
- [x] 9.4 Add "Save" and "Cancel" buttons on editor panel; validate mandatory fields and show inline error labels
- [x] 9.5 On save, call `RecipeService.save()`, refresh list, switch back to detail view
- [x] 9.6 On cancel, switch back to detail view without changes
- [x] 9.7 Add "Delete" button with `JOptionPane` confirmation dialog; call `RecipeService.delete()` on confirm and refresh list

## 10. Annotations Panel

- [x] 10.1 Implement `AnnotationsPanel` (JPanel): scrollable list of annotation entries (text + timestamp); "No annotations" placeholder when empty
- [x] 10.2 Add "Add Annotation" button that opens an inline text area + "Submit" / "Cancel"
- [x] 10.3 Validate non-empty text before saving; show inline error on empty submission
- [x] 10.4 Add edit (pencil icon or button) per annotation entry: replace text with editable field, save on confirm
- [x] 10.5 Add delete (X button) per annotation entry with confirmation; remove from recipe and persist

## 11. Integration & Polish

- [x] 11.1 Wire all panels together in `MainFrame`: search + tag filter both feed `RecipeService.search()` and refresh `RecipeListPanel`
- [x] 11.2 Ensure selecting a recipe updates both `RecipeDetailPanel` and `AnnotationsPanel`
- [x] 11.3 Handle startup load errors: show a non-blocking warning dialog listing skipped malformed files
- [x] 11.4 Create `Main` class with `main(String[] args)` that launches `MainFrame` on the Swing EDT
- [x] 11.5 Test end-to-end: create recipe with tags, search, filter, annotate, edit, delete
