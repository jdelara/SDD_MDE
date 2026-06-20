# Research: Cooking Recipe Manager

**Branch**: `001-recipe-manager` | **Date**: 2026-06-05

## Decision 1: JSON Serialization Without External Libraries

**Decision**: Implement a custom JSON serializer/deserializer using only `java.io` and
`java.lang`.

**Rationale**: The constitution (Principle I) mandates no external runtime dependencies. Java SE
11 provides no built-in JSON parser (`javax.json` is part of Java EE / Jakarta EE, not Java SE).
For this application's data model — shallow nesting, no polymorphism, no circular references — a
custom serializer is straightforward to implement correctly. The total recipe data structure is 4
entity types with a maximum depth of 3 levels (Recipe → Ingredient/Annotation fields).

**Alternatives considered**:
- **Gson (Google)**: Would require adding a JAR to the Eclipse classpath manually, violating
  Principle I. Rejected to keep the project constitutionally clean.
- **Jackson Databind**: Same issue as Gson — external JAR required. Rejected.
- **Java object serialization (`ObjectOutputStream`)**: Binary format, not human-readable.
  Rejected because JSON readability is an explicit user requirement.
- **XML via JAXP** (built into Java SE): Would satisfy the no-external-library constraint but
  produces verbose files that are harder to inspect manually. Rejected in favor of JSON as
  explicitly requested by the user.

---

## Decision 2: Single JSON File vs. One File Per Recipe

**Decision**: Store all recipes in a single `data/recipes.json` file.

**Rationale**: The application targets up to 500 recipes for a single user. A single file
simplifies backup, portability, and the serializer implementation. Loading 500 short recipes
at startup is well within Java's I/O performance envelope (typically < 50ms). Atomic writes
(write to a temp file, rename) ensure data integrity on save.

**Alternatives considered**:
- **One JSON file per recipe**: Simplifies individual saves but complicates list loading,
  search, and tag enumeration. Rejected for added complexity with no benefit at this scale.
- **SQLite via JDBC**: Would require a native library, violating Principle I. Rejected.

---

## Decision 3: Swing Layout Architecture

**Decision**: Three-pane layout using `JSplitPane` — a vertical split between a narrow left tag
panel and a main area, with the main area split horizontally between the recipe list (top) and
the recipe detail view (bottom). A search bar lives above the split pane.

**Rationale**: This mirrors established desktop recipe/note-taking application conventions.
The layout is achievable with standard `JSplitPane`, `JPanel`, `JList`, `JScrollPane`, and
`JTextField` — all from `javax.swing`. No custom painting or look-and-feel customization
required.

**Alternatives considered**:
- **Tabbed pane (JTabbedPane)**: Hides the recipe list while viewing detail. Rejected as it
  reduces discoverability.
- **Card layout**: Requires navigation buttons; overkill for this scope. Rejected.

---

## Decision 4: In-Memory Model + Save-on-Mutation Strategy

**Decision**: `RecipeStore` loads all recipes into memory at startup and saves the full JSON
file after every mutating operation (create, edit, delete, add annotation).

**Rationale**: For up to 500 recipes the entire dataset fits comfortably in memory (< 5MB).
Save-on-mutation keeps the code simple — no dirty-state tracking, no background flush thread,
no risk of data loss on unexpected close.

**Alternatives considered**:
- **Lazy load + save-on-close**: Risks data loss if the application crashes before closing.
  Rejected.
- **Background autosave thread**: Adds concurrency complexity. Rejected (YAGNI — Principle III).

---

## Decision 5: Full-Text Search Implementation

**Decision**: Case-insensitive `String.contains()` search across all text fields of each recipe
in memory (name, description, ingredients, instructions).

**Rationale**: For 500 recipes this linear scan takes < 10ms — well under the 1-second
requirement. No search index, no tokenization library needed. Simple to implement and test.

**Alternatives considered**:
- **Lucene or similar**: External dependency. Rejected (Principle I).
- **Regular expressions**: Unnecessary power for a simple contains-check. Rejected.

---

## Decision 6: Unique Recipe Identifiers

**Decision**: Generate UUIDs using `java.util.UUID.randomUUID()` for each recipe.

**Rationale**: UUIDs are guaranteed unique without a central counter, survive copy-paste of the
JSON file, and are built into Java SE. They serve as stable keys for edit and delete operations.

**Alternatives considered**:
- **Sequential integer IDs**: Require a counter persisted in the JSON file; can collide if
  files are merged. Rejected.
- **Recipe name as key**: Names can be edited; not a stable identifier. Rejected.
