package com.recipes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe {

    private final String id;
    private String name;
    private String description;
    private List<String> ingredients;
    private int servings;
    private List<String> instructions;
    private List<String> tags;
    private List<Annotation> annotations;

    private Recipe(String id, String name, String description,
                   List<String> ingredients, int servings,
                   List<String> instructions, List<String> tags,
                   List<Annotation> annotations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ingredients = new ArrayList<>(ingredients);
        this.servings = servings;
        this.instructions = new ArrayList<>(instructions);
        this.tags = new ArrayList<>(tags);
        this.annotations = new ArrayList<>(annotations);
    }

    /** Factory: create a new recipe (generates id, starts with empty annotations). */
    public static Recipe create(String name, String description,
                                List<String> ingredients, int servings,
                                List<String> instructions, List<String> tags) {
        validateBasic(name, servings);
        return new Recipe(UUID.randomUUID().toString(), name.trim(), description,
                          ingredients, servings, instructions, tags, new ArrayList<>());
    }

    /** Factory: reconstitute from persisted data. */
    public static Recipe of(String id, String name, String description,
                            List<String> ingredients, int servings,
                            List<String> instructions, List<String> tags,
                            List<Annotation> annotations) {
        validateBasic(name, servings);
        return new Recipe(id, name.trim(), description, ingredients, servings,
                          instructions, tags, annotations);
    }

    private static void validateBasic(String name, int servings) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name must not be empty");
        }
        if (servings < 1) {
            throw new IllegalArgumentException("Servings must be at least 1");
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description != null ? description : ""; }
    public List<String> getIngredients() { return new ArrayList<>(ingredients); }
    public int getServings() { return servings; }
    public List<String> getInstructions() { return new ArrayList<>(instructions); }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public List<Annotation> getAnnotations() { return new ArrayList<>(annotations); }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name must not be empty");
        }
        this.name = name.trim();
    }
    public void setDescription(String description) { this.description = description; }
    public void setIngredients(List<String> ingredients) { this.ingredients = new ArrayList<>(ingredients); }
    public void setServings(int servings) {
        if (servings < 1) throw new IllegalArgumentException("Servings must be at least 1");
        this.servings = servings;
    }
    public void setInstructions(List<String> instructions) { this.instructions = new ArrayList<>(instructions); }
    public void setTags(List<String> tags) { this.tags = new ArrayList<>(tags); }
    public void setAnnotations(List<Annotation> annotations) { this.annotations = new ArrayList<>(annotations); }

    public void addAnnotation(Annotation annotation) { annotations.add(annotation); }

    public void removeAnnotation(String annotationId) {
        annotations.removeIf(a -> a.getId().equals(annotationId));
    }

    /** Returns the Annotation object stored inside this recipe (not a copy). */
    public Annotation findAnnotation(String annotationId) {
        return annotations.stream().filter(a -> a.getId().equals(annotationId)).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe)) return false;
        return id.equals(((Recipe) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() { return name; }
}
