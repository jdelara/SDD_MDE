package com.recipemanager.service;

import java.util.List;

import com.recipemanager.model.Recipe;

/**
 * Interface for searching recipes by query string.
 */
public interface SearchEngine {

    /**
     * Searches the given list of recipes for those matching the query.
     * <p>
     * If the query is null, empty, or contains only whitespace characters,
     * all recipes are returned (treated as no filter).
     * <p>
     * The caller is responsible for ordering the results.
     *
     * @param query   the search query string
     * @param recipes the list of recipes to search through
     * @return a list of recipes matching the query
     */
    List<Recipe> search(String query, List<Recipe> recipes);
}
