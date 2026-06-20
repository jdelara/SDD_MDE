package com.recipemanager.service;

/**
 * Wraps IOException for save/delete failures in the persistence layer.
 */
public class PersistenceException extends Exception {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
