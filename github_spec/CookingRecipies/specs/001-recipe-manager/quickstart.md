# Quickstart & Validation Guide: Cooking Recipe Manager

**Branch**: `001-recipe-manager` | **Date**: 2026-06-05

This guide describes how to build, run, and manually validate the application against each
user story. No automated tests are required — each scenario is verified by direct interaction
with the Swing UI.

---

## Prerequisites

- Eclipse IDE for Java Developers (any version supporting Java 11+)
- JDK 11 or later configured as the Eclipse workspace JDK
- The `CookingRecipies` project imported into Eclipse

---

## Build & Run

1. Open Eclipse.
2. Import the project: **File → Open Projects from File System** → select the `CookingRecipies`
   folder.
3. Verify the project compiles with zero errors (**Project → Build All**).
4. Run the application: right-click `Main.java` → **Run As → Java Application**.
5. The main window appears. On first run, `data/recipes.json` is auto-created (empty store).

---

## Validation Scenario 1: Create a Recipe (P1 — MVP)

**Goal**: Verify a new recipe can be created and persists.

Steps:
1. Click **New Recipe**.
2. Enter "Tomato Soup" as the name.
3. Enter "A hearty winter soup." as the description.
4. Set serves to `4`.
5. Add two ingredients: "tomatoes (4, medium)" and "olive oil (2, tbsp)".
6. Enter instructions: "Dice tomatoes. Simmer 20 minutes. Blend."
7. Add tags "vegetarian" and "quick" (type new tag names in the tag field).
8. Click **Save**.

Expected:
- "Tomato Soup" appears in the recipe list with tags "vegetarian" and "quick" visible.
- Selecting the recipe in the list shows all entered fields in the detail panel.
- Close and reopen the application — "Tomato Soup" is still present (JSON persistence).

**Edge case — missing name**:
- Click **New Recipe**, leave the name blank, click **Save**.
- Expected: the dialog highlights the name field with an error; the recipe is NOT saved.

---

## Validation Scenario 2: Browse and Filter by Tag (P2)

**Goal**: Verify tag filtering narrows the recipe list correctly.

Prerequisites: at least 3 recipes exist — 2 tagged "vegetarian", 1 tagged "vegan" only.

Steps:
1. Confirm all 3 recipes appear in the list when no filter is active.
2. Click "vegetarian" in the tag filter panel (left pane).
3. Observe the list updates immediately.

Expected:
- Only the 2 vegetarian recipes are shown.
- The 1 vegan-only recipe is hidden.

Steps (clear filter):
4. Click "All" (or deselect the tag) in the tag filter panel.

Expected:
- All 3 recipes are shown again.

---

## Validation Scenario 3: Search Recipes (P3)

**Goal**: Verify full-text search works across all fields.

Prerequisites: the recipes from Scenario 2 exist. One recipe contains "lentils" only in its
instructions field.

Steps:
1. Type "lentils" in the search bar and confirm (press Enter or click Search).

Expected:
- All recipes containing "lentils" anywhere (name, description, ingredients, instructions) are
  returned.
- Recipes with no mention of "lentils" are hidden.

Steps (combined filter + search):
2. Select tag "vegetarian" in the tag filter panel, then type "tomato" in the search bar.

Expected:
- Only recipes that are both tagged "vegetarian" AND contain "tomato" somewhere are shown.

Steps (empty search):
3. Clear the search bar and search again.

Expected:
- The list reverts to showing all recipes (respecting any active tag filter).

---

## Validation Scenario 4: Annotate a Recipe (P4)

**Goal**: Verify annotations are saved with timestamps and persist.

Prerequisites: "Tomato Soup" exists from Scenario 1.

Steps:
1. Select "Tomato Soup" in the recipe list.
2. In the detail panel, click **Add Annotation**.
3. Enter "Reduced salt by half — much better." and confirm.

Expected:
- The annotation appears in the detail panel with today's date/time.

Steps (persistence):
4. Close and reopen the application.
5. Select "Tomato Soup".

Expected:
- The annotation is still present with its original timestamp.

Steps (multiple annotations):
6. Add a second annotation: "Also tried with basil — excellent."

Expected:
- Both annotations appear in chronological order (first annotation above second).

---

## Validation Scenario 5: Edit and Delete (Regression)

**Goal**: Verify editing does not corrupt data; delete removes the recipe.

Steps — edit:
1. Select "Tomato Soup", click **Edit Recipe**.
2. Change serves from `4` to `6` and add a new ingredient "basil (1, handful)".
3. Save.

Expected:
- Detail panel reflects the updated serves count and new ingredient.
- Annotations are preserved after edit.
- JSON file on disk reflects the changes.

Steps — delete:
4. Select a recipe, click **Delete Recipe**, confirm the deletion prompt.

Expected:
- Recipe is removed from the list and no longer appears after application restart.
- The tags that recipe used remain in the tag filter panel (tags are not deleted with recipes).

---

## JSON File Inspection

After any save operation, open `data/recipes.json` in a text editor to confirm:
- The file is valid, human-readable JSON matching the schema in
  [contracts/json-schema.md](contracts/json-schema.md).
- All saved recipes, ingredients, tags, and annotations are present.
- No corruption or truncation is visible.
