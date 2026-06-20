package com.recipemanager.controller;

import com.recipemanager.model.Annotation;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.service.DefaultValidationService;
import com.recipemanager.service.FileSystemRecipeStore;
import com.recipemanager.service.ValidationService;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Property-based tests for RecipeController.
 */
class RecipeControllerPropertyTest {

    // === Property 3: Recipe list is sorted case-insensitively ===

    /**
     * For any list of recipes with random names (various cases),
     * getRecipesSorted() returns a list where every adjacent pair satisfies
     * a.getName().compareToIgnoreCase(b.getName()) <= 0.
     *
     * Validates: Requirements 2.1, 2.5, 5.2
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 3: Recipe list is sorted case-insensitively")
    void getRecipesSortedReturnsCaseInsensitiveOrder(
            @ForAll("recipeNamesWithMixedCase") List<String> names
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-sort-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Save recipes with the generated names
            for (String name : names) {
                Recipe recipe = createValidRecipe(name);
                store.save(recipe);
            }

            // Get sorted list
            List<Recipe> sorted = controller.getRecipesSorted();

            // Assert correct count
            assert sorted.size() == names.size() :
                    "Expected " + names.size() + " recipes but got " + sorted.size();

            // Assert every adjacent pair satisfies case-insensitive ordering
            for (int i = 0; i < sorted.size() - 1; i++) {
                String a = sorted.get(i).getName();
                String b = sorted.get(i + 1).getName();
                int cmp = a.compareToIgnoreCase(b);
                assert cmp <= 0 :
                        "Recipes not sorted case-insensitively at index " + i + ": " +
                        "'" + a + "' should come before or equal '" + b + "' " +
                        "(compareToIgnoreCase returned " + cmp + ")";
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Provide
    Arbitrary<List<String>> recipeNamesWithMixedCase() {
        Arbitrary<String> mixedCaseName = Arbitraries.oneOf(
                // All lowercase
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50),
                // All uppercase
                Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(1).ofMaxLength(50),
                // Mixed case alphabetic
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                // Names starting with upper then lower (capitalized words)
                Combinators.combine(
                        Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(1).ofMaxLength(1),
                        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(49)
                ).as((first, rest) -> first + rest),
                // Common recipe-like names with varied casing
                Arbitraries.of(
                        "Apple Pie", "apple pie", "APPLE PIE",
                        "Banana Bread", "banana bread", "BANANA BREAD",
                        "Chicken Soup", "chicken soup", "CHICKEN SOUP",
                        "Zucchini Noodles", "zucchini noodles", "ZUCCHINI NOODLES",
                        "Pasta", "pasta", "PASTA",
                        "Omelette", "omelette", "OMELETTE"
                )
        );

        return mixedCaseName.list().ofMinSize(1).ofMaxSize(20);
    }

    private Recipe createValidRecipe(String name) {
        Recipe recipe = new Recipe();
        recipe.setId(UUID.randomUUID());
        recipe.setName(name);
        recipe.setDescription("A test recipe");
        recipe.setIngredients(List.of(new Ingredient("Flour", 1.0, "cup")));
        recipe.setServings(4);
        recipe.setInstructions("Mix and bake.");
        recipe.setTags(new ArrayList<>());
        recipe.setAnnotations(new ArrayList<>());
        return recipe;
    }

    // === Property 4: Tag filter returns only matching recipes ===

    /**
     * Validates: Requirements 2.2
     *
     * For any collection of recipes and any selected tag, filtering by that tag
     * returns exactly the subset of recipes whose tag list contains the selected tag,
     * and no others. The result is also sorted by name (case-insensitive).
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 4: Tag filter returns only matching recipes")
    void tagFilterReturnsOnlyMatchingRecipes(
            @ForAll("recipesWithTags") List<Recipe> recipes,
            @ForAll("tagToFilter") String filterTag
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-tag-filter-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Save all recipes
            for (Recipe recipe : recipes) {
                store.save(recipe);
            }

            // Compute expected results: recipes whose tags contain the filter tag
            List<Recipe> expectedMatches = recipes.stream()
                    .filter(r -> r.getTags().contains(filterTag))
                    .sorted(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());

            // Call filterByTag
            List<Recipe> actualResults = controller.filterByTag(filterTag);

            // Assert: same number of results
            assert actualResults.size() == expectedMatches.size() :
                    "Expected " + expectedMatches.size() + " recipes matching tag '" + filterTag +
                    "' but got " + actualResults.size();

            // Assert: result contains exactly those recipes whose tags contain the filter tag
            for (int i = 0; i < expectedMatches.size(); i++) {
                assert actualResults.get(i).getId().equals(expectedMatches.get(i).getId()) :
                        "Mismatch at index " + i + ": expected recipe '" +
                        expectedMatches.get(i).getName() + "' but got '" +
                        actualResults.get(i).getName() + "'";
            }

            // Assert: every result actually contains the filter tag
            for (Recipe result : actualResults) {
                assert result.getTags().contains(filterTag) :
                        "Recipe '" + result.getName() + "' in filter results does not contain tag '" + filterTag + "'";
            }

            // Assert: no recipe that has the tag is missing from results
            Set<UUID> resultIds = actualResults.stream()
                    .map(Recipe::getId)
                    .collect(Collectors.toSet());
            for (Recipe recipe : recipes) {
                if (recipe.getTags().contains(filterTag)) {
                    assert resultIds.contains(recipe.getId()) :
                            "Recipe '" + recipe.getName() + "' has tag '" + filterTag +
                            "' but was not included in filter results";
                }
            }

            // Assert: result is sorted by name (case-insensitive)
            for (int i = 0; i < actualResults.size() - 1; i++) {
                String currentName = actualResults.get(i).getName();
                String nextName = actualResults.get(i + 1).getName();
                assert currentName.compareToIgnoreCase(nextName) <= 0 :
                        "Results not sorted: '" + currentName + "' should come before '" + nextName + "'";
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // === Providers ===

    @Provide
    Arbitrary<List<Recipe>> recipesWithTags() {
        Arbitrary<String> tagArb = Arbitraries.of("breakfast", "lunch", "dinner", "dessert", "vegan", "quick");
        Arbitrary<List<String>> tagListArb = tagArb.list().ofMinSize(0).ofMaxSize(4)
                .map(tags -> tags.stream().distinct().collect(Collectors.toList()));

        Arbitrary<Recipe> recipeArb = Combinators.combine(
                validName(),
                validIngredientList(),
                validServings(),
                validInstructions(),
                tagListArb
        ).as((name, ingredients, servings, instructions, tags) -> {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(name);
            recipe.setDescription("");
            recipe.setIngredients(ingredients);
            recipe.setServings(servings);
            recipe.setInstructions(instructions);
            recipe.setTags(tags);
            recipe.setAnnotations(new ArrayList<>());
            return recipe;
        });

        return recipeArb.list().ofMinSize(1).ofMaxSize(8)
                .map(recipes -> {
                    // Ensure unique IDs
                    for (Recipe r : recipes) {
                        r.setId(UUID.randomUUID());
                    }
                    return recipes;
                });
    }

    @Provide
    Arbitrary<String> tagToFilter() {
        // Use the same tag pool so filtering produces meaningful results
        return Arbitraries.of("breakfast", "lunch", "dinner", "dessert", "vegan", "quick");
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
                Arbitraries.integers().between(1, 9999900).map(i -> i / 100.0),
                Arbitraries.of("g", "kg", "ml", "L", "cup", "tbsp", "tsp")
        ).as(Ingredient::new);
    }

    private Arbitrary<Integer> validServings() {
        return Arbitraries.integers().between(1, 100);
    }

    private Arbitrary<String> validInstructions() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200);
    }

    // === Property 10: Annotations are ordered newest-first ===

    /**
     * For any recipe with two or more annotations at random timestamps,
     * getAnnotationsSorted() returns them in reverse chronological order —
     * for every adjacent pair (a, b), a.getCreatedAt() >= b.getCreatedAt().
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 10: Annotations are ordered newest-first")
    void annotationsAreOrderedNewestFirst(
            @ForAll("recipeWithMultipleAnnotations") Recipe recipe
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-annotation-order-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Save the recipe with its annotations
            store.save(recipe);

            // Get annotations sorted via the controller
            List<Annotation> sorted = controller.getAnnotationsSorted(recipe.getId());

            // Verify we got all annotations back
            assert sorted.size() == recipe.getAnnotations().size() :
                    "Expected " + recipe.getAnnotations().size() + " annotations but got " + sorted.size();

            // Verify ordering: for every adjacent pair (a, b), a.createdAt >= b.createdAt
            for (int i = 0; i < sorted.size() - 1; i++) {
                Annotation a = sorted.get(i);
                Annotation b = sorted.get(i + 1);
                assert a.getCreatedAt().compareTo(b.getCreatedAt()) >= 0 :
                        "Annotation ordering violated at index " + i + ": " +
                        a.getCreatedAt() + " should be >= " + b.getCreatedAt();
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    /**
     * Verifies that annotations with identical timestamps maintain stable ordering
     * (no exception or ordering violation when createdAt values are equal).
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 10: Annotations are ordered newest-first")
    void annotationsWithSameTimestampDoNotViolateOrdering(
            @ForAll("recipeWithDuplicateTimestamps") Recipe recipe
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-dup-timestamp-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Save the recipe
            store.save(recipe);

            // Get annotations sorted
            List<Annotation> sorted = controller.getAnnotationsSorted(recipe.getId());

            // Verify ordering still holds (>= allows equal timestamps)
            for (int i = 0; i < sorted.size() - 1; i++) {
                Annotation a = sorted.get(i);
                Annotation b = sorted.get(i + 1);
                assert a.getCreatedAt().compareTo(b.getCreatedAt()) >= 0 :
                        "Annotation ordering violated at index " + i + " with duplicate timestamps: " +
                        a.getCreatedAt() + " should be >= " + b.getCreatedAt();
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Provide
    Arbitrary<Recipe> recipeWithMultipleAnnotations() {
        return Combinators.combine(
                validName(),
                validIngredientList(),
                validServings(),
                validInstructions(),
                annotationListWithRandomTimestamps()
        ).as((name, ingredients, servings, instructions, annotations) -> {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(name);
            recipe.setDescription("");
            recipe.setIngredients(ingredients);
            recipe.setServings(servings);
            recipe.setInstructions(instructions);
            recipe.setTags(new ArrayList<>());
            recipe.setAnnotations(annotations);
            return recipe;
        });
    }

    @Provide
    Arbitrary<Recipe> recipeWithDuplicateTimestamps() {
        return Combinators.combine(
                validName(),
                validIngredientList(),
                validServings(),
                validInstructions(),
                annotationListWithDuplicateTimestamps()
        ).as((name, ingredients, servings, instructions, annotations) -> {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(name);
            recipe.setDescription("");
            recipe.setIngredients(ingredients);
            recipe.setServings(servings);
            recipe.setInstructions(instructions);
            recipe.setTags(new ArrayList<>());
            recipe.setAnnotations(annotations);
            return recipe;
        });
    }

    private Arbitrary<List<Annotation>> annotationListWithRandomTimestamps() {
        return validAnnotation().list().ofMinSize(2).ofMaxSize(10);
    }

    private Arbitrary<List<Annotation>> annotationListWithDuplicateTimestamps() {
        return Combinators.combine(
                randomLocalDateTime(),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100).list().ofMinSize(2).ofMaxSize(6)
        ).as((sharedTimestamp, texts) -> {
            List<Annotation> annotations = new ArrayList<>();
            for (String text : texts) {
                annotations.add(new Annotation(text, sharedTimestamp));
            }
            return annotations;
        });
    }

    private Arbitrary<Annotation> validAnnotation() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200),
                randomLocalDateTime()
        ).as(Annotation::new);
    }

    private Arbitrary<LocalDateTime> randomLocalDateTime() {
        return Combinators.combine(
                Arbitraries.integers().between(2000, 2030),
                Arbitraries.integers().between(1, 12),
                Arbitraries.integers().between(1, 28),
                Arbitraries.integers().between(0, 23),
                Arbitraries.integers().between(0, 59),
                Arbitraries.integers().between(0, 59)
        ).as(LocalDateTime::of);
    }

    // === Property 12: Annotations are preserved during recipe edit ===

    /**
     * For any recipe with existing annotations, editing any combination of
     * name, description, ingredients, servings, instructions, or tags and saving
     * results in the annotations list being identical to its pre-edit state.
     *
     * Validates: Requirements 8.3
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 12: Annotations are preserved during recipe edit")
    void annotationsArePreservedDuringRecipeEdit(
            @ForAll("recipeWithAnnotationsForEdit") Recipe originalRecipe,
            @ForAll("recipeEdits") RecipeEdits edits
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-annotations-preserved-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Save the original recipe with annotations
            store.save(originalRecipe);

            // Capture the original annotations
            List<Annotation> originalAnnotations = new ArrayList<>(originalRecipe.getAnnotations());

            // Create a modified copy (simulating the user editing fields)
            Recipe editedRecipe = new Recipe();
            editedRecipe.setId(originalRecipe.getId());
            editedRecipe.setName(edits.editName ? edits.newName : originalRecipe.getName());
            editedRecipe.setDescription(edits.editDescription ? edits.newDescription : originalRecipe.getDescription());
            editedRecipe.setIngredients(edits.editIngredients ? edits.newIngredients : originalRecipe.getIngredients());
            editedRecipe.setServings(edits.editServings ? edits.newServings : originalRecipe.getServings());
            editedRecipe.setInstructions(edits.editInstructions ? edits.newInstructions : originalRecipe.getInstructions());
            editedRecipe.setTags(edits.editTags ? edits.newTags : originalRecipe.getTags());
            // Annotations NOT set on the edited recipe (simulates editor not passing them)
            editedRecipe.setAnnotations(new ArrayList<>());

            // Update via controller (should preserve annotations from persisted version)
            controller.updateRecipe(editedRecipe);

            // Reload from disk
            List<Recipe> allRecipes = store.loadAll();
            Recipe reloaded = null;
            for (Recipe r : allRecipes) {
                if (r.getId().equals(originalRecipe.getId())) {
                    reloaded = r;
                    break;
                }
            }

            assert reloaded != null : "Recipe not found after update";

            // Assert annotations are identical to the originals
            List<Annotation> reloadedAnnotations = reloaded.getAnnotations();
            assert reloadedAnnotations.size() == originalAnnotations.size() :
                    "Annotation count changed: expected " + originalAnnotations.size() +
                    " but got " + reloadedAnnotations.size();

            for (int i = 0; i < originalAnnotations.size(); i++) {
                Annotation expected = originalAnnotations.get(i);
                Annotation actual = reloadedAnnotations.get(i);
                assert expected.equals(actual) :
                        "Annotation at index " + i + " differs: expected " + expected + " but got " + actual;
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Provide
    Arbitrary<Recipe> recipeWithAnnotationsForEdit() {
        return Combinators.combine(
                validName(),
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100),
                validIngredientList(),
                validServings(),
                validInstructions(),
                validTagList(),
                annotationListForEdit()
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

    @Provide
    Arbitrary<RecipeEdits> recipeEdits() {
        // Use flatMap to first generate edit flags, then combine with values
        return Arbitraries.of(true, false).list().ofSize(6).flatMap(flags -> {
            return Combinators.combine(
                    validName(),
                    Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100),
                    validIngredientList(),
                    validServings(),
                    validInstructions(),
                    validTagList()
            ).as((newName, newDesc, newIng, newServ, newInstr, newTags) -> {
                RecipeEdits edits = new RecipeEdits();
                boolean editName = flags.get(0);
                boolean editDesc = flags.get(1);
                boolean editIngFlag = flags.get(2);
                boolean editServFlag = flags.get(3);
                boolean editInstrFlag = flags.get(4);
                boolean editTagsFlag = flags.get(5);
                // Ensure at least one edit is applied
                if (!editName && !editDesc && !editIngFlag && !editServFlag && !editInstrFlag && !editTagsFlag) {
                    edits.editName = true;
                } else {
                    edits.editName = editName;
                }
                edits.editDescription = editDesc;
                edits.editIngredients = editIngFlag;
                edits.editServings = editServFlag;
                edits.editInstructions = editInstrFlag;
                edits.editTags = editTagsFlag;
                edits.newName = newName;
                edits.newDescription = newDesc;
                edits.newIngredients = newIng;
                edits.newServings = newServ;
                edits.newInstructions = newInstr;
                edits.newTags = newTags;
                return edits;
            });
        });
    }

    private Arbitrary<List<String>> validTagList() {
        return Arbitraries.of("breakfast", "lunch", "dinner", "dessert", "vegan", "quick")
                .list().ofMinSize(0).ofMaxSize(3)
                .map(tags -> tags.stream().distinct().collect(Collectors.toList()));
    }

    private Arbitrary<List<Annotation>> annotationListForEdit() {
        Arbitrary<Annotation> annotationArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200),
                randomLocalDateTime()
        ).as(Annotation::new);
        return annotationArb.list().ofMinSize(1).ofMaxSize(5);
    }

    /**
     * Helper class holding random edit choices for a recipe.
     */
    static class RecipeEdits {
        boolean editName;
        boolean editDescription;
        boolean editIngredients;
        boolean editServings;
        boolean editInstructions;
        boolean editTags;
        String newName;
        String newDescription;
        List<Ingredient> newIngredients;
        int newServings;
        String newInstructions;
        List<String> newTags;
    }

    // === Property 13: Cancel edit discards all changes ===

    /**
     * For any recipe and any set of modifications made in the editor,
     * cancelling the edit (not calling updateRecipe) results in the recipe's
     * persisted state being identical to its state before the edit began.
     *
     * Validates: Requirements 8.4
     */
    @Property(tries = 100)
    @Tag("Feature: recipe-manager, Property 13: Cancel edit discards all changes")
    void cancelEditDiscardsAllChanges(
            @ForAll("validRecipeForCancelTest") Recipe originalRecipe,
            @ForAll("recipeModifications") RecipeModification modification
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("recipe-controller-cancel-test");
        try {
            FileSystemRecipeStore store = new FileSystemRecipeStore(tempDir);
            ValidationService validationService = new DefaultValidationService();
            RecipeController controller = new RecipeController(store, validationService);

            // Step 1: Save the original recipe to disk
            store.save(originalRecipe);

            // Step 2: Load it from disk (this is the "pre-edit" state)
            List<Recipe> loaded = store.loadAll();
            Recipe preEditState = loaded.stream()
                    .filter(r -> r.getId().equals(originalRecipe.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Recipe not found after save"));

            // Step 3: Create a modified copy in memory (simulating editor changes)
            Recipe modifiedCopy = cloneRecipe(preEditState);
            modification.apply(modifiedCopy);

            // Step 4: Do NOT call controller.updateRecipe — simulating "cancel"
            // The user discards the modified copy without saving

            // Step 5: Reload from disk and assert recipe is identical to pre-edit state
            List<Recipe> reloaded = store.loadAll();
            Recipe afterCancelState = reloaded.stream()
                    .filter(r -> r.getId().equals(originalRecipe.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Recipe not found after cancel"));

            // Compare all fields
            assert preEditState.getId().equals(afterCancelState.getId()) :
                    "ID changed after cancel: expected " + preEditState.getId() + " but got " + afterCancelState.getId();
            assert preEditState.getName().equals(afterCancelState.getName()) :
                    "Name changed after cancel: expected '" + preEditState.getName() + "' but got '" + afterCancelState.getName() + "'";
            assert Objects.equals(preEditState.getDescription(), afterCancelState.getDescription()) :
                    "Description changed after cancel";
            assert preEditState.getServings() == afterCancelState.getServings() :
                    "Servings changed after cancel: expected " + preEditState.getServings() + " but got " + afterCancelState.getServings();
            assert Objects.equals(preEditState.getInstructions(), afterCancelState.getInstructions()) :
                    "Instructions changed after cancel";

            // Compare ingredients (order matters)
            assert preEditState.getIngredients().size() == afterCancelState.getIngredients().size() :
                    "Ingredients count changed after cancel: expected " + preEditState.getIngredients().size() +
                    " but got " + afterCancelState.getIngredients().size();
            for (int i = 0; i < preEditState.getIngredients().size(); i++) {
                Ingredient expected = preEditState.getIngredients().get(i);
                Ingredient actual = afterCancelState.getIngredients().get(i);
                assert expected.equals(actual) :
                        "Ingredient at index " + i + " changed after cancel: expected " + expected + " but got " + actual;
            }

            // Compare tags
            assert preEditState.getTags().equals(afterCancelState.getTags()) :
                    "Tags changed after cancel: expected " + preEditState.getTags() + " but got " + afterCancelState.getTags();

            // Compare annotations
            assert preEditState.getAnnotations().size() == afterCancelState.getAnnotations().size() :
                    "Annotations count changed after cancel";
            for (int i = 0; i < preEditState.getAnnotations().size(); i++) {
                Annotation expected = preEditState.getAnnotations().get(i);
                Annotation actual = afterCancelState.getAnnotations().get(i);
                assert expected.equals(actual) :
                        "Annotation at index " + i + " changed after cancel";
            }

            // Also verify equality via equals() for completeness
            assert preEditState.equals(afterCancelState) :
                    "Recipe state changed after cancel (equals check failed)";
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Provide
    Arbitrary<Recipe> validRecipeForCancelTest() {
        Arbitrary<String> tagArb = Arbitraries.of("breakfast", "lunch", "dinner", "dessert", "vegan", "quick");
        Arbitrary<List<String>> tagListArb = tagArb.list().ofMinSize(0).ofMaxSize(3)
                .map(tags -> tags.stream().distinct().collect(Collectors.toList()));

        return Combinators.combine(
                validName(),
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100),
                validIngredientList(),
                validServings(),
                validInstructions(),
                tagListArb,
                validAnnotation().list().ofMinSize(0).ofMaxSize(3)
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

    @Provide
    Arbitrary<RecipeModification> recipeModifications() {
        return Arbitraries.oneOf(
                // Modify name
                validName().map(newName -> (RecipeModification) recipe -> recipe.setName(newName)),
                // Modify description
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100)
                        .map(newDesc -> (RecipeModification) recipe -> recipe.setDescription(newDesc)),
                // Modify servings
                validServings().map(newServings -> (RecipeModification) recipe -> recipe.setServings(newServings)),
                // Modify instructions
                validInstructions().map(newInstr -> (RecipeModification) recipe -> recipe.setInstructions(newInstr)),
                // Modify ingredients (replace entire list)
                validIngredientList().map(newIngr -> (RecipeModification) recipe -> recipe.setIngredients(newIngr)),
                // Modify tags
                Arbitraries.of("breakfast", "lunch", "dinner", "dessert", "vegan", "quick")
                        .list().ofMinSize(0).ofMaxSize(4)
                        .map(tags -> tags.stream().distinct().collect(Collectors.toList()))
                        .map(newTags -> (RecipeModification) recipe -> recipe.setTags(newTags)),
                // Multiple modifications at once
                Combinators.combine(
                        validName(),
                        validServings(),
                        validIngredientList()
                ).as((newName, newServings, newIngr) -> (RecipeModification) recipe -> {
                    recipe.setName(newName);
                    recipe.setServings(newServings);
                    recipe.setIngredients(newIngr);
                })
        );
    }

    /**
     * Functional interface representing a modification that can be applied to a recipe.
     */
    @FunctionalInterface
    interface RecipeModification {
        void apply(Recipe recipe);
    }

    private Recipe cloneRecipe(Recipe original) {
        return new Recipe(
                original.getId(),
                original.getName(),
                original.getDescription(),
                new ArrayList<>(original.getIngredients().stream()
                        .map(i -> new Ingredient(i.getName(), i.getQuantity(), i.getUnit()))
                        .collect(Collectors.toList())),
                original.getServings(),
                original.getInstructions(),
                new ArrayList<>(original.getTags()),
                new ArrayList<>(original.getAnnotations().stream()
                        .map(a -> new Annotation(a.getText(), a.getCreatedAt()))
                        .collect(Collectors.toList()))
        );
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
