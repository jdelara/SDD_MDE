package com.cookingrecipes.ui;

import com.cookingrecipes.model.*;
import com.cookingrecipes.service.RecipeService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final RecipeService service;

    private final SearchBar searchBar       = new SearchBar();
    private final TagFilterPanel tagPanel   = new TagFilterPanel();
    private final RecipeListPanel listPanel = new RecipeListPanel();
    private final RecipeDetailPanel detailPanel;

    private final JButton editBtn   = new JButton("Edit Recipe");
    private final JButton deleteBtn = new JButton("Delete Recipe");

    private String currentQuery = "";

    public MainWindow(RecipeService service) {
        super("Cooking Recipes");
        this.service = service;
        this.detailPanel = new RecipeDetailPanel(service);
        buildUI();
        wireEvents();
        refresh();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton newBtn = new JButton("New Recipe");
        newBtn.addActionListener(e -> onNew());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        toolbar.add(newBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);

        // Top bar = toolbar + search
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(toolbar, BorderLayout.WEST);
        topBar.add(searchBar, BorderLayout.CENTER);

        // Center: recipe list (top) + detail (bottom)
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, detailPanel);
        centerSplit.setResizeWeight(0.4);
        centerSplit.setDividerLocation(220);

        // Main split: tag filter (left) + center
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tagPanel, centerSplit);
        mainSplit.setDividerLocation(165);

        add(topBar, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void wireEvents() {
        // Tag filter
        tagPanel.addFilterListener(e -> {
            if (!e.getValueIsAdjusting()) applyFilter();
        });

        // Search
        searchBar.addSearchListener(q -> {
            currentQuery = q;
            applyFilter();
        });

        // List selection → detail + button state
        listPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Recipe sel = listPanel.getSelectedRecipe();
                detailPanel.setRecipe(sel);
                editBtn.setEnabled(sel != null);
                deleteBtn.setEnabled(sel != null);
            }
        });
    }

    private void applyFilter() {
        Tag tag = tagPanel.getSelectedTag();
        List<Recipe> filtered = service.filterAndSearch(tag, currentQuery);
        listPanel.setRecipes(filtered);
    }

    private void refresh() {
        tagPanel.setTags(service.getAllTags());
        applyFilter();
    }

    private void onNew() {
        RecipeFormDialog dlg = new RecipeFormDialog(this, service, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void onEdit() {
        Recipe sel = listPanel.getSelectedRecipe();
        if (sel == null) return;
        RecipeFormDialog dlg = new RecipeFormDialog(this, service, sel);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
            // Re-show updated recipe in detail
            Recipe updated = service.getAllRecipes().stream()
                .filter(r -> r.getId().equals(sel.getId())).findFirst().orElse(null);
            detailPanel.setRecipe(updated);
        }
    }

    private void onDelete() {
        Recipe sel = listPanel.getSelectedRecipe();
        if (sel == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete recipe \"" + sel.getName() + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteRecipe(sel.getId());
            detailPanel.setRecipe(null);
            refresh();
        }
    }
}
