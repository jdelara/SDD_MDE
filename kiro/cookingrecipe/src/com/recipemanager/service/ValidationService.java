package com.recipemanager.service;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

/**
 * Validates recipes, ingredients, and annotations against defined rules.
 */
public interface ValidationService {
    ValidationResult validateRecipe(Recipe recipe);
    ValidationResult validateIngredient(Ingredient ingredient);
    ValidationResult validateAnnotation(String text);
}
