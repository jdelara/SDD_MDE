package com.recipemanager.service;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Property-based tests for ValidationService.
 * 
 * Validates: Requirements 1.2, 1.3, 1.4, 1.6
 */
@Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
class ValidationServiceTest {

    private final ValidationService validationService = new DefaultValidationService();

    // === Property 2: Recipe validation accepts valid and rejects invalid ===

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void validRecipesAreAccepted(
            @ForAll("validRecipeName") String name,
            @ForAll("validServings") int servings,
            @ForAll("validIngredientList") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert result.isValid() : "Expected valid recipe to be accepted but got errors: " + result.getErrors();
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void invalidNameRecipesAreRejected(
            @ForAll("invalidRecipeName") String name,
            @ForAll("validServings") int servings,
            @ForAll("validIngredientList") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert !result.isValid() : "Expected recipe with invalid name '" + name + "' to be rejected";
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void invalidServingsRecipesAreRejected(
            @ForAll("validRecipeName") String name,
            @ForAll("invalidServings") int servings,
            @ForAll("validIngredientList") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert !result.isValid() : "Expected recipe with invalid servings " + servings + " to be rejected";
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void invalidIngredientCountRecipesAreRejected(
            @ForAll("validRecipeName") String name,
            @ForAll("validServings") int servings,
            @ForAll("invalidIngredientCount") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert !result.isValid() : "Expected recipe with " + ingredients.size() + " ingredients to be rejected";
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void invalidIngredientNameRecipesAreRejected(
            @ForAll("validRecipeName") String name,
            @ForAll("validServings") int servings,
            @ForAll("ingredientListWithInvalidName") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert !result.isValid() : "Expected recipe with invalid ingredient name to be rejected";
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void invalidIngredientQuantityRecipesAreRejected(
            @ForAll("validRecipeName") String name,
            @ForAll("validServings") int servings,
            @ForAll("ingredientListWithInvalidQuantity") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);
        assert !result.isValid() : "Expected recipe with invalid ingredient quantity to be rejected";
    }

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void validationAcceptsIfAndOnlyIfAllBoundsSatisfied(
            @ForAll("anyRecipeName") String name,
            @ForAll("anyServings") int servings,
            @ForAll("anyIngredientList") List<Ingredient> ingredients
    ) {
        Recipe recipe = buildRecipe(name, servings, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);

        boolean nameValid = name != null && !name.isEmpty() && name.length() <= 100;
        boolean servingsValid = servings >= 1 && servings <= 100;
        boolean countValid = ingredients != null && ingredients.size() >= 1 && ingredients.size() <= 50;
        boolean allIngredientsValid = countValid && ingredients.stream().allMatch(this::isIngredientValid);

        boolean shouldBeValid = nameValid && servingsValid && allIngredientsValid;

        assert result.isValid() == shouldBeValid :
                "Biconditional failed: expected isValid()=" + shouldBeValid +
                " but got " + result.isValid() +
                " (name='" + name + "', servings=" + servings +
                ", ingredientCount=" + (ingredients != null ? ingredients.size() : 0) +
                ", errors=" + result.getErrors() + ")";
    }

    // === Boundary-specific properties ===

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void nameBoundaryValues(@ForAll("boundaryName") String name) {
        Recipe recipe = buildRecipe(name, 4, createValidIngredients(2));
        ValidationResult result = validationService.validateRecipe(recipe);

        boolean nameValid = name != null && !name.isEmpty() && name.length() <= 100;
        assert result.isValid() == nameValid :
                "Name boundary: expected isValid()=" + nameValid + " for name length " +
                (name != null ? name.length() : "null") + " but got " + result.isValid();
    }

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void servingsBoundaryValues(@ForAll("boundaryServings") int servings) {
        Recipe recipe = buildRecipe("Valid Name", servings, createValidIngredients(2));
        ValidationResult result = validationService.validateRecipe(recipe);

        boolean servingsValid = servings >= 1 && servings <= 100;
        assert result.isValid() == servingsValid :
                "Servings boundary: expected isValid()=" + servingsValid + " for servings=" +
                servings + " but got " + result.isValid();
    }

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void ingredientCountBoundaryValues(@ForAll("boundaryIngredientCount") int count) {
        List<Ingredient> ingredients = createValidIngredients(count);
        Recipe recipe = buildRecipe("Valid Name", 4, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);

        boolean countValid = count >= 1 && count <= 50;
        assert result.isValid() == countValid :
                "Ingredient count boundary: expected isValid()=" + countValid +
                " for count=" + count + " but got " + result.isValid();
    }

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 2: Recipe validation accepts valid and rejects invalid")
    void ingredientQuantityBoundaryValues(@ForAll("boundaryQuantity") double quantity) {
        Ingredient ingredient = new Ingredient("Salt", quantity, "g");
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        Recipe recipe = buildRecipe("Valid Name", 4, ingredients);
        ValidationResult result = validationService.validateRecipe(recipe);

        boolean quantityValid = quantity >= 0.01 && quantity <= 99999;
        assert result.isValid() == quantityValid :
                "Quantity boundary: expected isValid()=" + quantityValid +
                " for quantity=" + quantity + " but got " + result.isValid();
    }

    // === Providers ===

    @Provide
    Arbitrary<String> validRecipeName() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(100)
                .alpha()
                .ofMinLength(1);
    }

    @Provide
    Arbitrary<String> invalidRecipeName() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.just(null),
                Arbitraries.strings().alpha().ofMinLength(101).ofMaxLength(150)
        );
    }

    @Provide
    Arbitrary<String> anyRecipeName() {
        return Arbitraries.oneOf(
                validRecipeName(),
                Arbitraries.just(""),
                Arbitraries.just(null),
                Arbitraries.strings().alpha().ofMinLength(101).ofMaxLength(120)
        );
    }

    @Provide
    Arbitrary<String> boundaryName() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),                                          // length 0 - invalid
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(1),   // length 1 - valid boundary
                Arbitraries.strings().alpha().ofMinLength(100).ofMaxLength(100), // length 100 - valid boundary
                Arbitraries.strings().alpha().ofMinLength(101).ofMaxLength(101)  // length 101 - invalid boundary
        );
    }

    @Provide
    Arbitrary<Integer> validServings() {
        return Arbitraries.integers().between(1, 100);
    }

    @Provide
    Arbitrary<Integer> invalidServings() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-100, 0),
                Arbitraries.integers().between(101, 1000)
        );
    }

    @Provide
    Arbitrary<Integer> anyServings() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(1, 100),
                Arbitraries.integers().between(-50, 0),
                Arbitraries.integers().between(101, 500)
        );
    }

    @Provide
    Arbitrary<Integer> boundaryServings() {
        return Arbitraries.of(0, 1, 2, 50, 99, 100, 101);
    }

    @Provide
    Arbitrary<List<Ingredient>> validIngredientList() {
        return validIngredient().list().ofMinSize(1).ofMaxSize(50);
    }

    @Provide
    Arbitrary<List<Ingredient>> invalidIngredientCount() {
        return Arbitraries.oneOf(
                Arbitraries.just(new ArrayList<>()),  // 0 ingredients
                validIngredient().list().ofMinSize(51).ofMaxSize(55) // 51+ ingredients
        );
    }

    @Provide
    Arbitrary<List<Ingredient>> ingredientListWithInvalidName() {
        Arbitrary<Ingredient> invalidNameIngredient = Arbitraries.oneOf(
                Arbitraries.just(new Ingredient("", 1.0, "g")),
                Arbitraries.just(new Ingredient(null, 1.0, "g")),
                Arbitraries.strings().alpha().ofMinLength(51).ofMaxLength(60)
                        .map(n -> new Ingredient(n, 1.0, "g"))
        );
        // List with at least one invalid-name ingredient
        return invalidNameIngredient.map(inv -> {
            List<Ingredient> list = new ArrayList<>();
            list.add(inv);
            return list;
        });
    }

    @Provide
    Arbitrary<List<Ingredient>> ingredientListWithInvalidQuantity() {
        Arbitrary<Ingredient> invalidQuantityIngredient = Arbitraries.oneOf(
                Arbitraries.doubles().between(0.0, 0.009).ofScale(3).map(q -> new Ingredient("Salt", q, "g")),
                Arbitraries.doubles().between(99999.01, 200000.0).ofScale(2).map(q -> new Ingredient("Salt", q, "g")),
                Arbitraries.just(new Ingredient("Salt", 0.0, "g")),
                Arbitraries.just(new Ingredient("Salt", -1.0, "g"))
        );
        return invalidQuantityIngredient.map(inv -> {
            List<Ingredient> list = new ArrayList<>();
            list.add(inv);
            return list;
        });
    }

    @Provide
    Arbitrary<List<Ingredient>> anyIngredientList() {
        Arbitrary<Ingredient> anyIngredient = Arbitraries.oneOf(
                validIngredient(),
                Arbitraries.just(new Ingredient("", 1.0, "g")),
                Arbitraries.just(new Ingredient(null, 1.0, "g")),
                Arbitraries.strings().alpha().ofMinLength(51).ofMaxLength(55)
                        .map(n -> new Ingredient(n, 1.0, "g")),
                Arbitraries.doubles().between(0.0, 0.009).ofScale(3)
                        .map(q -> new Ingredient("Salt", q, "g")),
                Arbitraries.doubles().between(99999.01, 150000.0).ofScale(2)
                        .map(q -> new Ingredient("Salt", q, "g"))
        );
        return Arbitraries.oneOf(
                anyIngredient.list().ofMinSize(1).ofMaxSize(50),
                anyIngredient.list().ofMinSize(0).ofMaxSize(0),   // empty
                anyIngredient.list().ofMinSize(51).ofMaxSize(55)  // too many
        );
    }

    @Provide
    Arbitrary<Integer> boundaryIngredientCount() {
        return Arbitraries.of(0, 1, 2, 25, 49, 50, 51);
    }

    @Provide
    Arbitrary<Double> boundaryQuantity() {
        return Arbitraries.of(0.0, 0.009, 0.01, 0.02, 1.0, 500.0, 99998.0, 99999.0, 99999.01, 100000.0);
    }

    // === Helper methods ===

    private Arbitrary<Ingredient> validIngredient() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.doubles().between(0.01, 99999.0),
                Arbitraries.of("g", "kg", "ml", "L", "cup", "tbsp", "tsp", "oz", "lb")
        ).as(Ingredient::new);
    }

    private boolean isIngredientValid(Ingredient ingredient) {
        if (ingredient.getName() == null || ingredient.getName().isEmpty()) return false;
        if (ingredient.getName().length() > 50) return false;
        if (ingredient.getQuantity() < 0.01 || ingredient.getQuantity() > 99999) return false;
        return true;
    }

    private Recipe buildRecipe(String name, int servings, List<Ingredient> ingredients) {
        Recipe recipe = new Recipe();
        recipe.setId(UUID.randomUUID());
        recipe.setName(name);
        recipe.setDescription("Test recipe description");
        recipe.setIngredients(ingredients != null ? ingredients : new ArrayList<>());
        recipe.setServings(servings);
        recipe.setInstructions("Test instructions");
        recipe.setTags(new ArrayList<>());
        recipe.setAnnotations(new ArrayList<>());
        return recipe;
    }

    private List<Ingredient> createValidIngredients(int count) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ingredients.add(new Ingredient("Ingredient" + (i + 1), 1.0, "g"));
        }
        return ingredients;
    }

    // === Property 11: Whitespace-only annotations are rejected ===
    // **Validates: Requirements 6.4**

    @Property(tries = 200)
    @Tag("Feature: recipe-manager, Property 11: Whitespace-only annotations are rejected")
    void whitespaceOnlyAnnotationsAreRejected(
            @ForAll("whitespaceOnlyStrings") String text
    ) {
        ValidationResult result = validationService.validateAnnotation(text);
        assert !result.isValid() : "Expected whitespace-only annotation '" + text.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r") + "' to be rejected";
    }

    @Provide
    Arbitrary<String> whitespaceOnlyStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),                          // empty string
                Arbitraries.of(' ', '\t', '\n', '\r')         // whitespace chars
                        .list()
                        .ofMinSize(1)
                        .ofMaxSize(50)
                        .map(chars -> {
                            StringBuilder sb = new StringBuilder();
                            for (Character c : chars) {
                                sb.append(c);
                            }
                            return sb.toString();
                        })
        );
    }
}
