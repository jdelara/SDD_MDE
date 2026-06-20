package com.cookingrecipes.model;

public class Ingredient {
    private String name;
    private String quantity;
    private String unit;

    public Ingredient() {}

    public Ingredient(String name, String quantity, String unit) {
        this.name = name;
        this.quantity = quantity == null ? "" : quantity;
        this.unit = unit == null ? "" : unit;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity == null ? "" : quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit == null ? "" : unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
