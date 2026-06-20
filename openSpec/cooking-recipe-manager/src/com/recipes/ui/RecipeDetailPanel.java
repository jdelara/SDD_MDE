package com.recipes.ui;

import com.recipes.model.Recipe;

import javax.swing.*;
import java.awt.*;

public class RecipeDetailPanel extends JPanel {

    private final JEditorPane display = new JEditorPane();
    private final JButton editBtn = new JButton("Edit");
    private final JButton deleteBtn = new JButton("Delete");

    private Runnable editListener;
    private Runnable deleteListener;

    public RecipeDetailPanel() {
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Button bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        deleteBtn.setForeground(new Color(180, 0, 0));
        btnBar.add(editBtn);
        btnBar.add(deleteBtn);
        add(btnBar, BorderLayout.NORTH);

        // HTML display
        display.setContentType("text/html");
        display.setEditable(false);
        display.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        showPlaceholder();
        add(new JScrollPane(display), BorderLayout.CENTER);

        editBtn.addActionListener(e -> { if (editListener != null) editListener.run(); });
        deleteBtn.addActionListener(e -> { if (deleteListener != null) deleteListener.run(); });
    }

    public void setEditListener(Runnable l) { this.editListener = l; }
    public void setDeleteListener(Runnable l) { this.deleteListener = l; }

    public void showRecipe(Recipe r) {
        editBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
        display.setText(buildHtml(r));
        display.setCaretPosition(0);
    }

    public void showPlaceholder() {
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        display.setText("<html><body style='font-family:sans-serif;color:#aaa;padding:20px'>"
                + "<p>Select a recipe to view its details.</p></body></html>");
    }

    private String buildHtml(Recipe r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:sans-serif;padding:12px;margin:0'>");
        sb.append("<h2 style='margin-top:4px'>").append(esc(r.getName())).append("</h2>");
        sb.append("<table style='margin-bottom:8px'>");
        sb.append("<tr><td><b>Serves:</b></td><td>&nbsp;").append(r.getServings()).append("</td></tr>");
        if (!r.getTags().isEmpty()) {
            sb.append("<tr><td><b>Tags:</b></td><td>&nbsp;")
              .append(esc(String.join(", ", r.getTags()))).append("</td></tr>");
        }
        sb.append("</table>");
        if (!r.getDescription().isEmpty()) {
            sb.append("<p style='color:#555;font-style:italic'>").append(esc(r.getDescription())).append("</p>");
        }
        sb.append("<h3>Ingredients</h3><ul>");
        for (String ing : r.getIngredients()) {
            sb.append("<li>").append(esc(ing)).append("</li>");
        }
        sb.append("</ul>");
        sb.append("<h3>Instructions</h3><ol>");
        for (String step : r.getInstructions()) {
            sb.append("<li style='margin-bottom:4px'>").append(esc(step)).append("</li>");
        }
        sb.append("</ol></body></html>");
        return sb.toString();
    }

    private String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
