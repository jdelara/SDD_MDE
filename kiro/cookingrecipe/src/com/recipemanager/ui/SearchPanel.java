package com.recipemanager.ui;

import com.recipemanager.controller.SearchController;
import com.recipemanager.model.Recipe;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Panel providing a search text field that filters the recipe list
 * on each keystroke via the SearchController.
 * Clearing the field restores the full sorted recipe list.
 */
public class SearchPanel extends JPanel {

    private final SearchController searchController;
    private final RecipeListPanel recipeListPanel;

    private JTextField searchField;

    public SearchPanel(SearchController searchController, RecipeListPanel recipeListPanel) {
        this.searchController = searchController;
        this.recipeListPanel = recipeListPanel;

        setLayout(new BorderLayout(5, 0));
        initComponents();
    }

    private void initComponents() {
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        searchField.setToolTipText("Type to search recipes by name, description, ingredients, or instructions");

        // Listen for every text change (keystroke, paste, delete)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }
        });

        add(searchLabel, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);
    }

    /**
     * Performs a search using the current text field value.
     * Updates the RecipeListPanel with results.
     * If the query is empty/whitespace, the SearchController returns
     * all recipes sorted, effectively restoring the full list.
     */
    private void performSearch() {
        String query = searchField.getText();
        List<Recipe> results = searchController.search(query);
        recipeListPanel.updateDisplayedRecipes(results);
    }

    /**
     * Returns the search text field for external access (e.g., testing).
     */
    public JTextField getSearchField() {
        return searchField;
    }

    /**
     * Clears the search field, which triggers the document listener
     * and restores the full recipe list.
     */
    public void clearSearch() {
        searchField.setText("");
    }
}
