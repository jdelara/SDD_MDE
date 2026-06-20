package com.recipes.model;

import java.time.Instant;
import java.util.UUID;

public class Annotation {

    private final String id;
    private String text;
    private final String createdAt;

    private Annotation(String id, String text, String createdAt) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
    }

    /** Factory: create a brand-new annotation (generates id + timestamp). */
    public static Annotation create(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Annotation text must not be empty");
        }
        return new Annotation(UUID.randomUUID().toString(), text.trim(), Instant.now().toString());
    }

    /** Factory: reconstitute from persisted data. */
    public static Annotation of(String id, String text, String createdAt) {
        return new Annotation(id, text, createdAt);
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getCreatedAt() { return createdAt; }

    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Annotation text must not be empty");
        }
        this.text = text.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        return id.equals(((Annotation) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
