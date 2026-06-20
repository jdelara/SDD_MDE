package com.recipemanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the result of a validation operation, collecting all error messages.
 */
public class ValidationResult {
    private final List<String> errors;

    public ValidationResult() {
        this.errors = new ArrayList<>();
    }

    public ValidationResult(List<String> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
