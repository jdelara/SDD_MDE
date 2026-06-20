package com.recipemanager.service;

import java.util.Collections;
import java.util.List;

/**
 * Contains all validation errors for a single operation.
 * Thrown as a RuntimeException when validation fails.
 */
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Validation failed: " + String.join("; ", errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
