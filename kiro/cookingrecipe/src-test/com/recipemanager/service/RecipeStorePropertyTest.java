package com.recipemanager.service;

import com.recipemanager.model.Annotation;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.Assume;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Property-based tests for Recipe serialization round-trip.
 *
 * Validates: Requirements 1.1, 7.5, 8.1
 */
@Tag("Feature: recipe-manager, Property 1: Recipe serialization round-trip")
class RecipeStorePropertyTest {

    // === Property 1: Recipe serialization round-trip ===

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 1: Recipe serialization round-trip")
    void serializationRoundTripPreservesAllFields(
            @ForAll("validRecipe") Recipe recipe
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-store-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);

            store.save(recipe);

            List<Recipe> loaded = store.loadAll();

            assert loaded.size() == 1 :
                    "Expected exactly 1 recipe but got " + loaded.size();

            Recipe deserialized = loaded.get(0);

            assert recipe.equals(deserialized) :
                    "Deserialized recipe does not equal original.\n" +
                    "Original: " + recipe + "\n" +
                    "Deserialized: " + deserialized;
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 1: Recipe serialization round-trip")
    void serializationPreservesIngredientOrder(
            @ForAll("validRecipe") Recipe recipe
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-store-order-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);

            store.save(recipe);

            List<Recipe> loaded = store.loadAll();
            Recipe deserialized = loaded.get(0);

            List<Ingredient> originalIngredients = recipe.getIngredients();
            List<Ingredient> deserializedIngredients = deserialized.getIngredients();

            assert originalIngredients.size() == deserializedIngredients.size() :
                    "Ingredient count mismatch: expected " + originalIngredients.size() +
                    " but got " + deserializedIngredients.size();

            for (int i = 0; i < originalIngredients.size(); i++) {
                assert originalIngredients.get(i).equals(deserializedIngredients.get(i)) :
                        "Ingredient at index " + i + " differs.\n" +
                        "Original: " + originalIngredients.get(i) + "\n" +
                        "Deserialized: " + deserializedIngredients.get(i);
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Providers ===

    @Provide
    Arbitrary<Recipe> validRecipe() {
        return Combinators.combine(
                validName(),
                validDescription(),
                validIngredientList(),
                validServings(),
                validInstructions(),
                validTagList(),
                validAnnotationList()
        ).as((name, description, ingredients, servings, instructions, tags, annotations) -> {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(name);
            recipe.setDescription(description);
            recipe.setIngredients(ingredients);
            recipe.setServings(servings);
            recipe.setInstructions(instructions);
            recipe.setTags(tags);
            recipe.setAnnotations(annotations);
            return recipe;
        });
    }

    private Arbitrary<String> validName() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(100);
    }

    private Arbitrary<String> validDescription() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200)
        );
    }

    private Arbitrary<List<Ingredient>> validIngredientList() {
        return validIngredient().list().ofMinSize(1).ofMaxSize(10);
    }

    private Arbitrary<Ingredient> validIngredient() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                validQuantity(),
                Arbitraries.of("g", "kg", "ml", "L", "cup", "tbsp", "tsp", "oz", "lb")
        ).as(Ingredient::new);
    }

    private Arbitrary<Double> validQuantity() {
        // Use integers divided by 100 to get clean decimal representations
        // that survive JSON serialization without floating point issues
        return Arbitraries.integers().between(1, 9999900)
                .map(i -> i / 100.0);
    }

    private Arbitrary<Integer> validServings() {
        return Arbitraries.integers().between(1, 100);
    }

    private Arbitrary<String> validInstructions() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(500);
    }

    private Arbitrary<List<String>> validTagList() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .list().ofMinSize(0).ofMaxSize(5);
    }

    private Arbitrary<List<Annotation>> validAnnotationList() {
        return validAnnotation().list().ofMinSize(0).ofMaxSize(3);
    }

    private Arbitrary<Annotation> validAnnotation() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200),
                validLocalDateTime()
        ).as(Annotation::new);
    }

    private Arbitrary<LocalDateTime> validLocalDateTime() {
        // Generate LocalDateTime without nano precision to avoid serialization issues
        return Combinators.combine(
                Arbitraries.integers().between(2000, 2030),  // year
                Arbitraries.integers().between(1, 12),        // month
                Arbitraries.integers().between(1, 28),        // day (use 28 to avoid month overflow)
                Arbitraries.integers().between(0, 23),        // hour
                Arbitraries.integers().between(0, 59),        // minute
                Arbitraries.integers().between(0, 59)         // second
        ).as(LocalDateTime::of);
    }

    // === Property 14: Delete removes recipe from storage and listing ===

    /**
     * Validates: Requirements 9.2
     */
    @Property(tries = 100)
    @Tag("Property14-Delete-removes-recipe")
    void deleteRemovesRecipeFromStorageAndListing(
            @ForAll("validRecipeList") List<Recipe> recipes,
            @ForAll @IntRange(min = 0, max = 9) int deleteIndex
    ) throws Exception {
        Assume.that(recipes.size() > 0);
        int actualDeleteIndex = deleteIndex % recipes.size();

        Path tempDir = Files.createTempDirectory("recipe-store-delete-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);

            // Save all recipes
            for (Recipe recipe : recipes) {
                store.save(recipe);
            }

            // Pick the recipe to delete
            Recipe toDelete = recipes.get(actualDeleteIndex);
            UUID deletedId = toDelete.getId();

            // Delete it
            store.delete(deletedId);

            // Assert the deleted recipe's JSON file no longer exists on disk
            Path deletedFile = tempDir.resolve(deletedId.toString() + ".json");
            assert !Files.exists(deletedFile) :
                    "Deleted recipe's file should not exist on disk: " + deletedFile;

            // Assert loadAll() no longer returns the deleted recipe
            List<Recipe> loaded = store.loadAll();
            boolean containsDeleted = loaded.stream()
                    .anyMatch(r -> r.getId().equals(deletedId));
            assert !containsDeleted :
                    "loadAll() should not return the deleted recipe with id: " + deletedId;

            // Assert all other recipes are still present
            assert loaded.size() == recipes.size() - 1 :
                    "Expected " + (recipes.size() - 1) + " recipes after deletion but got " + loaded.size();

            for (int i = 0; i < recipes.size(); i++) {
                if (i == actualDeleteIndex) continue;
                Recipe expected = recipes.get(i);
                boolean found = loaded.stream()
                        .anyMatch(r -> r.getId().equals(expected.getId()));
                assert found :
                        "Recipe with id " + expected.getId() + " should still be present after deleting " + deletedId;
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Providers for Property 14 ===

    @Provide
    Arbitrary<List<Recipe>> validRecipeList() {
        return validRecipe().list().ofMinSize(1).ofMaxSize(5)
                .map(recipes -> {
                    // Ensure all recipes have unique IDs
                    List<Recipe> uniqueRecipes = new ArrayList<>();
                    for (Recipe r : recipes) {
                        r.setId(UUID.randomUUID());
                        uniqueRecipes.add(r);
                    }
                    return uniqueRecipes;
                });
    }

    // === Property 15: Malformed JSON files are skipped without crash ===

    /**
     * Validates: Requirements 7.4
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 15: Malformed JSON files are skipped without crash")
    void malformedJsonFilesAreSkippedWithoutCrash(
            @ForAll("validRecipe") Recipe validRecipe,
            @ForAll("malformedJson") String malformedContent
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-store-malformed-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);

            // Save a valid recipe
            store.save(validRecipe);

            // Write a malformed JSON file alongside the valid one
            Path malformedFile = tempDir.resolve("malformed-" + UUID.randomUUID() + ".json");
            Files.writeString(malformedFile, malformedContent);

            // loadAll() should skip the malformed file and return only the valid recipe
            List<Recipe> loaded = store.loadAll();

            assert loaded.size() == 1 :
                    "Expected exactly 1 valid recipe but got " + loaded.size() +
                    ". Malformed content was: " + malformedContent;

            assert loaded.get(0).equals(validRecipe) :
                    "Loaded recipe does not match the valid recipe.\n" +
                    "Expected: " + validRecipe + "\n" +
                    "Got: " + loaded.get(0);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    /**
     * Validates: Requirements 7.4
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 15: Malformed JSON files are skipped without crash")
    void multipleMalformedFilesDoNotAffectValidRecipes(
            @ForAll("validRecipe") Recipe validRecipe1,
            @ForAll("validRecipe") Recipe validRecipe2,
            @ForAll("malformedJsonList") List<String> malformedContents
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-store-multi-malformed-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);

            // Save valid recipes
            store.save(validRecipe1);
            store.save(validRecipe2);

            // Write multiple malformed JSON files
            for (int i = 0; i < malformedContents.size(); i++) {
                Path malformedFile = tempDir.resolve("bad-" + i + "-" + UUID.randomUUID() + ".json");
                Files.writeString(malformedFile, malformedContents.get(i));
            }

            // loadAll() should return only the 2 valid recipes
            List<Recipe> loaded = store.loadAll();

            assert loaded.size() == 2 :
                    "Expected 2 valid recipes but got " + loaded.size();

            // Check both valid recipes are present (order may vary)
            boolean has1 = loaded.contains(validRecipe1);
            boolean has2 = loaded.contains(validRecipe2);

            assert has1 : "Valid recipe 1 was not found in loaded results";
            assert has2 : "Valid recipe 2 was not found in loaded results";
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Providers for Property 15 ===

    @Provide
    Arbitrary<String> malformedJson() {
        return Arbitraries.oneOf(
                // Invalid JSON syntax: truncated
                Arbitraries.just("{\"name\": \"test\""),
                // Invalid JSON syntax: random characters
                Arbitraries.strings().ofMinLength(1).ofMaxLength(100)
                        .filter(s -> !isValidRecipeJson(s)),
                // Empty string
                Arbitraries.just(""),
                // Just braces with no content
                Arbitraries.just("{}"),
                // Valid JSON but missing required fields (no name)
                Arbitraries.just("{\"servings\": 4, \"instructions\": \"mix\"}"),
                // Array instead of object
                Arbitraries.just("[1, 2, 3]"),
                // Number instead of object
                Arbitraries.just("42"),
                // Null literal
                Arbitraries.just("null"),
                // Trailing comma (invalid JSON)
                Arbitraries.just("{\"name\": \"test\",}"),
                // Unquoted keys (invalid JSON)
                Arbitraries.just("{name: \"test\"}")
        );
    }

    @Provide
    Arbitrary<List<String>> malformedJsonList() {
        return malformedJson().list().ofMinSize(1).ofMaxSize(5);
    }

    private boolean isValidRecipeJson(String s) {
        // Quick check: if it could parse as a valid Recipe, reject it from our malformed generator
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Recipe r = gson.fromJson(s, Recipe.class);
            return r != null && r.getId() != null && r.getName() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // === Helper methods ===

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // best effort cleanup
                            }
                        });
            }
        }
    }
}
