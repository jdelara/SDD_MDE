package com.recipemanager.controller;

import com.recipemanager.service.DuplicateTagException;
import com.recipemanager.service.TagService;
import com.recipemanager.util.ErrorLogger;

import java.util.List;
import java.util.UUID;

/**
 * Bridges the UI layer to the TagService for tag lifecycle management
 * and recipe-tag associations.
 */
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Returns all available tags.
     *
     * @return list of all tag names
     */
    public List<String> getAllTags() {
        return tagService.getAllTags();
    }

    /**
     * Creates a new tag with the given name.
     *
     * @param tagName the name of the tag to create
     * @return true if the tag was created successfully, false if it already exists
     */
    public boolean createTag(String tagName) {
        try {
            tagService.createTag(tagName);
            return true;
        } catch (DuplicateTagException e) {
            return false;
        }
    }

    /**
     * Deletes a tag from the system and removes it from all recipes.
     *
     * @param tagName the name of the tag to delete
     */
    public void deleteTag(String tagName) {
        tagService.deleteTag(tagName);
    }

    /**
     * Assigns a tag to a recipe.
     *
     * @param recipeId the UUID of the recipe
     * @param tagName the tag name to assign
     */
    public void assignTag(UUID recipeId, String tagName) {
        tagService.assignTag(recipeId, tagName);
    }

    /**
     * Removes a tag from a recipe.
     *
     * @param recipeId the UUID of the recipe
     * @param tagName the tag name to remove
     */
    public void removeTag(UUID recipeId, String tagName) {
        tagService.removeTag(recipeId, tagName);
    }
}
