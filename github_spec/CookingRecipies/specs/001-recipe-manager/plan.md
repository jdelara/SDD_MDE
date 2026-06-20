# Implementation Plan: Cooking Recipe Manager

**Branch**: `001-recipe-manager` | **Date**: 2026-06-05 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-recipe-manager/spec.md`

## Summary

A single-user Java Swing desktop application for creating and managing cooking recipes.
Recipes are stored in a local JSON file (`data/recipes.json`) using a custom serializer built
entirely on Java SE — no external libraries. The UI presents a tag-filtered recipe list, a
full-text search bar, and a detail/annotation panel, all within a single JFrame.

## Technical Context

**Language/Version**: Java 11 (Eclipse workspace default JDK)

**Primary Dependencies**: `javax.swing`, `java.awt` (JDK standard library only — no third-party
libraries)

**Storage**: Local JSON file (`data/recipes.json`) via a custom JSON serializer built on
`java.io` / `java.nio` — no external JSON library

**Testing**: Manual verification via Eclipse run configuration; quickstart.md scenarios

**Target Platform**: Windows desktop, Eclipse IDE for Java Developers

**Project Type**: Desktop GUI application (single Eclipse Java project)

**Performance Goals**: Recipe list renders in under 2 seconds for up to 500 recipes; search
returns results in under 1 second

**Constraints**: No network access; single user; no external runtime dependencies;
Eclipse project structure enforced by constitution

**Scale/Scope**: Single user, up to 500 recipes, one local JSON file

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Plain Java & Swing Only | ✅ PASS | UI exclusively uses `javax.swing` / `java.awt`; custom JSON serializer eliminates any external library dependency |
| II. Eclipse Project Structure | ✅ PASS | Standard `.project` / `.classpath`, `src/` → `bin/` layout; Eclipse built-in builder only |
| III. Simplicity First | ✅ PASS | JSON flat-file, no database, no DI container, no frameworks; single `Main.java` entry point |

*Post-design re-check (Phase 1)*: ✅ All principles remain satisfied after design. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-recipe-manager/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── json-schema.md   # JSON storage file format
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
CookingRecipies/                         ← Eclipse project root
├── .project                             ← Eclipse project descriptor
├── .classpath                           ← Eclipse classpath (src → bin)
├── src/
│   └── com/
│       └── cookingrecipes/
│           ├── Main.java                ← Entry point (main method)
│           ├── model/
│           │   ├── Recipe.java          ← Recipe POJO (id, name, desc, serves, instructions, tags, ingredients, annotations)
│           │   ├── Ingredient.java      ← Ingredient POJO (name, quantity, unit)
│           │   ├── Tag.java             ← Tag POJO (name)
│           │   └── Annotation.java      ← Annotation POJO (text, timestamp)
│           ├── storage/
│           │   ├── RecipeStore.java     ← Load/save data/recipes.json; manages in-memory state
│           │   └── JsonSerializer.java  ← Custom JSON read/write (no external library)
│           ├── service/
│           │   └── RecipeService.java   ← CRUD, full-text search, tag filtering
│           └── ui/
│               ├── MainWindow.java      ← Root JFrame; assembles panels; menu bar
│               ├── TagFilterPanel.java  ← Left pane: JList of all tags + "All" option
│               ├── RecipeListPanel.java ← Center-top pane: JList of filtered/searched recipes
│               ├── RecipeDetailPanel.java  ← Center-bottom pane: full recipe view + annotations
│               ├── SearchBar.java          ← Top bar: JTextField + search trigger
│               └── RecipeFormDialog.java   ← JDialog for create / edit recipe
├── bin/                                 ← Eclipse compiled output (add to .gitignore)
└── data/
    └── recipes.json                     ← Local JSON storage (auto-created on first run)
```

**Structure Decision**: Single Eclipse Java project, package root `com.cookingrecipes`. A
three-pane Swing layout (tag filter left | recipe list center-top | recipe detail center-bottom)
inside a single `JFrame`. Business logic isolated in `RecipeService`. Storage isolated behind
`RecipeStore` and `JsonSerializer`. Eclipse built-in builder; no Maven or Gradle.

## Complexity Tracking

> No constitution violations — all principles satisfied. This section is intentionally empty.
