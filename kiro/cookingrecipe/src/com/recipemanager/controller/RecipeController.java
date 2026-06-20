package com.recipemanager.controller;

import com.recipemanager.model.Annotation;
import com.recipemanager.model.Recipe;
import com.recipemanager.service.PersistenceException;
import com.recipemanager.service.RecipeStore;
import com.recipemanager.service.ValidationResult;
import com.recipemanager.service.ValidationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Coordinates between the UI layer and RecipeStore/ValidationService.
 * Handles recipe CRUD operations, sorting, tag filtering, and annotation management.
 */
public class RecipeController {

    private final RecipeStore recipeStore;
    private final ValidationService validationService;

    public RecipeController(RecipeStore recipeStore, ValidationService validationService) {
        this.recipeStore = recipeStore;
        this.validationService = validationService;
    }

    /**
     * Creates a new recipe after validation.
     *
     * @param recipe the recipe to create
     * @return ValidationResult indicating success (valid) or failure (with errors)
     */
    public ValidationResult createRecipe(Recipe recipe) {
        ValidationResult result = validationService.validateRecipe(recipe);
        if (!result.isValid()) {
            return result;
        }
        try {
            recipeStore.save(recipe);
            return result; // valid result indicates success
        } catch (PersistenceException e) {
            ValidationResult failure = new ValidationResult();
            failure.addError("Failed to save recipe: " + e.getMessage());
            return failure;
        }
    }

    /**
     * Updates an existing recipe after validation, preserving existing annotations.
     *
     * @param recipe the recipe with updated data
     * @return ValidationResult indicating success (valid) or failure (with errors)
     */
    public ValidationResult updateRecipe(Recipe recipe) {
        ValidationResult result = validationService.validateRecipe(recipe);
        if (!result.isValid()) {
            return result;
        }

        // Preserve existing annotations from the persisted version
        List<Recipe> allRecipes = recipeStore.loadAll();
        for (Recipe existing : allRecipes) {
            if (existing.getId().equals(recipe.getId())) {
                recipe.setAnnotations(existing.getAnnotations());
                break;
            }
        }

        try {
            recipeStore.save(recipe);
            return result; // valid result indicates success
        } catch (PersistenceException e) {
            ValidationResult failure = new ValidationResult();
            failure.addError("Failed to update recipe: " + e.getMessage());
            return failure;
        }
    }

    /**
     * Deletes a recipe by its ID.
     *
     * @param recipeId the UUID of the recipe to delete
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteRecipe(UUID recipeId) {
        try {
            recipeStore.delete(recipeId);
            return true;
        } catch (PersistenceException e) {
            return false;
        }
    }

    /**
     * Returns all recipes from the store.
     *
     * @return list of all recipes
     */
    public List<Recipe> getAllRecipes() {
        return recipeStore.loadAll();
    }

    /**
     * Returns all recipes sorted alphabetically by name (case-insensitive).
     *
     * @return sorted list of recipes
     */
    public List<Recipe> getRecipesSorted() {
        List<Recipe> recipes = new ArrayList<>(recipeStore.loadAll());
        recipes.sort(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER));
        return recipes;
    }

    /**
     * Returns recipes filtered by a specific tag, sorted alphabetically (case-insensitive).
     *
     * @param tag the tag to filter by
     * @return sorted list of recipes containing the specified tag
     */
    public List<Recipe> filterByTag(String tag) {
        List<Recipe> recipes = recipeStore.loadAll();
        return recipes.stream()
                .filter(recipe -> recipe.getTags().contains(tag))
                .sorted(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Returns annotations for a recipe sorted newest-first (reverse chronological order).
     *
     * @param recipeId the UUID of the recipe
     * @return list of annotations sorted by createdAt descending, or empty list if recipe not found
     */
    public List<Annotation> getAnnotationsSorted(UUID recipeId) {
        List<Recipe> allRecipes = recipeStore.loadAll();
        for (Recipe recipe : allRecipes) {
            if (recipe.getId().equals(recipeId)) {
                List<Annotation> annotations = new ArrayList<>(recipe.getAnnotations());
                annotations.sort(Comparator.comparing(Annotation::getCreatedAt, Comparator.reverseOrder()));
                return annotations;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Adds an annotation to a recipe after validation.
     *
     * @param recipeId the UUID of the recipe to annotate
     * @param text the annotation text
     * @return ValidationResult indicating success (valid) or failure (with errors)
     */
    public ValidationResult addAnnotation(UUID recipeId, String text) {
        ValidationResult result = validationService.validateAnnotation(text);
        if (!result.isValid()) {
            return result;
        }

        List<Recipe> allRecipes = recipeStore.loadAll();
        Recipe target = null;
        for (Recipe recipe : allRecipes) {
            if (recipe.getId().equals(recipeId)) {
                target = recipe;
                break;
            }
        }

        if (target == null) {
            ValidationResult failure = new ValidationResult();
            failure.addError("Recipe not found");
            return failure;
        }

        Annotation annotation = new Annotation(text, LocalDateTime.now());
        List<Annotation> annotations = new ArrayList<>(target.getAnnotations());
        annotations.add(annotation);
        target.setAnnotations(annotations);

        try {
            recipeStore.save(target);
            return result; // valid result indicates success
        } catch (PersistenceException e) {
            ValidationResult failure = new ValidationResult();
            failure.addError("Failed to save annotation: " + e.getMessage());
            return failure;
        }
    }
}
