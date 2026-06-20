package com.cookingrecipes.service;

import com.cookingrecipes.model.*;
import com.cookingrecipes.storage.RecipeStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeService {
    private static final DateTimeFormatter TS_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RecipeStore store;

    public RecipeService(RecipeStore store) {
        this.store = store;
    }

    public void createRecipe(Recipe recipe) {
        registerTags(recipe.getTags());
        store.addRecipe(recipe);
        store.save();
    }

    public void updateRecipe(Recipe updated) {
        Recipe existing = store.findById(updated.getId());
        if (existing == null) return;
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setServes(updated.getServes());
        existing.setInstructions(updated.getInstructions());
        existing.setIngredients(updated.getIngredients());
        existing.setTags(updated.getTags());
        // annotations are not replaced on edit
        registerTags(updated.getTags());
        store.save();
    }

    public void deleteRecipe(String id) {
        store.removeRecipe(id);
        store.save();
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(store.getRecipes());
    }

    public List<Tag> getAllTags() {
        return new ArrayList<>(store.getTags());
    }

    public void addAnnotation(String recipeId, String text) {
        Recipe recipe = store.findById(recipeId);
        if (recipe == null || text == null || text.trim().isEmpty()) return;
        String ts = LocalDateTime.now().format(TS_FORMAT);
        recipe.getAnnotations().add(new Annotation(text.trim(), ts));
        store.save();
    }

    public List<Recipe> getRecipesByTag(Tag tag) {
        if (tag == null) return getAllRecipes();
        return store.getRecipes().stream()
            .filter(r -> r.getTags().stream().anyMatch(t -> t.equals(tag)))
            .collect(Collectors.toList());
    }

    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) return getAllRecipes();
        String q = query.trim().toLowerCase();
        return store.getRecipes().stream()
            .filter(r -> matches(r, q))
            .collect(Collectors.toList());
    }

    public List<Recipe> filterAndSearch(Tag tag, String query) {
        List<Recipe> base = (tag == null) ? getAllRecipes() : getRecipesByTag(tag);
        if (query == null || query.trim().isEmpty()) return base;
        String q = query.trim().toLowerCase();
        return base.stream().filter(r -> matches(r, q)).collect(Collectors.toList());
    }

    private boolean matches(Recipe r, String q) {
        if (contains(r.getName(), q)) return true;
        if (contains(r.getDescription(), q)) return true;
        if (contains(r.getInstructions(), q)) return true;
        return r.getIngredients().stream().anyMatch(i -> contains(i.getName(), q));
    }

    private boolean contains(String text, String q) {
        return text != null && text.toLowerCase().contains(q);
    }

    private void registerTags(List<Tag> tags) {
        if (tags != null) tags.forEach(store::addTag);
    }
}
