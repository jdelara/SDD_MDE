package com.recipemanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user-added note, tip, or modification attached to a recipe.
 */
public class Annotation {
    private String text;          // 1-2000 characters
    private LocalDateTime createdAt;

    public Annotation() {
    }

    public Annotation(String text, LocalDateTime createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotation that = (Annotation) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, createdAt);
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "text='" + text + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
