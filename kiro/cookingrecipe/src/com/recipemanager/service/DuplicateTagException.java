package com.recipemanager.service;

/**
 * Thrown when attempting to create a tag that already exists (case-insensitive comparison).
 */
public class DuplicateTagException extends Exception {

    public DuplicateTagException(String message) {
        super(message);
    }
}
