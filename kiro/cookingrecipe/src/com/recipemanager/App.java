package com.recipemanager;

import com.recipemanager.controller.RecipeController;
import com.recipemanager.controller.SearchController;
import com.recipemanager.controller.TagController;
import com.recipemanager.service.DefaultValidationService;
import com.recipemanager.service.FileSystemRecipeStore;
import com.recipemanager.service.FileSystemTagService;
import com.recipemanager.service.RecipeStore;
import com.recipemanager.service.SearchEngine;
import com.recipemanager.service.SimpleSearchEngine;
import com.recipemanager.service.TagService;
import com.recipemanager.service.ValidationService;
import com.recipemanager.ui.MainFrame;

import javax.swing.*;

/**
 * Application entry point. Initializes all services, controllers,
 * and launches the main UI on the Swing Event Dispatch Thread.
 */
public class App {

    public static void main(String[] args) {
        // Initialize persistence and services
        RecipeStore recipeStore = new FileSystemRecipeStore();
        TagService tagService = new FileSystemTagService(recipeStore);
        SearchEngine searchEngine = new SimpleSearchEngine();
        ValidationService validationService = new DefaultValidationService();

        // Initialize controllers
        RecipeController recipeController = new RecipeController(recipeStore, validationService);
        TagController tagController = new TagController(tagService);
        SearchController searchController = new SearchController(searchEngine, recipeController);

        // Launch UI on the Swing EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(recipeController, tagController, searchController);
            mainFrame.setVisible(true);
        });
    }
}
