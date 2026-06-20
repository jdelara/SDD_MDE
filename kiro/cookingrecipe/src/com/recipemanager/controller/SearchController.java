package com.recipemanager.controller;

import com.recipemanager.model.Recipe;
import com.recipemanager.service.SearchEngine;

import java.util.Comparator;
import java.util.List;

/**
 * Bridges the SearchPanel to the SearchEngine.
 * Handles search queries and returns results sorted case-insensitively by recipe name.
 */
public class SearchController {

    private final SearchEngine searchEngine;
    private final RecipeController recipeController;

    public SearchController(SearchEngine searchEngine, RecipeController recipeController) {
        this.searchEngine = searchEngine;
        this.recipeController = recipeController;
    }

    /**
     * Searches recipes by the given query string.
     * If the query is null, empty, or contains only whitespace, returns
     * all recipes sorted case-insensitively by name (delegates to RecipeController).
     * Otherwise, performs a search and sorts the results case-insensitively by name.
     *
     * @param query the search query string
     * @return list of matching recipes sorted case-insensitively by name
     */
    public List<Recipe> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return recipeController.getRecipesSorted();
        }

        List<Recipe> results = searchEngine.search(query, recipeController.getAllRecipes());
        results.sort(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER));
        return results;
    }
}
