package com.cookingrecipes.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private int serves;
    private String instructions;
    private List<Tag> tags;
    private List<Ingredient> ingredients;
    private List<Annotation> annotations;

    public Recipe() {
        this.tags = new ArrayList<>();
        this.ingredients = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description == null ? "" : description; }
    public void setDescription(String description) { this.description = description; }

    public int getServes() { return serves; }
    public void setServes(int serves) { this.serves = serves; }

    public String getInstructions() { return instructions == null ? "" : instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags == null ? new ArrayList<>() : tags; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients == null ? new ArrayList<>() : ingredients; }

    public List<Annotation> getAnnotations() { return annotations; }
    public void setAnnotations(List<Annotation> annotations) { this.annotations = annotations == null ? new ArrayList<>() : annotations; }

    @Override
    public String toString() { return name; }
}
