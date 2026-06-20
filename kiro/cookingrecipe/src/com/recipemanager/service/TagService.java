package com.recipemanager.service;

import java.util.List;
import java.util.UUID;

/**
 * Interface for managing recipe tags.
 * Tags are user-defined labels used to categorize and organize recipes.
 */
public interface TagService {

    /**
     * Returns all available tags.
     *
     * @return list of all tag names
     */
    List<String> getAllTags();

    /**
     * Creates a new tag with the given name.
     * Tag names are compared case-insensitively for duplicate detection.
     *
     * @param tagName the name of the tag to create (1-30 characters)
     * @throws DuplicateTagException if a tag with the same name already exists (case-insensitive)
     */
    void createTag(String tagName) throws DuplicateTagException;

    /**
     * Deletes a tag from the available tags list and removes it from all recipes
     * that have it assigned.
     *
     * @param tagName the name of the tag to delete
     */
    void deleteTag(String tagName);

    /**
     * Assigns a tag to a recipe.
     *
     * @param recipeId the UUID of the recipe to tag
     * @param tagName the tag name to assign
     */
    void assignTag(UUID recipeId, String tagName);

    /**
     * Removes a tag from a recipe.
     *
     * @param recipeId the UUID of the recipe to untag
     * @param tagName the tag name to remove
     */
    void removeTag(UUID recipeId, String tagName);
}
