package com.recipemanager.service;

import com.recipemanager.model.Recipe;

import java.util.List;
import java.util.UUID;

/**
 * Interface for recipe persistence operations.
 */
public interface RecipeStore {

    /**
     * Loads all recipes from storage. Malformed files are skipped with a warning log.
     *
     * @return list of all successfully loaded recipes
     */
    List<Recipe> loadAll();

    /**
     * Persists a recipe to storage. Creates directories if they don't exist.
     *
     * @param recipe the recipe to save
     * @throws PersistenceException if an I/O error occurs during write
     */
    void save(Recipe recipe) throws PersistenceException;

    /**
     * Deletes a recipe from storage by its ID.
     *
     * @param recipeId the UUID of the recipe to delete
     * @throws PersistenceException if an I/O error occurs during deletion
     */
    void delete(UUID recipeId) throws PersistenceException;
}
