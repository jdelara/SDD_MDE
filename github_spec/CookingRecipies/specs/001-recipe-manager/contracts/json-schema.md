# JSON Storage Contract: Cooking Recipe Manager

**File**: `data/recipes.json` (relative to Eclipse project root)
**Format**: UTF-8 encoded JSON
**Created**: Auto-created on first run if it does not exist
**Written by**: `com.cookingrecipes.storage.JsonSerializer`

---

## Top-Level Structure

```json
{
  "tags": [ ... ],
  "recipes": [ ... ]
}
```

| Key | Type | Description |
|-----|------|-------------|
| `tags` | Array of Tag objects | Global tag registry; all tags ever created |
| `recipes` | Array of Recipe objects | All saved recipes |

---

## Tag Object

```json
{
  "name": "vegetarian"
}
```

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `name` | String | No | Case preserved; unique case-insensitively across the array |

---

## Recipe Object

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tomato Soup",
  "description": "A hearty winter soup.",
  "serves": 4,
  "instructions": "1. Dice tomatoes.\n2. Simmer for 20 minutes.\n3. Blend and season.",
  "tags": ["vegetarian", "quick"],
  "ingredients": [ ... ],
  "annotations": [ ... ]
}
```

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `id` | String | No | UUID v4; immutable |
| `name` | String | No | Non-empty |
| `description` | String | Yes | Empty string `""` when not provided |
| `serves` | Number (int) | No | `0` means "not specified"; otherwise ≥ 1 |
| `instructions` | String | Yes | Empty string `""` when not provided; newlines encoded as `\n` |
| `tags` | Array of String | No | Tag names; may be empty array `[]` |
| `ingredients` | Array of Ingredient objects | No | May be empty array `[]` |
| `annotations` | Array of Annotation objects | No | Chronological order; may be empty array `[]` |

---

## Ingredient Object

```json
{
  "name": "tomatoes",
  "quantity": "4",
  "unit": "medium"
}
```

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `name` | String | No | Non-empty |
| `quantity` | String | No | Empty string `""` when not specified |
| `unit` | String | No | Empty string `""` when not specified |

---

## Annotation Object

```json
{
  "text": "Reduced salt by half — much better result.",
  "timestamp": "2026-06-05T14:32:00"
}
```

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `text` | String | No | Non-empty |
| `timestamp` | String | No | ISO-8601 local datetime: `YYYY-MM-DDTHH:mm:ss`; set at creation; immutable |

---

## Complete Example

```json
{
  "tags": [
    { "name": "vegetarian" },
    { "name": "vegan" },
    { "name": "quick" }
  ],
  "recipes": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Tomato Soup",
      "description": "A hearty winter soup.",
      "serves": 4,
      "instructions": "1. Dice tomatoes.\n2. Simmer for 20 minutes.\n3. Blend and season.",
      "tags": ["vegetarian", "quick"],
      "ingredients": [
        { "name": "tomatoes",    "quantity": "4",   "unit": "medium" },
        { "name": "olive oil",   "quantity": "2",   "unit": "tbsp"   },
        { "name": "salt",        "quantity": "",    "unit": ""       }
      ],
      "annotations": [
        {
          "text": "Reduced salt by half — much better result.",
          "timestamp": "2026-06-05T14:32:00"
        }
      ]
    }
  ]
}
```

---

## Write Safety

- The file is written atomically: `JsonSerializer` writes to `data/recipes.tmp` then renames
  it to `data/recipes.json` using `java.nio.file.Files.move()` with
  `StandardCopyOption.REPLACE_EXISTING`. This prevents a partial write from corrupting stored data.
- If `data/` does not exist on startup, `RecipeStore` creates it before the first write.
- If `data/recipes.json` does not exist on startup, `RecipeStore` initializes an empty store
  (`{ "tags": [], "recipes": [] }`) in memory and creates the file on the first save.
