package com.recipemanager.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for SimpleSearchEngine.
 */
public class SearchEnginePropertyTest {

    private final SimpleSearchEngine searchEngine = new SimpleSearchEngine();

    // --- Property 5: Search performs case-insensitive substring matching across fields ---

    /**
     * Property 5: Search performs case-insensitive substring matching across fields.
     * Generate recipes with known content, derive queries from that content.
     * Assert results contain exactly those recipes where query is a substring of
     * name, description, any ingredient name, or instructions.
     *
     * **Validates: Requirements 5.1, 5.4**
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 5: Search performs case-insensitive substring matching across fields")
    void searchReturnsExactlyMatchingRecipes(
            @ForAll("recipeListWithDerivedQuery") Object[] input) {

        @SuppressWarnings("unchecked")
        List<Recipe> recipes = (List<Recipe>) input[0];
        String query = (String) input[1];

        List<Recipe> results = searchEngine.search(query, recipes);

        // Compute expected: recipes where query is a case-insensitive substring
        // of name, description, any ingredient name, or instructions
        List<Recipe> expected = recipes.stream()
                .filter(r -> matchesQuery(r, query))
                .collect(Collectors.toList());

        assertEquals(expected.size(), results.size(),
                "Expected " + expected.size() + " results but got " + results.size() +
                " for query '" + query + "'");

        for (Recipe expectedRecipe : expected) {
            assertTrue(results.stream().anyMatch(r -> r.getId().equals(expectedRecipe.getId())),
                    "Expected recipe '" + expectedRecipe.getName() + "' (id=" + expectedRecipe.getId() +
                    ") to be in results for query '" + query + "'");
        }

        for (Recipe resultRecipe : results) {
            assertTrue(expected.stream().anyMatch(r -> r.getId().equals(resultRecipe.getId())),
                    "Recipe '" + resultRecipe.getName() + "' (id=" + resultRecipe.getId() +
                    ") should NOT be in results for query '" + query + "'");
        }
    }

    /**
     * Property 5: Search is truly case-insensitive.
     * A query in different cases (upper, lower, original) should return the same results.
     *
     * **Validates: Requirements 5.1, 5.4**
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 5: Search performs case-insensitive substring matching across fields")
    void searchIsCaseInsensitive(
            @ForAll("recipeListWithDerivedQuery") Object[] input) {

        @SuppressWarnings("unchecked")
        List<Recipe> recipes = (List<Recipe>) input[0];
        String query = (String) input[1];

        List<Recipe> resultsOriginal = searchEngine.search(query, recipes);
        List<Recipe> resultsUpper = searchEngine.search(query.toUpperCase(), recipes);
        List<Recipe> resultsLower = searchEngine.search(query.toLowerCase(), recipes);

        assertEquals(resultsOriginal.size(), resultsUpper.size(),
                "Upper-case query should return same count as original for query '" + query + "'");
        assertEquals(resultsOriginal.size(), resultsLower.size(),
                "Lower-case query should return same count as original for query '" + query + "'");
    }

    // --- Property 5 Helpers ---

    private boolean matchesQuery(Recipe recipe, String query) {
        String lowerQuery = query.toLowerCase();

        if (recipe.getName() != null && recipe.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.getName() != null && ingredient.getName().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        if (recipe.getInstructions() != null && recipe.getInstructions().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        return false;
    }

    @Provide
    Arbitrary<Object[]> recipeListWithDerivedQuery() {
        return Arbitraries.lazyOf(
                // Strategy 1: query derived from one recipe's name
                () -> recipeListWithQueryFromField(0),
                // Strategy 2: query derived from one recipe's description
                () -> recipeListWithQueryFromField(1),
                // Strategy 3: query derived from one recipe's ingredient name
                () -> recipeListWithQueryFromField(2),
                // Strategy 4: query derived from one recipe's instructions
                () -> recipeListWithQueryFromField(3),
                // Strategy 5: query that does NOT match any recipe (no matches case)
                () -> recipeListWithNonMatchingQuery()
        );
    }

    private Arbitrary<Object[]> recipeListWithQueryFromField(int fieldIndex) {
        return searchRecipeList().flatMap(recipes -> {
            if (recipes.isEmpty()) {
                return Arbitraries.just(new Object[]{recipes, "xyznonmatch"});
            }
            return Arbitraries.integers().between(0, recipes.size() - 1).flatMap(idx -> {
                Recipe target = recipes.get(idx);
                String fieldContent = getFieldContent(target, fieldIndex);
                if (fieldContent == null || fieldContent.isEmpty()) {
                    fieldContent = target.getName();
                }
                final String content = fieldContent;
                return substringOf(content).map(query ->
                        new Object[]{recipes, query}
                );
            });
        });
    }

    private Arbitrary<Object[]> recipeListWithNonMatchingQuery() {
        return searchRecipeList().map(recipes ->
                new Object[]{recipes, "ZZQXJK99NOMATCH"}
        );
    }

    private String getFieldContent(Recipe recipe, int fieldIndex) {
        switch (fieldIndex) {
            case 0: return recipe.getName();
            case 1: return recipe.getDescription();
            case 2:
                if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                    return recipe.getIngredients().get(0).getName();
                }
                return null;
            case 3: return recipe.getInstructions();
            default: return null;
        }
    }

    private Arbitrary<String> substringOf(String content) {
        if (content == null || content.isEmpty()) {
            return Arbitraries.just("a");
        }
        int len = content.length();
        return Arbitraries.integers().between(0, len - 1).flatMap(start ->
                Arbitraries.integers().between(start + 1, len).map(end ->
                        content.substring(start, end)
                )
        );
    }

    private Arbitrary<List<Recipe>> searchRecipeList() {
        return validRecipe().list().ofMinSize(1).ofMaxSize(6)
                .map(recipes -> {
                    for (Recipe r : recipes) {
                        r.setId(UUID.randomUUID());
                    }
                    return recipes;
                });
    }

    // --- Property 6: Whitespace-only search returns all recipes ---

    /**
     * Property 6: Whitespace-only search returns all recipes.
     * For any string composed entirely of whitespace characters (spaces, tabs, newlines),
     * performing a search SHALL return all recipes (equivalent to no filter applied).
     *
     * Validates: Requirements 5.6
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 6: Whitespace-only search returns all recipes")
    void whitespaceOnlySearchReturnsAllRecipes(
            @ForAll("whitespaceOnlyStrings") String whitespaceQuery,
            @ForAll("recipeList") List<Recipe> recipes) {

        List<Recipe> results = searchEngine.search(whitespaceQuery, recipes);

        // Assert results contain all recipes
        assertEquals(recipes.size(), results.size(),
                "Whitespace-only query should return all recipes. Query: '" + escape(whitespaceQuery) + "'");

        // Assert same content (order may differ, but SimpleSearchEngine returns same order)
        for (Recipe recipe : recipes) {
            assertTrue(results.contains(recipe),
                    "Result should contain every recipe from the input list");
        }
    }

    // --- Generators ---

    @Provide
    Arbitrary<String> whitespaceOnlyStrings() {
        // Generate strings composed only of whitespace characters: space, tab, newline, carriage return
        Arbitrary<Character> whitespaceChars = Arbitraries.of(' ', '\t', '\n', '\r');
        return whitespaceChars.list().ofMinSize(1).ofMaxSize(20)
                .map(chars -> {
                    StringBuilder sb = new StringBuilder();
                    for (Character c : chars) {
                        sb.append(c);
                    }
                    return sb.toString();
                });
    }

    @Provide
    Arbitrary<List<Recipe>> recipeList() {
        return validRecipe().list().ofMinSize(0).ofMaxSize(10);
    }

    private Arbitrary<Recipe> validRecipe() {
        Arbitrary<String> names = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(50);
        Arbitrary<String> descriptions = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(100);
        Arbitrary<String> instructions = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(100);
        Arbitrary<Integer> servings = Arbitraries.integers().between(1, 100);
        Arbitrary<List<Ingredient>> ingredients = validIngredient().list().ofMinSize(1).ofMaxSize(5);

        return Combinators.combine(names, descriptions, instructions, servings, ingredients)
                .as((name, desc, instr, serv, ingr) -> {
                    Recipe recipe = new Recipe();
                    recipe.setId(UUID.randomUUID());
                    recipe.setName(name);
                    recipe.setDescription(desc);
                    recipe.setInstructions(instr);
                    recipe.setServings(serv);
                    recipe.setIngredients(ingr);
                    recipe.setTags(new ArrayList<>());
                    recipe.setAnnotations(new ArrayList<>());
                    return recipe;
                });
    }

    private Arbitrary<Ingredient> validIngredient() {
        Arbitrary<String> names = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(30);
        Arbitrary<Double> quantities = Arbitraries.doubles().between(0.01, 9999.0);
        Arbitrary<String> units = Arbitraries.of("g", "kg", "ml", "l", "cup", "tbsp", "tsp", "piece");

        return Combinators.combine(names, quantities, units)
                .as(Ingredient::new);
    }

    // Helper to escape whitespace for readable assertion messages
    private String escape(String s) {
        return s.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r").replace(" ", "·");
    }
}
