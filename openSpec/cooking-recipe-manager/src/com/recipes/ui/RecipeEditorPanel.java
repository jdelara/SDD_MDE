package com.recipes.ui;

import com.recipes.model.Recipe;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecipeEditorPanel extends JPanel {

    private final JTextField nameField = new JTextField();
    private final JLabel nameError = errorLabel();
    private final JTextArea descArea = new JTextArea(3, 0);
    private final JSpinner servingsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField tagsField = new JTextField();
    private final JTextArea ingredientsArea = new JTextArea(5, 0);
    private final JLabel ingredientsError = errorLabel();
    private final JTextArea instructionsArea = new JTextArea(7, 0);
    private final JLabel instructionsError = errorLabel();
    private final JButton saveBtn = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    private Recipe originalRecipe; // null when creating new

    private Consumer<Recipe> saveListener;
    private Runnable cancelListener;

    public RecipeEditorPanel() {
        setLayout(new BorderLayout());

        JPanel form = buildForm();
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        add(btnRow, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> attemptSave());
        cancelBtn.addActionListener(e -> { if (cancelListener != null) cancelListener.run(); });
    }

    public void setSaveListener(Consumer<Recipe> l) { this.saveListener = l; }
    public void setCancelListener(Runnable l) { this.cancelListener = l; }

    /** Populate the form for editing an existing recipe, or pass null for a new recipe. */
    public void populate(Recipe recipe) {
        this.originalRecipe = recipe;
        clearErrors();
        if (recipe == null) {
            nameField.setText("");
            descArea.setText("");
            servingsSpinner.setValue(1);
            tagsField.setText("");
            ingredientsArea.setText("");
            instructionsArea.setText("");
        } else {
            nameField.setText(recipe.getName());
            descArea.setText(recipe.getDescription());
            servingsSpinner.setValue(recipe.getServings());
            tagsField.setText(String.join(", ", recipe.getTags()));
            ingredientsArea.setText(String.join("\n", recipe.getIngredients()));
            instructionsArea.setText(String.join("\n", recipe.getInstructions()));
        }
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 0, 0, 0);

        int row = 0;
        row = addField(p, gbc, row, "Name: *", nameField, nameError);
        row = addField(p, gbc, row, "Description:", scrolled(descArea, 60), null);
        row = addSpinnerField(p, gbc, row, "Serves: *", servingsSpinner);
        row = addField(p, gbc, row, "Tags (comma-separated):", tagsField, null);
        row = addField(p, gbc, row, "Ingredients (one per line): *", scrolled(ingredientsArea, 90), ingredientsError);
        row = addField(p, gbc, row, "Instructions (one step per line): *", scrolled(instructionsArea, 130), instructionsError);

        // filler to push form to top
        gbc.gridy = row; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        p.add(new JPanel(), gbc);

        return p;
    }

    private int addField(JPanel p, GridBagConstraints gbc, int row,
                         String label, JComponent field, JLabel error) {
        gbc.gridy = row++; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 0, 0);
        p.add(new JLabel(label), gbc);

        gbc.gridy = row++; gbc.insets = new Insets(2, 0, 0, 0);
        boolean grows = field instanceof JScrollPane;
        gbc.fill = grows ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        gbc.weighty = grows ? 0 : 0;
        p.add(field, gbc);

        if (error != null) {
            gbc.gridy = row++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
            gbc.insets = new Insets(1, 2, 0, 0);
            p.add(error, gbc);
        }
        return row;
    }

    private int addSpinnerField(JPanel p, GridBagConstraints gbc, int row,
                                String label, JSpinner spinner) {
        gbc.gridy = row++; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 0, 0);
        p.add(new JLabel(label), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(2, 0, 0, 0);
        spinner.setMaximumSize(new Dimension(80, spinner.getPreferredSize().height));
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.add(spinner);
        p.add(wrapper, gbc);
        return row;
    }

    private JScrollPane scrolled(JTextArea area, int height) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(0, height));
        return sp;
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(new Color(180, 0, 0));
        l.setFont(l.getFont().deriveFont(11f));
        return l;
    }

    private void clearErrors() {
        nameError.setText(" ");
        ingredientsError.setText(" ");
        instructionsError.setText(" ");
    }

    private void attemptSave() {
        clearErrors();
        boolean valid = true;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameError.setText("Name is required");
            valid = false;
        }

        List<String> ingredients = parseLines(ingredientsArea.getText());
        if (ingredients.isEmpty()) {
            ingredientsError.setText("At least one ingredient is required");
            valid = false;
        }

        List<String> instructions = parseLines(instructionsArea.getText());
        if (instructions.isEmpty()) {
            instructionsError.setText("At least one instruction step is required");
            valid = false;
        }

        if (!valid) return;

        String description = descArea.getText().trim();
        int servings = (Integer) servingsSpinner.getValue();
        List<String> tags = parseTags(tagsField.getText());

        Recipe result;
        if (originalRecipe == null) {
            result = Recipe.create(name, description, ingredients, servings, instructions, tags);
        } else {
            result = Recipe.of(originalRecipe.getId(), name, description, ingredients,
                               servings, instructions, tags, originalRecipe.getAnnotations());
        }

        if (saveListener != null) saveListener.accept(result);
    }

    private List<String> parseLines(String text) {
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> parseTags(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
