package com.recipes.persistence;

import com.recipes.model.Recipe;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RecipeStore {

    private static final Logger LOG = Logger.getLogger(RecipeStore.class.getName());

    private final File recipesDir;
    private final List<String> loadWarnings = new ArrayList<>();

    public RecipeStore(File recipesDir) {
        this.recipesDir = recipesDir;
        ensureDir();
    }

    public void save(Recipe recipe) throws IOException {
        ensureDir();
        File file = new File(recipesDir, recipe.getId() + ".json");
        try (Writer w = new BufferedWriter(new FileWriter(file))) {
            w.write(RecipeSerializer.toJson(recipe).toString(2));
        }
    }

    public List<Recipe> loadAll() {
        ensureDir();
        loadWarnings.clear();
        List<Recipe> result = new ArrayList<>();
        File[] files = recipesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return result;
        for (File f : files) {
            try {
                String content = readFile(f);
                result.add(RecipeSerializer.fromJson(new JSONObject(content)));
            } catch (Exception e) {
                String msg = "Skipped malformed file: " + f.getName() + " (" + e.getMessage() + ")";
                LOG.warning(msg);
                loadWarnings.add(msg);
            }
        }
        return result;
    }

    public void delete(String id) throws IOException {
        File file = new File(recipesDir, id + ".json");
        if (file.exists() && !file.delete()) {
            throw new IOException("Could not delete: " + file.getAbsolutePath());
        }
    }

    public List<String> getLoadWarnings() {
        return new ArrayList<>(loadWarnings);
    }

    private void ensureDir() {
        if (!recipesDir.exists()) {
            recipesDir.mkdirs();
        }
    }

    private String readFile(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
}
