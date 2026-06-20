package com.cookingrecipes.model;

public class Annotation {
    private String text;
    private String timestamp;

    public Annotation() {}

    public Annotation(String text, String timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
