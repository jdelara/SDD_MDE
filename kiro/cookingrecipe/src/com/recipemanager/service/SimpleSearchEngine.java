package com.recipemanager.service;

import java.util.ArrayList;
import java.util.List;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

/**
 * A search engine implementation that performs case-insensitive substring matching
 * across recipe name, description, ingredient names, and instructions.
 */
public class SimpleSearchEngine implements SearchEngine {

    @Override
    public List<Recipe> search(String query, List<Recipe> recipes) {
        if (recipes == null) {
            return new ArrayList<>();
        }

        // Whitespace-only or null/empty queries return all recipes
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(recipes);
        }

        String lowerQuery = query.toLowerCase();
        List<Recipe> results = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (matches(recipe, lowerQuery)) {
                results.add(recipe);
            }
        }

        return results;
    }

    private boolean matches(Recipe recipe, String lowerQuery) {
        // Check recipe name
        if (recipe.getName() != null && recipe.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Check description
        if (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Check ingredient names
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.getName() != null && ingredient.getName().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }

        // Check instructions
        if (recipe.getInstructions() != null && recipe.getInstructions().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        return false;
    }
}
