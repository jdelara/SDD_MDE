package com.recipemanager.ui;

import com.recipemanager.controller.RecipeController;
import com.recipemanager.controller.TagController;
import com.recipemanager.model.Recipe;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel displaying a list of recipes with their names and tags.
 * Provides tag filtering via a JComboBox and notifies listeners on recipe selection.
 */
public class RecipeListPanel extends JPanel {

    private static final String ALL_TAGS_OPTION = "All (no filter)";
    private static final String EMPTY_FILTER_MESSAGE = "No recipes match the selected tag";
    private static final String EMPTY_SEARCH_MESSAGE = "No results found";

    private final RecipeController recipeController;
    private final TagController tagController;

    private JList<Recipe> recipeList;
    private DefaultListModel<Recipe> listModel;
    private JComboBox<String> tagFilterCombo;
    private JLabel emptyMessageLabel;
    private JScrollPane scrollPane;

    private Consumer<Recipe> recipeSelectionListener;

    public RecipeListPanel(RecipeController recipeController, TagController tagController) {
        this.recipeController = recipeController;
        this.tagController = tagController;

        setLayout(new BorderLayout());
        initComponents();
        loadRecipes();
    }

    private void initComponents() {
        // Top panel with tag filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by tag:"));
        tagFilterCombo = new JComboBox<>();
        populateTagFilter();
        tagFilterCombo.addActionListener(e -> onTagFilterChanged());
        filterPanel.add(tagFilterCombo);
        add(filterPanel, BorderLayout.NORTH);

        // Recipe list
        listModel = new DefaultListModel<>();
        recipeList = new JList<>(listModel);
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.setCellRenderer(new RecipeListCellRenderer());
        recipeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Recipe selected = recipeList.getSelectedValue();
                if (selected != null && recipeSelectionListener != null) {
                    recipeSelectionListener.accept(selected);
                }
            }
        });

        scrollPane = new JScrollPane(recipeList);
        add(scrollPane, BorderLayout.CENTER);

        // Empty message label (hidden initially)
        emptyMessageLabel = new JLabel(EMPTY_FILTER_MESSAGE);
        emptyMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyMessageLabel.setFont(emptyMessageLabel.getFont().deriveFont(Font.ITALIC));
        emptyMessageLabel.setVisible(false);
        add(emptyMessageLabel, BorderLayout.SOUTH);
    }

    /**
     * Populates the tag filter combo box with available tags.
     */
    private void populateTagFilter() {
        tagFilterCombo.removeAllItems();
        tagFilterCombo.addItem(ALL_TAGS_OPTION);
        List<String> tags = tagController.getAllTags();
        for (String tag : tags) {
            tagFilterCombo.addItem(tag);
        }
    }

    /**
     * Handles tag filter selection changes.
     */
    private void onTagFilterChanged() {
        String selected = (String) tagFilterCombo.getSelectedItem();
        if (selected == null || ALL_TAGS_OPTION.equals(selected)) {
            // Clear filter - show all recipes sorted
            displayRecipes(recipeController.getRecipesSorted());
        } else {
            // Filter by selected tag
            List<Recipe> filtered = recipeController.filterByTag(selected);
            displayRecipes(filtered);
        }
    }

    /**
     * Displays the given list of recipes in the JList.
     * Shows empty message if the list is empty due to filtering.
     */
    private void displayRecipes(List<Recipe> recipes) {
        displayRecipesWithMessage(recipes, EMPTY_FILTER_MESSAGE);
    }

    /**
     * Loads all recipes sorted alphabetically and displays them.
     * Called on startup and when refreshing.
     */
    public void loadRecipes() {
        populateTagFilter();
        tagFilterCombo.setSelectedIndex(0); // Reset to "All (no filter)"
        displayRecipes(recipeController.getRecipesSorted());
    }

    /**
     * Refreshes the recipe list, maintaining the current filter if applicable.
     */
    public void refreshList() {
        populateTagFilter();
        onTagFilterChanged();
    }

    /**
     * Updates the displayed recipe list (used by search functionality).
     * Shows "No results found" message when search yields empty results.
     *
     * @param recipes the recipes to display
     */
    public void updateDisplayedRecipes(List<Recipe> recipes) {
        displayRecipesWithMessage(recipes, EMPTY_SEARCH_MESSAGE);
    }

    /**
     * Displays recipes with a custom empty message.
     */
    private void displayRecipesWithMessage(List<Recipe> recipes, String emptyMessage) {
        listModel.clear();
        if (recipes.isEmpty()) {
            emptyMessageLabel.setText(emptyMessage);
            emptyMessageLabel.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            emptyMessageLabel.setVisible(false);
            scrollPane.setVisible(true);
            for (Recipe recipe : recipes) {
                listModel.addElement(recipe);
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Sets the listener to be notified when a recipe is selected.
     *
     * @param listener the selection listener
     */
    public void setRecipeSelectionListener(Consumer<Recipe> listener) {
        this.recipeSelectionListener = listener;
    }

    /**
     * Returns the currently selected recipe, or null if nothing is selected.
     */
    public Recipe getSelectedRecipe() {
        return recipeList.getSelectedValue();
    }

    /**
     * Custom cell renderer that displays recipe name and tags.
     */
    private static class RecipeListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Recipe) {
                Recipe recipe = (Recipe) value;
                StringBuilder display = new StringBuilder(recipe.getName());
                List<String> tags = recipe.getTags();
                if (tags != null && !tags.isEmpty()) {
                    display.append("  [");
                    display.append(String.join(", ", tags));
                    display.append("]");
                }
                setText(display.toString());
            }
            return this;
        }
    }
}
