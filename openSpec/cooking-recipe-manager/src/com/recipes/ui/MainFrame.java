package com.recipes.ui;

import com.recipes.model.Recipe;
import com.recipes.service.RecipeService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {

    private static final String CARD_DETAIL = "detail";
    private static final String CARD_EDITOR = "editor";

    private final RecipeService service;
    private final TagFilterPanel tagFilterPanel;
    private final RecipeListPanel recipeListPanel;
    private final RecipeDetailPanel detailPanel;
    private final RecipeEditorPanel editorPanel;
    private final AnnotationsPanel annotationsPanel;
    private final JPanel cardPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JLabel statusLabel = new JLabel("0 recipes");

    private Recipe currentRecipe = null;

    public MainFrame(RecipeService service) {
        super("Recipe Manager");
        this.service = service;

        tagFilterPanel = new TagFilterPanel();
        recipeListPanel = new RecipeListPanel();
        detailPanel = new RecipeDetailPanel();
        editorPanel = new RecipeEditorPanel();
        annotationsPanel = new AnnotationsPanel(service);

        cardPanel = new JPanel(cardLayout);
        cardPanel.add(detailPanel, CARD_DETAIL);
        cardPanel.add(editorPanel, CARD_EDITOR);

        wireCallbacks();
        buildLayout();
        buildMenuBar();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        refreshAll();
        warnIfLoadErrors();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void buildLayout() {
        // Right side: detail/editor on top, annotations on bottom
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cardPanel, annotationsPanel);
        rightSplit.setResizeWeight(0.62);
        rightSplit.setBorder(null);

        // Center: search + list | Right side
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, recipeListPanel, rightSplit);
        centerSplit.setDividerLocation(270);
        centerSplit.setResizeWeight(0.0);
        centerSplit.setBorder(null);

        // Outermost: tag filter | center split
        JSplitPane outerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tagFilterPanel, centerSplit);
        outerSplit.setDividerLocation(185);
        outerSplit.setResizeWeight(0.0);
        outerSplit.setBorder(null);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.add(statusLabel);

        getContentPane().add(outerSplit, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    private void buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu recipeMenu = new JMenu("Recipe");
        JMenuItem newItem = new JMenuItem("New Recipe");
        newItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newItem.addActionListener(e -> openNewEditor());
        recipeMenu.add(newItem);

        mb.add(fileMenu);
        mb.add(recipeMenu);
        setJMenuBar(mb);
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────

    private void wireCallbacks() {
        tagFilterPanel.setFilterListener(this::refreshList);
        recipeListPanel.setSearchChangeListener(this::refreshList);
        recipeListPanel.setSelectionListener(this::selectRecipe);

        detailPanel.setEditListener(this::openEditor);
        detailPanel.setDeleteListener(this::deleteCurrentRecipe);

        editorPanel.setSaveListener(this::saveRecipe);
        editorPanel.setCancelListener(this::cancelEdit);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void selectRecipe(Recipe recipe) {
        currentRecipe = recipe;
        detailPanel.showRecipe(recipe);
        annotationsPanel.setRecipe(recipe);
        cardLayout.show(cardPanel, CARD_DETAIL);
    }

    private void openNewEditor() {
        currentRecipe = null;
        editorPanel.populate(null);
        detailPanel.showPlaceholder();
        annotationsPanel.showEmpty();
        recipeListPanel.clearSelection();
        cardLayout.show(cardPanel, CARD_EDITOR);
    }

    private void openEditor() {
        if (currentRecipe == null) return;
        editorPanel.populate(currentRecipe);
        cardLayout.show(cardPanel, CARD_EDITOR);
    }

    private void cancelEdit() {
        if (currentRecipe != null) {
            detailPanel.showRecipe(currentRecipe);
            cardLayout.show(cardPanel, CARD_DETAIL);
        } else {
            detailPanel.showPlaceholder();
            cardLayout.show(cardPanel, CARD_DETAIL);
        }
    }

    private void saveRecipe(Recipe recipe) {
        try {
            service.save(recipe);
            currentRecipe = recipe;
            refreshAll();
            recipeListPanel.selectRecipe(recipe);
            detailPanel.showRecipe(recipe);
            annotationsPanel.setRecipe(recipe);
            cardLayout.show(cardPanel, CARD_DETAIL);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save recipe: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCurrentRecipe() {
        if (currentRecipe == null) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + currentRecipe.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            service.delete(currentRecipe.getId());
            currentRecipe = null;
            detailPanel.showPlaceholder();
            annotationsPanel.showEmpty();
            cardLayout.show(cardPanel, CARD_DETAIL);
            refreshAll();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete recipe: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Refresh helpers ───────────────────────────────────────────────────────

    private void refreshAll() {
        refreshTagPanel();
        refreshList();
    }

    private void refreshList() {
        String query = recipeListPanel.getSearchText();
        Set<String> selectedTags = tagFilterPanel.getSelectedTags();
        List<Recipe> filtered = service.search(query, selectedTags);
        recipeListPanel.refresh(filtered);
        int total = service.getAll().size();
        statusLabel.setText(filtered.size() + " of " + total + " recipe" + (total == 1 ? "" : "s"));
    }

    private void refreshTagPanel() {
        tagFilterPanel.updateTags(service.getAllTags(), tagFilterPanel.getSelectedTags());
    }

    private void warnIfLoadErrors() {
        List<String> warnings = service.getLoadWarnings();
        if (!warnings.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                String msg = "Some recipe files could not be loaded and were skipped:\n\n"
                        + String.join("\n", warnings);
                JOptionPane.showMessageDialog(this, msg, "Load Warnings", JOptionPane.WARNING_MESSAGE);
            });
        }
    }
}
