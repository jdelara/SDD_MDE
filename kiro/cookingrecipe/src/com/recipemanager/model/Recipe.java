package com.recipemanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a culinary recipe with all associated data including
 * ingredients, tags, and annotations.
 */
public class Recipe {
    private UUID id;
    private String name;              // 1-100 characters
    private String description;       // optional, may be empty
    private List<Ingredient> ingredients; // 1-50 items
    private int servings;             // 1-100 (number of people)
    private String instructions;      // required
    private List<String> tags;        // 0 or more tag names
    private List<Annotation> annotations; // 0 or more

    public Recipe() {
        this.id = UUID.randomUUID();
        this.ingredients = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    public Recipe(UUID id, String name, String description, List<Ingredient> ingredients,
                  int servings, String instructions, List<String> tags, List<Annotation> annotations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
        this.servings = servings;
        this.instructions = instructions;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.annotations = annotations != null ? new ArrayList<>(annotations) : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations != null ? new ArrayList<>(annotations) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return servings == recipe.servings &&
                Objects.equals(id, recipe.id) &&
                Objects.equals(name, recipe.name) &&
                Objects.equals(description, recipe.description) &&
                Objects.equals(ingredients, recipe.ingredients) &&
                Objects.equals(instructions, recipe.instructions) &&
                Objects.equals(tags, recipe.tags) &&
                Objects.equals(annotations, recipe.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, ingredients, servings, instructions, tags, annotations);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", servings=" + servings +
                ", instructions='" + instructions + '\'' +
                ", tags=" + tags +
                ", annotations=" + annotations +
                '}';
    }
}
