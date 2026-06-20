package com.recipemanager.service;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

import java.util.List;

/**
 * Default implementation of ValidationService with the following rules:
 * - Recipe name: 1-100 characters
 * - Recipe servings: 1-100 whole number
 * - Recipe ingredients: 1-50 count
 * - Ingredient name: 1-50 characters
 * - Ingredient quantity: 0.01-99999
 * - Annotation text: 1-2000 characters and not whitespace-only
 */
public class DefaultValidationService implements ValidationService {

    @Override
    public ValidationResult validateRecipe(Recipe recipe) {
        ValidationResult result = new ValidationResult();

        // Validate name: 1-100 characters
        if (recipe.getName() == null || recipe.getName().isEmpty()) {
            result.addError("Recipe name is required");
        } else if (recipe.getName().length() > 100) {
            result.addError("Recipe name must not exceed 100 characters");
        }

        // Validate servings: 1-100 whole number
        if (recipe.getServings() < 1 || recipe.getServings() > 100) {
            result.addError("Servings must be a whole number between 1 and 100");
        }

        // Validate ingredients count: 1-50
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) {
            result.addError("Recipe must have at least 1 ingredient");
        } else if (ingredients.size() > 50) {
            result.addError("Recipe must not have more than 50 ingredients");
        } else {
            // Validate each ingredient
            for (int i = 0; i < ingredients.size(); i++) {
                ValidationResult ingredientResult = validateIngredient(ingredients.get(i));
                for (String error : ingredientResult.getErrors()) {
                    result.addError("Ingredient " + (i + 1) + ": " + error);
                }
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateIngredient(Ingredient ingredient) {
        ValidationResult result = new ValidationResult();

        // Validate ingredient name: 1-50 characters
        if (ingredient.getName() == null || ingredient.getName().isEmpty()) {
            result.addError("Ingredient name is required");
        } else if (ingredient.getName().length() > 50) {
            result.addError("Ingredient name must not exceed 50 characters");
        }

        // Validate ingredient quantity: 0.01-99999
        if (ingredient.getQuantity() < 0.01 || ingredient.getQuantity() > 99999) {
            result.addError("Ingredient quantity must be between 0.01 and 99999");
        }

        return result;
    }

    @Override
    public ValidationResult validateAnnotation(String text) {
        ValidationResult result = new ValidationResult();

        // Validate annotation text: 1-2000 characters and not whitespace-only
        if (text == null || text.isEmpty()) {
            result.addError("Annotation text is required");
        } else if (text.trim().isEmpty()) {
            result.addError("Annotation text must not be whitespace-only");
        } else if (text.length() > 2000) {
            result.addError("Annotation text must not exceed 2000 characters");
        }

        return result;
    }
}
