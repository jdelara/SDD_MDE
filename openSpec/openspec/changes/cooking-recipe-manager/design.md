## Context

A standalone desktop application for managing personal cooking recipes. The target runtime is Java 11+ on Windows/macOS/Linux with no build tooling required beyond a standard Eclipse project (`.classpath` / `.project`). Persistence is JSON files on the local filesystem. There is a single user; no concurrency, no networking, no authentication.

## Goals / Non-Goals

**Goals:**
- Eclipse-importable Java project buildable with the IDE's built-in compiler
- Swing UI with a recipe list panel, a search/filter bar, a detail/editor panel, and an annotations panel
- JSON persistence: one JSON file per recipe stored in a configurable directory (default: `<user.home>/recipes/`)
- Extensible tag system: tags are just strings; no predefined vocabulary
- Flexible search across name, description, ingredients, and tags
- Minimal external dependencies: one small JSON library bundled as a JAR in `lib/`

**Non-Goals:**
- Multi-user or networked access
- Image/media attachment for recipes
- Import/export to third-party formats (e.g., Paprika, Cookpad)
- Printing or PDF generation
- Undo/redo history
- Cloud sync

## Decisions

### D1: Single JSON file per recipe
Each recipe is serialized to its own `<uuid>.json` file inside the recipes directory.

- **Why**: Simplifies add/delete (no need to rewrite a monolithic file); makes manual inspection easy; allows the directory to grow without performance cliffs.
- **Alternative considered**: One `recipes.json` array — rejected because concurrent writes (even accidental) would corrupt all data and file size grows unbounded.

### D2: JSON library — org.json (single JAR, ~350 KB)
`org.json` (`org.json:json`) is bundled in `lib/org.json.jar`.

- **Why**: Zero transitive dependencies; well-known API; small footprint; no annotation processing or code generation.
- **Alternative considered**: Gson — similar size but requires Gson dependency chain; Jackson — too large and complex for this use case.

### D3: MVC-style Swing architecture
Three layers: **Model** (POJOs + persistence), **Controller** (application logic / service layer), **View** (Swing panels).

- `RecipeStore` owns all I/O; Swing panels never touch the filesystem directly.
- `RecipeController` mediates between `RecipeStore` and the UI.
- **Why**: Keeps Swing event-dispatch concerns away from persistence logic, making each layer independently testable.

### D4: Search implemented in-memory
On startup, all recipes are loaded into a `List<Recipe>` in memory. Search filters this list using simple `String.contains` / tag-set intersection.

- **Why**: Simplicity; for personal recipe collections (tens to low hundreds of recipes), in-memory search is instant. No index infrastructure needed.
- **Alternative considered**: SQLite FTS — rejected (requires database dependency, violates constraints).

### D5: Eclipse project structure (no Maven/Gradle)
```
cooking-recipe-manager/
  src/              Java source (package: com.recipes)
  lib/              org.json.jar
  resources/        icons, default config
  .classpath        references lib/org.json.jar
  .project
```

- **Why**: User explicitly requires a standard Eclipse project with minimal setup — no build tool invocation.

### D6: Tag storage as a JSON array of strings on each recipe
Tags are stored as `["vegetarian", "vegan"]` on the recipe JSON object. No separate tag registry file.

- **Why**: Self-contained per recipe; tags are discovered at load time by aggregating across all recipes. Adding a new tag is just typing it in the editor — no schema migration required.

## Risks / Trade-offs

- **Large recipe collections (1000+)** → loading all into memory and linear search may slow startup. Mitigation: acceptable for personal use; can add lazy loading later if needed.
- **Concurrent process writes** → two JVM instances opening the same recipe directory could corrupt files. Mitigation: application is mono-user; document that only one instance should run at a time (no file locking implemented).
- **org.json license (MIT)** → compatible with personal use; no issues.
- **No input validation on JSON files** → hand-edited JSON could cause parse errors on load. Mitigation: catch and log malformed files, skip them, surface a warning in the UI.
