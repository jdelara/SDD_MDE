## Why

There is no existing application to manage cooking recipes locally in a simple desktop environment. A mono-user, offline-first Java/Swing application with JSON persistence fills this gap without requiring a database or internet connectivity.

## What Changes

- Introduce a new desktop application project (Eclipse/Java/Swing) for creating and managing cooking recipes
- Recipes are stored as JSON files on the local filesystem
- Users can tag recipes with extensible labels (e.g., vegetarian, vegan, gluten-free)
- Full-text and tag-based search across the recipe library
- Recipe annotations (user notes) can be added to any recipe
- No external database; no network dependencies

## Capabilities

### New Capabilities

- `recipe-model`: Core data model for a recipe — name, description, ingredients, servings (number of people), instructions, tags, and annotations
- `recipe-persistence`: JSON-based load/save of recipes to/from the local filesystem; single-file or per-recipe file strategy
- `recipe-list-view`: Swing UI panel listing all recipes, filterable/grouped by tags
- `recipe-search`: Flexible search across recipe name, description, ingredients, and tags
- `recipe-editor`: Swing UI form for creating and editing a recipe (all fields)
- `recipe-annotation`: Ability to add, edit, and delete free-text annotations on a recipe
- `tag-management`: Extensible tag system — define, assign, and remove tags; no fixed vocabulary

### Modified Capabilities

## Impact

- New Eclipse Java project with Swing UI; no Maven/Gradle required (plain Eclipse classpath)
- Single runtime dependency: a JSON library (minimal — e.g., org.json or a small bundled parser)
- All data persisted locally under a configurable directory (default: user home folder)
- No network calls, no database, no authentication
