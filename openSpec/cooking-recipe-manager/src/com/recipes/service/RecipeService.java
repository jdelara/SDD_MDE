package com.recipes.service;

import com.recipes.model.Recipe;
import com.recipes.persistence.RecipeStore;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeService {

    private final RecipeStore store;
    private final List<Recipe> recipes = new ArrayList<>();
    private List<String> loadWarnings = new ArrayList<>();

    public RecipeService(File recipesDir) {
        this.store = new RecipeStore(recipesDir);
        reload();
    }

    private void reload() {
        recipes.clear();
        recipes.addAll(store.loadAll());
        loadWarnings = store.getLoadWarnings();
    }

    /** Persist a recipe (new or existing) and update the in-memory list. */
    public void save(Recipe recipe) throws IOException {
        // Normalize tags to lowercase, deduplicated
        List<String> normalized = recipe.getTags().stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .map(t -> t.trim().toLowerCase())
                .distinct()
                .collect(Collectors.toList());
        recipe.setTags(normalized);

        store.save(recipe);
        int idx = indexById(recipe.getId());
        if (idx >= 0) {
            recipes.set(idx, recipe);
        } else {
            recipes.add(recipe);
        }
    }

    public void delete(String id) throws IOException {
        store.delete(id);
        recipes.removeIf(r -> r.getId().equals(id));
    }

    /**
     * Filter recipes by free-text query (name, description, ingredients, tags)
     * AND a required tag set (AND semantics). Both are case-insensitive.
     */
    public List<Recipe> search(String query, Set<String> tags) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return recipes.stream()
                .filter(r -> matchesTags(r, tags))
                .filter(r -> q.isEmpty() || matchesText(r, q))
                .collect(Collectors.toList());
    }

    public List<String> getAllTags() {
        return recipes.stream()
                .flatMap(r -> r.getTags().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Recipe> getAll() {
        return Collections.unmodifiableList(recipes);
    }

    public List<String> getLoadWarnings() {
        return Collections.unmodifiableList(loadWarnings);
    }

    private boolean matchesTags(Recipe r, Set<String> required) {
        if (required.isEmpty()) return true;
        List<String> rTags = r.getTags();
        for (String t : required) {
            if (!rTags.contains(t.toLowerCase())) return false;
        }
        return true;
    }

    private boolean matchesText(Recipe r, String q) {
        if (r.getName().toLowerCase().contains(q)) return true;
        if (r.getDescription().toLowerCase().contains(q)) return true;
        for (String ing : r.getIngredients()) {
            if (ing.toLowerCase().contains(q)) return true;
        }
        for (String tag : r.getTags()) {
            if (tag.contains(q)) return true;
        }
        return false;
    }

    private int indexById(String id) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}
