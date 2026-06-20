package com.cookingrecipes.storage;

import com.cookingrecipes.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class RecipeStore {
    private static final String DATA_FILE = "data/recipes.json";
    private static final String TEMP_FILE = "data/recipes.tmp";

    private List<Recipe> recipes = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private final JsonSerializer serializer = new JsonSerializer();

    public void load() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdirs();

        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try {
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            JsonSerializer.StoreData data = serializer.deserialize(json);
            recipes = data.recipes;
            tags = data.tags;
        } catch (IOException e) {
            // Start with empty store on read error
            recipes = new ArrayList<>();
            tags = new ArrayList<>();
        }
    }

    public void save() {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) dataDir.mkdirs();

            String json = serializer.serialize(recipes, tags);
            Path temp = Paths.get(TEMP_FILE);
            Files.write(temp, json.getBytes(StandardCharsets.UTF_8));
            Files.move(temp, Paths.get(DATA_FILE), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save recipes: " + e.getMessage(), e);
        }
    }

    public List<Recipe> getRecipes() { return recipes; }
    public List<Tag> getTags() { return tags; }

    public void addRecipe(Recipe r) { recipes.add(r); }

    public void removeRecipe(String id) {
        recipes.removeIf(r -> id.equals(r.getId()));
    }

    public Recipe findById(String id) {
        return recipes.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
    }

    public void addTag(Tag tag) {
        boolean exists = tags.stream().anyMatch(t -> t.equals(tag));
        if (!exists) tags.add(tag);
    }
}
