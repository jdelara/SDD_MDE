# Data Model: Cooking Recipe Manager

**Branch**: `001-recipe-manager` | **Date**: 2026-06-05

## Entities

### Recipe

The central entity. Represents one cooking recipe owned by the single user.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `id` | String (UUID) | Yes | Auto-generated; immutable after creation |
| `name` | String | Yes | Non-empty; max 200 characters |
| `description` | String | No | Free text; may be empty |
| `serves` | int | No | ≥ 1 when provided; 0 means "not specified" |
| `instructions` | String | No | Free text; ordered steps as a single block |
| `tags` | List\<Tag\> | No | May be empty; no duplicates (case-insensitive) |
| `ingredients` | List\<Ingredient\> | No | Ordered; may be empty |
| `annotations` | List\<Annotation\> | No | Ordered chronologically (oldest first) |

**Validation rules**:
- `name` MUST NOT be blank (whitespace-only names are rejected).
- `id` is assigned on creation by `RecipeService` and never changed.
- Tag names within a recipe MUST be unique (case-insensitive comparison).

---

### Ingredient

An item in a recipe's ingredient list.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | String | Yes | Non-empty |
| `quantity` | String | No | Free text (e.g., "2", "1/2", "a handful"); empty string if not specified |
| `unit` | String | No | Free text (e.g., "cups", "g", "tbsp"); empty string if not specified |

**Validation rules**:
- `name` MUST NOT be blank.
- `quantity` and `unit` are optional and independently nullable.

---

### Tag

A user-defined category label that can be assigned to zero or more recipes.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | String | Yes | Non-empty; unique across all tags (case-insensitive) |

**Validation rules**:
- Tag names are stored in their original casing but compared case-insensitively for uniqueness.
- A tag is created implicitly when first assigned to a recipe.
- A tag is never deleted automatically; it persists in the global tag list even when no recipe
  carries it (simplifies the tag filter panel).

**Global tag list**: `RecipeStore` maintains a deduplicated list of all tags ever created, used
to populate the tag filter panel. Tags are sorted alphabetically (case-insensitive) in the UI.

---

### Annotation

A personal note attached to a recipe, with an automatic creation timestamp.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `text` | String | Yes | Non-empty |
| `timestamp` | String (ISO-8601) | Yes | Auto-set at creation; immutable; format `YYYY-MM-DDTHH:mm:ss` |

**Validation rules**:
- `text` MUST NOT be blank.
- `timestamp` is set by `RecipeService` using `java.time.LocalDateTime.now()` at the moment
  the annotation is created; it cannot be changed.
- Annotations are appended to the recipe's list; the list is always in insertion order.

---

## Relationships

```text
RecipeStore (singleton)
  └── tags: List<Tag>        ← global tag registry
  └── recipes: List<Recipe>  ← all recipes

Recipe
  ├── tags: List<Tag>         ← references names from global registry
  ├── ingredients: List<Ingredient>
  └── annotations: List<Annotation>
```

- Tags are referenced by name (string), not by object identity. The global tag list and each
  recipe's tag list both hold `Tag` objects whose `name` is the key.
- No foreign key constraints: this is a flat in-memory model persisted to JSON.

---

## State Transitions

### Recipe lifecycle

```
[not exists] --create--> [exists] --edit--> [exists]
                                 --delete-> [not exists]
                                 --annotate-> [exists + new Annotation]
```

### Annotation lifecycle

```
[not exists] --add--> [exists]   ← no edit or delete in v1
```

---

## Java Class Mapping

| Entity | Java Class | Package |
|--------|------------|---------|
| Recipe | `Recipe.java` | `com.cookingrecipes.model` |
| Ingredient | `Ingredient.java` | `com.cookingrecipes.model` |
| Tag | `Tag.java` | `com.cookingrecipes.model` |
| Annotation | `Annotation.java` | `com.cookingrecipes.model` |
| All recipes + tags | `RecipeStore.java` | `com.cookingrecipes.storage` |

All model classes are plain Java objects (no annotations, no reflection, no framework).
`RecipeStore` holds the in-memory state and delegates serialization to `JsonSerializer`.
