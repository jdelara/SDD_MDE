package com.cookingrecipes.model;

public class Tag {
    private String name;

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        return name != null && name.equalsIgnoreCase(((Tag) o).name);
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.toLowerCase().hashCode();
    }

    @Override
    public String toString() { return name; }
}
