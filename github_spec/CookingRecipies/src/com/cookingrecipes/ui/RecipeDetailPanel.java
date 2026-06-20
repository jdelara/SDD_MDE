package com.cookingrecipes.ui;

import com.cookingrecipes.model.*;
import com.cookingrecipes.service.RecipeService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RecipeDetailPanel extends JPanel {

    private final RecipeService service;
    private Recipe current;

    private final JLabel nameLabel    = new JLabel(" ");
    private final JLabel servesLabel  = new JLabel(" ");
    private final JTextArea descArea  = new JTextArea();
    private final JTextArea instrArea = new JTextArea();
    private final JTextArea ingArea   = new JTextArea();
    private final JTextArea tagsLabel = new JTextArea();
    private final JTextArea annArea   = new JTextArea();
    private final JButton addAnnBtn   = new JButton("Add Annotation");

    public RecipeDetailPanel(RecipeService service) {
        this.service = service;
        setLayout(new BorderLayout(6, 6));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        buildUI();
        setRecipe(null);
    }

    private void buildUI() {
        // Header
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        JPanel header = new JPanel(new BorderLayout());
        header.add(nameLabel, BorderLayout.CENTER);
        header.add(servesLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main content in a scrollable panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(section("Tags", tagsLabel));
        content.add(section("Description", descArea));
        content.add(section("Ingredients", ingArea));
        content.add(section("Instructions", instrArea));
        content.add(section("Annotations", annArea));

        JPanel annBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        annBtnRow.add(addAnnBtn);
        content.add(annBtnRow);

        add(new JScrollPane(content), BorderLayout.CENTER);

        addAnnBtn.addActionListener(e -> onAddAnnotation());
    }

    private JPanel section(String title, JTextArea area) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(getBackground());
        area.setFont(UIManager.getFont("Label.font"));
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(area, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 60));
        return p;
    }

    public void setRecipe(Recipe recipe) {
        this.current = recipe;
        if (recipe == null) {
            nameLabel.setText("Select a recipe");
            servesLabel.setText("");
            descArea.setText(""); instrArea.setText("");
            ingArea.setText(""); tagsLabel.setText("");
            annArea.setText("");
            addAnnBtn.setEnabled(false);
            return;
        }
        addAnnBtn.setEnabled(true);
        nameLabel.setText(recipe.getName());
        servesLabel.setText(recipe.getServes() > 0 ? "Serves: " + recipe.getServes() : "");
        tagsLabel.setText(recipe.getTags().isEmpty() ? "(none)" :
            recipe.getTags().stream().map(Tag::getName).reduce((a,b)->a+", "+b).orElse(""));
        descArea.setText(recipe.getDescription().isEmpty() ? "(none)" : recipe.getDescription());
        ingArea.setText(buildIngredientText(recipe));
        instrArea.setText(recipe.getInstructions().isEmpty() ? "(none)" : recipe.getInstructions());
        annArea.setText(buildAnnotationText(recipe));
        revalidate(); repaint();
    }

    private String buildIngredientText(Recipe r) {
        if (r.getIngredients().isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (Ingredient ing : r.getIngredients()) {
            sb.append("• ").append(ing.getName());
            if (!ing.getQuantity().isEmpty()) sb.append("  ").append(ing.getQuantity());
            if (!ing.getUnit().isEmpty()) sb.append(" ").append(ing.getUnit());
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String buildAnnotationText(Recipe r) {
        if (r.getAnnotations().isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (Annotation a : r.getAnnotations()) {
            sb.append("[").append(a.getTimestamp()).append("]\n");
            sb.append(a.getText()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private void onAddAnnotation() {
        if (current == null) return;
        String text = JOptionPane.showInputDialog(this, "Enter annotation:", "Add Annotation",
            JOptionPane.PLAIN_MESSAGE);
        if (text != null && !text.trim().isEmpty()) {
            service.addAnnotation(current.getId(), text.trim());
            // Refresh from store
            Recipe updated = service.getAllRecipes().stream()
                .filter(r -> r.getId().equals(current.getId())).findFirst().orElse(null);
            setRecipe(updated);
        }
    }
}
