package com.recipemanager.service;

import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeTry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Property-based tests for TagService.
 *
 * Validates: Requirements 4.2, 4.4
 */
class TagServicePropertyTest {

    // === Property 7: Tag creation rejects case-insensitive duplicates ===

    /**
     * Property 7: Tag creation rejects case-insensitive duplicates
     *
     * For any existing tag name and any case variant of that name (same characters,
     * different casing), attempting to create a new tag with the case variant SHALL
     * be rejected as a duplicate.
     *
     * Validates: Requirements 4.2
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 7: Tag creation rejects case-insensitive duplicates")
    void tagCreationRejectsCaseInsensitiveDuplicates(
            @ForAll("validTagName") String tagName,
            @ForAll("casePermutationStrategy") String permutationStrategy
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("tag-dup-test");
        try {
            Path recipesDir = tempDir.resolve("recipes");
            Files.createDirectories(recipesDir);
            Path tagsFile = tempDir.resolve("tags.json");

            FileSystemRecipeStore recipeStore = new FileSystemRecipeStore(recipesDir);
            FileSystemTagService tagService = new FileSystemTagService(tagsFile, recipeStore);

            // Create the original tag
            tagService.createTag(tagName);

            // Generate a case variant of the tag name
            String caseVariant = applyCasePermutation(tagName, permutationStrategy);

            // Attempting to create the case variant should throw DuplicateTagException
            try {
                tagService.createTag(caseVariant);
                throw new AssertionError(
                        "Expected DuplicateTagException when creating case variant '" +
                        caseVariant + "' of existing tag '" + tagName + "'");
            } catch (DuplicateTagException e) {
                // Expected behavior - duplicate correctly rejected
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    /**
     * Property 7 additional: Creating the exact same tag name should also be rejected.
     *
     * Validates: Requirements 4.2
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 7: Tag creation rejects case-insensitive duplicates")
    void tagCreationRejectsExactDuplicate(
            @ForAll("validTagName") String tagName
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("tag-exact-dup-test");
        try {
            Path recipesDir = tempDir.resolve("recipes");
            Files.createDirectories(recipesDir);
            Path tagsFile = tempDir.resolve("tags.json");

            FileSystemRecipeStore recipeStore = new FileSystemRecipeStore(recipesDir);
            FileSystemTagService tagService = new FileSystemTagService(tagsFile, recipeStore);

            // Create the original tag
            tagService.createTag(tagName);

            // Attempting to create the exact same tag should throw DuplicateTagException
            try {
                tagService.createTag(tagName);
                throw new AssertionError(
                        "Expected DuplicateTagException when creating duplicate tag '" + tagName + "'");
            } catch (DuplicateTagException e) {
                // Expected behavior - duplicate correctly rejected
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Property 8: Tag deletion cascades to all recipes ===

    /**
     * Validates: Requirements 4.4
     *
     * For any tag that is associated with one or more recipes, deleting that tag
     * SHALL result in the tag being absent from the tag list AND absent from the
     * tags field of every recipe that previously contained it.
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 8: Tag deletion cascades to all recipes")
    void tagDeletionCascadesToAllRecipes(
            @ForAll("tagAndRecipesWithTag") TagAndRecipes input
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("tag-cascade-test");
        try {
            Path recipesDir = tempDir.resolve("recipes");
            Files.createDirectories(recipesDir);
            Path tagsFile = tempDir.resolve("tags.json");

            FileSystemRecipeStore recipeStore = new FileSystemRecipeStore(recipesDir);
            FileSystemTagService tagService = new FileSystemTagService(tagsFile, recipeStore);

            // Save all recipes (they already have the target tag assigned)
            for (Recipe recipe : input.recipes) {
                recipeStore.save(recipe);
            }

            // Create the target tag in the tag service
            try {
                tagService.createTag(input.targetTag);
            } catch (DuplicateTagException e) {
                // Tag may already exist as a default - that's fine
            }

            // Verify the tag exists before deletion
            List<String> tagsBefore = tagService.getAllTags();
            boolean tagExistsBefore = tagsBefore.stream()
                    .anyMatch(t -> t.equalsIgnoreCase(input.targetTag));
            assert tagExistsBefore :
                    "Target tag '" + input.targetTag + "' should exist before deletion. Tags: " + tagsBefore;

            // Delete the tag
            tagService.deleteTag(input.targetTag);

            // Assert: tag is no longer in the available tags list
            List<String> tagsAfter = tagService.getAllTags();
            boolean tagExistsAfter = tagsAfter.stream()
                    .anyMatch(t -> t.equalsIgnoreCase(input.targetTag));
            assert !tagExistsAfter :
                    "Tag '" + input.targetTag + "' should not exist in tags list after deletion. Tags: " + tagsAfter;

            // Assert: tag is absent from all recipes' tag lists (reload from disk)
            List<Recipe> reloadedRecipes = recipeStore.loadAll();
            for (Recipe reloaded : reloadedRecipes) {
                boolean recipeHasTag = reloaded.getTags().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(input.targetTag));
                assert !recipeHasTag :
                        "Recipe '" + reloaded.getName() + "' should not contain tag '" +
                        input.targetTag + "' after deletion. Recipe tags: " + reloaded.getTags();
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Data class to hold test input ===

    static class TagAndRecipes {
        final String targetTag;
        final List<Recipe> recipes;

        TagAndRecipes(String targetTag, List<Recipe> recipes) {
            this.targetTag = targetTag;
            this.recipes = recipes;
        }

        @Override
        public String toString() {
            return "TagAndRecipes{targetTag='" + targetTag + "', recipeCount=" + recipes.size() + "}";
        }
    }

    // === Providers ===

    @Provide
    Arbitrary<String> validTagName() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .filter(s -> !isDefaultTag(s));
    }

    @Provide
    Arbitrary<String> casePermutationStrategy() {
        return Arbitraries.of("UPPER", "LOWER", "SWAP", "ALTERNATE");
    }

    @Provide
    Arbitrary<TagAndRecipes> tagAndRecipesWithTag() {
        // Generate a tag name that does not conflict with default tags
        Arbitrary<String> tagArb = Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20)
                .filter(s -> !isDefaultTag(s));

        return tagArb.flatMap(tag ->
            validRecipeWithTag(tag).list().ofMinSize(2).ofMaxSize(5)
                .map(recipes -> {
                    // Ensure unique IDs
                    for (Recipe r : recipes) {
                        r.setId(UUID.randomUUID());
                    }
                    return new TagAndRecipes(tag, recipes);
                })
        );
    }

    // === Case permutation logic ===

    private String applyCasePermutation(String input, String strategy) {
        switch (strategy) {
            case "UPPER":
                return input.toUpperCase();
            case "LOWER":
                return input.toLowerCase();
            case "SWAP":
                StringBuilder sb = new StringBuilder();
                for (char c : input.toCharArray()) {
                    if (Character.isUpperCase(c)) {
                        sb.append(Character.toLowerCase(c));
                    } else {
                        sb.append(Character.toUpperCase(c));
                    }
                }
                return sb.toString();
            case "ALTERNATE":
                StringBuilder alt = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (i % 2 == 0) {
                        alt.append(Character.toUpperCase(c));
                    } else {
                        alt.append(Character.toLowerCase(c));
                    }
                }
                return alt.toString();
            default:
                return input.toUpperCase();
        }
    }

    // === Recipe generators for Property 8 ===

    private Arbitrary<Recipe> validRecipeWithTag(String tag) {
        return Combinators.combine(
                validName(),
                validIngredientList(),
                validServings(),
                validInstructions(),
                additionalTags()
        ).as((name, ingredients, servings, instructions, otherTags) -> {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(name);
            recipe.setDescription("");
            recipe.setIngredients(ingredients);
            recipe.setServings(servings);
            recipe.setInstructions(instructions);
            // Always include the target tag, plus possibly other tags
            List<String> allTags = new ArrayList<>(otherTags);
            allTags.add(tag);
            recipe.setTags(allTags);
            recipe.setAnnotations(new ArrayList<>());
            return recipe;
        });
    }

    private Arbitrary<String> validName() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    private Arbitrary<List<Ingredient>> validIngredientList() {
        return validIngredient().list().ofMinSize(1).ofMaxSize(5);
    }

    private Arbitrary<Ingredient> validIngredient() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30),
                Arbitraries.integers().between(1, 99900).map(i -> i / 100.0),
                Arbitraries.of("g", "kg", "ml", "L", "cup", "tbsp", "tsp")
        ).as(Ingredient::new);
    }

    private Arbitrary<Integer> validServings() {
        return Arbitraries.integers().between(1, 100);
    }

    private Arbitrary<String> validInstructions() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200);
    }

    private Arbitrary<List<String>> additionalTags() {
        return Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(15)
                .filter(s -> !isDefaultTag(s))
                .list().ofMinSize(0).ofMaxSize(3);
    }

    private boolean isDefaultTag(String tag) {
        return tag.equalsIgnoreCase("vegetarian") ||
               tag.equalsIgnoreCase("vegan") ||
               tag.equalsIgnoreCase("dessert") ||
               tag.equalsIgnoreCase("main course") ||
               tag.equalsIgnoreCase("appetizer");
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
