package com.recipes.ui;

import com.recipes.model.Recipe;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class RecipeListPanel extends JPanel {

    private final DefaultListModel<Recipe> listModel = new DefaultListModel<>();
    private final JList<Recipe> recipeList = new JList<>(listModel);
    private final JTextField searchField = new JTextField();
    private final JLabel emptyLabel = new JLabel("No recipes found", SwingConstants.CENTER);
    private final JPanel listArea = new JPanel(new CardLayout());

    private Consumer<Recipe> selectionListener;
    private Runnable searchChangeListener;

    private int totalCount = 0;

    public RecipeListPanel() {
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(4, 0));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // List + empty placeholder
        recipeList.setCellRenderer(new RecipeCellRenderer());
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionListener != null) {
                Recipe r = recipeList.getSelectedValue();
                if (r != null) selectionListener.accept(r);
            }
        });

        emptyLabel.setForeground(Color.GRAY);
        listArea.add(new JScrollPane(recipeList), "list");
        listArea.add(emptyLabel, "empty");
        add(listArea, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { fire(); }
            @Override public void removeUpdate(DocumentEvent e) { fire(); }
            @Override public void changedUpdate(DocumentEvent e) { fire(); }
            private void fire() { if (searchChangeListener != null) searchChangeListener.run(); }
        });
    }

    public void setSelectionListener(Consumer<Recipe> listener) {
        this.selectionListener = listener;
    }

    public void setSearchChangeListener(Runnable listener) {
        this.searchChangeListener = listener;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void refresh(List<Recipe> filtered) {
        Recipe previously = recipeList.getSelectedValue();
        listModel.clear();
        for (Recipe r : filtered) listModel.addElement(r);

        CardLayout cl = (CardLayout) listArea.getLayout();
        if (filtered.isEmpty()) {
            cl.show(listArea, "empty");
        } else {
            cl.show(listArea, "list");
            // Restore selection if the same recipe is still in the list
            if (previously != null) {
                for (int i = 0; i < listModel.size(); i++) {
                    if (listModel.get(i).getId().equals(previously.getId())) {
                        recipeList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    public void selectRecipe(Recipe recipe) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getId().equals(recipe.getId())) {
                recipeList.setSelectedIndex(i);
                recipeList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    public void clearSelection() {
        recipeList.clearSelection();
    }

    // ── Cell renderer ──────────────────────────────────────────────────────────

    private static class RecipeCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                       int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Recipe) {
                Recipe r = (Recipe) value;
                String tags = r.getTags().isEmpty() ? "" : "  [" + String.join(", ", r.getTags()) + "]";
                setText("<html><b>" + escapeHtml(r.getName()) + "</b>"
                        + "<font color='#888888'>" + escapeHtml(tags) + "</font></html>");
            }
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            return this;
        }

        private String escapeHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
