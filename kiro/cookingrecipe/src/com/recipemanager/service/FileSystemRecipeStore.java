package com.recipemanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.recipemanager.model.Recipe;
import com.recipemanager.util.ErrorLogger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Filesystem-based implementation of RecipeStore.
 * Stores each recipe as a separate JSON file named {uuid}.json
 * in the configured storage directory.
 */
public class FileSystemRecipeStore implements RecipeStore {

    private final Path storageDir;
    private final Gson gson;

    /**
     * Creates a FileSystemRecipeStore using the default storage directory:
     * {user.home}/.recipe-manager/recipes/
     */
    public FileSystemRecipeStore() {
        this(Paths.get(System.getProperty("user.home"), ".recipe-manager", "recipes"));
    }

    /**
     * Creates a FileSystemRecipeStore using the specified storage directory.
     * Useful for testing with @TempDir.
     *
     * @param storageDir the directory where recipe JSON files are stored
     */
    public FileSystemRecipeStore(Path storageDir) {
        this.storageDir = storageDir;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<Recipe> loadAll() {
        List<Recipe> recipes = new ArrayList<>();

        if (!Files.exists(storageDir) || !Files.isDirectory(storageDir)) {
            return recipes;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*.json")) {
            for (Path file : stream) {
                try {
                    String json = Files.readString(file);
                    Recipe recipe = gson.fromJson(json, Recipe.class);
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                } catch (JsonParseException | IllegalStateException e) {
                    ErrorLogger.warn("Skipping malformed recipe file: " + file.getFileName() + " - " + e.getMessage());
                } catch (IOException e) {
                    ErrorLogger.warn("Failed to read recipe file: " + file.getFileName() + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            ErrorLogger.warn("Failed to list recipe directory: " + e.getMessage());
        }

        return recipes;
    }

    @Override
    public void save(Recipe recipe) throws PersistenceException {
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            Path file = storageDir.resolve(recipe.getId().toString() + ".json");
            String json = gson.toJson(recipe);
            Files.writeString(file, json);
        } catch (IOException e) {
            throw new PersistenceException("Failed to save recipe: " + recipe.getName(), e);
        }
    }

    @Override
    public void delete(UUID recipeId) throws PersistenceException {
        try {
            Path file = storageDir.resolve(recipeId.toString() + ".json");
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new PersistenceException("Failed to delete recipe: " + recipeId, e);
        }
    }

    /**
     * Custom Gson TypeAdapter for LocalDateTime using ISO-8601 format.
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateStr = in.nextString();
            try {
                return LocalDateTime.parse(dateStr, FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IOException("Invalid LocalDateTime format: " + dateStr, e);
            }
        }
    }
}
