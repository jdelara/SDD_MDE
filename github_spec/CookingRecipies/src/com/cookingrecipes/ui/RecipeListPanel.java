package com.cookingrecipes.ui;

import com.cookingrecipes.model.Recipe;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public class RecipeListPanel extends JPanel {

    private final DefaultListModel<Recipe> model = new DefaultListModel<>();
    private final JList<Recipe> list = new JList<>(model);
    private final JLabel emptyLabel = new JLabel("No recipes found.", SwingConstants.CENTER);

    public RecipeListPanel() {
        setLayout(new BorderLayout());
        list.setCellRenderer(new RecipeCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC, 13f));

        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void setRecipes(List<Recipe> recipes) {
        Recipe selected = list.getSelectedValue();
        model.clear();
        if (recipes == null || recipes.isEmpty()) {
            list.setVisible(false);
            if (getComponentCount() == 1) add(emptyLabel, BorderLayout.NORTH);
            emptyLabel.setVisible(true);
        } else {
            emptyLabel.setVisible(false);
            list.setVisible(true);
            for (Recipe r : recipes) model.addElement(r);
            // Restore selection if same recipe still present
            if (selected != null) {
                for (int i = 0; i < model.size(); i++) {
                    if (model.get(i).getId().equals(selected.getId())) {
                        list.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        revalidate(); repaint();
    }

    public void addSelectionListener(ListSelectionListener l) {
        list.addListSelectionListener(l);
    }

    public Recipe getSelectedRecipe() { return list.getSelectedValue(); }

    private static class RecipeCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Recipe) {
                Recipe r = (Recipe) value;
                String tags = r.getTags().isEmpty() ? "" :
                    "  [" + r.getTags().stream()
                        .map(t -> t.getName())
                        .reduce((a,b) -> a + ", " + b).orElse("") + "]";
                setText("<html><b>" + htmlEscape(r.getName()) + "</b>"
                    + "<font color='gray'>" + htmlEscape(tags) + "</font></html>");
            }
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return this;
        }

        private String htmlEscape(String s) {
            return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        }
    }
}
