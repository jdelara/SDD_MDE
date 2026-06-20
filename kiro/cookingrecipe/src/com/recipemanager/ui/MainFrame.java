package com.recipemanager.ui;

import com.recipemanager.controller.RecipeController;
import com.recipemanager.controller.SearchController;
import com.recipemanager.controller.TagController;
import com.recipemanager.model.Recipe;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level JFrame that hosts a CardLayout for switching between
 * RecipeListPanel, RecipeDetailPanel, and RecipeEditorPanel.
 */
public class MainFrame extends JFrame {

    private static final String LIST_PANEL = "LIST";
    private static final String DETAIL_PANEL = "DETAIL";
    private static final String EDITOR_PANEL = "EDITOR";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private final RecipeController recipeController;
    private final TagController tagController;
    private final SearchController searchController;

    private RecipeListPanel recipeListPanel;
    private SearchPanel searchPanel;
    private RecipeDetailPanel recipeDetailPanel;
    private RecipeEditorPanel recipeEditorPanel;

    public MainFrame(RecipeController recipeController, TagController tagController,
                     SearchController searchController) {
        super("Recipe Manager");
        this.recipeController = recipeController;
        this.tagController = tagController;
        this.searchController = searchController;

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);

        initComponents();
        setupFrame();
    }

    private void initComponents() {
        recipeListPanel = new RecipeListPanel(recipeController, tagController);
        recipeListPanel.setRecipeSelectionListener(this::showDetail);

        // Search panel wired to SearchController and RecipeListPanel
        searchPanel = new SearchPanel(searchController, recipeListPanel);

        recipeDetailPanel = new RecipeDetailPanel(recipeController);
        recipeDetailPanel.setEditListener(this::showEditor);
        recipeDetailPanel.setDeleteListener(this::initiateDelete);
        recipeDetailPanel.setBackListener(this::showList);

        recipeEditorPanel = new RecipeEditorPanel(recipeController, tagController);
        recipeEditorPanel.setNavigationCallback(this::showList);

        // List view wraps search panel + recipe list
        JPanel listViewPanel = new JPanel(new BorderLayout());

        // Top bar with search, new recipe button, and manage tags button
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topBar.add(searchPanel, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton newRecipeButton = new JButton("New Recipe");
        newRecipeButton.addActionListener(e -> showEditor());
        buttonBar.add(newRecipeButton);

        JButton manageTagsButton = new JButton("Manage Tags");
        manageTagsButton.addActionListener(e -> showTagManagerDialog());
        buttonBar.add(manageTagsButton);

        topBar.add(buttonBar, BorderLayout.EAST);

        listViewPanel.add(topBar, BorderLayout.NORTH);
        listViewPanel.add(recipeListPanel, BorderLayout.CENTER);

        contentPanel.add(listViewPanel, LIST_PANEL);
        contentPanel.add(recipeDetailPanel, DETAIL_PANEL);
        contentPanel.add(recipeEditorPanel, EDITOR_PANEL);
    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        setSize(900, 650);
        setLocationRelativeTo(null);
    }

    /**
     * Switches to the recipe list panel view and refreshes data.
     */
    public void showList() {
        searchPanel.clearSearch();
        recipeListPanel.refreshList();
        cardLayout.show(contentPanel, LIST_PANEL);
    }

    /**
     * Switches to the recipe detail panel for the given recipe.
     *
     * @param recipe the recipe to display
     */
    public void showDetail(Recipe recipe) {
        recipeDetailPanel.setRecipe(recipe);
        cardLayout.show(contentPanel, DETAIL_PANEL);
    }

    /**
     * Switches to the recipe editor panel in create mode.
     */
    public void showEditor() {
        if (recipeEditorPanel.hasUnsavedChanges()) {
            if (!recipeEditorPanel.confirmNavigationAway()) {
                return;
            }
        }
        recipeEditorPanel.setCreateMode();
        cardLayout.show(contentPanel, EDITOR_PANEL);
    }

    /**
     * Switches to the recipe editor panel in edit mode for the given recipe.
     *
     * @param recipe the recipe to edit
     */
    public void showEditor(Recipe recipe) {
        if (recipeEditorPanel.hasUnsavedChanges()) {
            if (!recipeEditorPanel.confirmNavigationAway()) {
                return;
            }
        }
        recipeEditorPanel.setRecipe(recipe);
        cardLayout.show(contentPanel, EDITOR_PANEL);
    }

    /**
     * Returns the RecipeListPanel for external access (e.g., search updates).
     */
    public RecipeListPanel getRecipeListPanel() {
        return recipeListPanel;
    }

    /**
     * Returns the content panel used for CardLayout switching.
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Returns the CardLayout manager.
     */
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    /**
     * Opens the Tag Manager dialog for creating/deleting tags.
     */
    private void showTagManagerDialog() {
        TagManagerDialog dialog = new TagManagerDialog(this, tagController);
        dialog.setVisible(true);
        // Refresh list after dialog closes to reflect tag changes
        recipeListPanel.refreshList();
    }

    /**
     * Initiates the delete flow for a recipe.
     * Shows a confirmation dialog, and if confirmed, deletes the recipe
     * via the controller and refreshes the list view.
     *
     * @param recipe the recipe to delete
     */
    private void initiateDelete(Recipe recipe) {
        boolean confirmed = ConfirmationDialogs.confirmDeleteRecipe(this, recipe.getName());
        if (!confirmed) {
            return;
        }

        boolean success = recipeController.deleteRecipe(recipe.getId());
        if (success) {
            showList();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete recipe.",
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Returns the RecipeDetailPanel for external access.
     */
    public RecipeDetailPanel getRecipeDetailPanel() {
        return recipeDetailPanel;
    }

    /**
     * Returns the RecipeEditorPanel for external access.
     */
    public RecipeEditorPanel getRecipeEditorPanel() {
        return recipeEditorPanel;
    }

    /**
     * Returns the SearchPanel for external access.
     */
    public SearchPanel getSearchPanel() {
        return searchPanel;
    }
}
