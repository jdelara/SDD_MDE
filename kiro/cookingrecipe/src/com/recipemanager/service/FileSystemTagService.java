package com.recipemanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.recipemanager.model.Recipe;
import com.recipemanager.util.ErrorLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Filesystem-based implementation of TagService.
 * Persists tags to {user.home}/.recipe-manager/tags.json.
 * On first use when tags.json doesn't exist, creates it with default tags.
 */
public class FileSystemTagService implements TagService {

    private static final List<String> DEFAULT_TAGS = Arrays.asList(
            "vegetarian", "vegan", "dessert", "main course", "appetizer"
    );

    private final Path tagsFile;
    private final Gson gson;
    private final RecipeStore recipeStore;
    private List<String> tags;

    /**
     * Creates a FileSystemTagService using the default tags file location:
     * {user.home}/.recipe-manager/tags.json
     *
     * @param recipeStore the recipe store used for cascading tag deletions to recipes
     */
    public FileSystemTagService(RecipeStore recipeStore) {
        this(Paths.get(System.getProperty("user.home"), ".recipe-manager", "tags.json"), recipeStore);
    }

    /**
     * Creates a FileSystemTagService using the specified tags file path.
     * Useful for testing with @TempDir.
     *
     * @param tagsFile the path to the tags JSON file
     * @param recipeStore the recipe store used for cascading tag deletions to recipes
     */
    public FileSystemTagService(Path tagsFile, RecipeStore recipeStore) {
        this.tagsFile = tagsFile;
        this.recipeStore = recipeStore;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.tags = null; // lazy-loaded
    }

    @Override
    public List<String> getAllTags() {
        ensureLoaded();
        return new ArrayList<>(tags);
    }

    @Override
    public void createTag(String tagName) throws DuplicateTagException {
        ensureLoaded();

        // Case-insensitive duplicate detection
        for (String existing : tags) {
            if (existing.equalsIgnoreCase(tagName)) {
                throw new DuplicateTagException("Tag already exists: " + tagName);
            }
        }

        tags.add(tagName);
        persist();
    }

    @Override
    public void deleteTag(String tagName) {
        ensureLoaded();

        // Remove from tags list (case-insensitive match)
        tags.removeIf(t -> t.equalsIgnoreCase(tagName));
        persist();

        // Cascade: remove tag from all recipes
        List<Recipe> allRecipes = recipeStore.loadAll();
        for (Recipe recipe : allRecipes) {
            List<String> recipeTags = recipe.getTags();
            boolean removed = recipeTags.removeIf(t -> t.equalsIgnoreCase(tagName));
            if (removed) {
                try {
                    recipeStore.save(recipe);
                } catch (PersistenceException e) {
                    ErrorLogger.error("Failed to update recipe after tag deletion: " + recipe.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void assignTag(UUID recipeId, String tagName) {
        List<Recipe> allRecipes = recipeStore.loadAll();
        for (Recipe recipe : allRecipes) {
            if (recipe.getId().equals(recipeId)) {
                List<String> recipeTags = recipe.getTags();
                // Only add if not already present (case-insensitive)
                boolean alreadyAssigned = recipeTags.stream()
                        .anyMatch(t -> t.equalsIgnoreCase(tagName));
                if (!alreadyAssigned) {
                    recipeTags.add(tagName);
                    try {
                        recipeStore.save(recipe);
                    } catch (PersistenceException e) {
                        ErrorLogger.error("Failed to assign tag to recipe: " + recipe.getName() + " - " + e.getMessage());
                    }
                }
                break;
            }
        }
    }

    @Override
    public void removeTag(UUID recipeId, String tagName) {
        List<Recipe> allRecipes = recipeStore.loadAll();
        for (Recipe recipe : allRecipes) {
            if (recipe.getId().equals(recipeId)) {
                List<String> recipeTags = recipe.getTags();
                boolean removed = recipeTags.removeIf(t -> t.equalsIgnoreCase(tagName));
                if (removed) {
                    try {
                        recipeStore.save(recipe);
                    } catch (PersistenceException e) {
                        ErrorLogger.error("Failed to remove tag from recipe: " + recipe.getName() + " - " + e.getMessage());
                    }
                }
                break;
            }
        }
    }

    /**
     * Ensures tags are loaded from disk. If the file doesn't exist,
     * creates it with default tags.
     */
    private void ensureLoaded() {
        if (tags != null) {
            return;
        }

        if (Files.exists(tagsFile)) {
            try {
                String json = Files.readString(tagsFile);
                TagsData data = gson.fromJson(json, TagsData.class);
                if (data != null && data.tags != null) {
                    tags = new ArrayList<>(data.tags);
                } else {
                    // Malformed file - recreate with defaults
                    tags = new ArrayList<>(DEFAULT_TAGS);
                    persist();
                }
            } catch (IOException | JsonParseException e) {
                ErrorLogger.warn("Failed to read tags file, recreating with defaults: " + e.getMessage());
                tags = new ArrayList<>(DEFAULT_TAGS);
                persist();
            }
        } else {
            // First use - create with defaults
            tags = new ArrayList<>(DEFAULT_TAGS);
            persist();
        }
    }

    /**
     * Persists current tags list to disk.
     */
    private void persist() {
        try {
            Path parent = tagsFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            TagsData data = new TagsData();
            data.tags = new ArrayList<>(tags);
            String json = gson.toJson(data);
            Files.writeString(tagsFile, json);
        } catch (IOException e) {
            ErrorLogger.error("Failed to persist tags: " + e.getMessage());
        }
    }

    /**
     * Internal data class for JSON serialization of tags.
     */
    private static class TagsData {
        List<String> tags;
    }
}
