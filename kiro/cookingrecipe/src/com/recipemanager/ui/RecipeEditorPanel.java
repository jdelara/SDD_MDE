package com.recipemanager.ui;

import com.recipemanager.controller.RecipeController;
import com.recipemanager.controller.TagController;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.service.ValidationResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Form panel for creating and editing recipes.
 * Supports both create-new and edit-existing modes with inline validation,
 * dynamic ingredient rows, tag assignment, and unsaved changes detection.
 */
public class RecipeEditorPanel extends JPanel {

    private static final int MAX_INGREDIENTS = 50;

    private final RecipeController recipeController;
    private final TagController tagController;

    // Form fields
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField servingsField;
    private JTextArea instructionsArea;

    // Error labels
    private JLabel nameErrorLabel;
    private JLabel servingsErrorLabel;
    private JLabel ingredientsErrorLabel;
    private JLabel instructionsErrorLabel;

    // Ingredient list
    private JPanel ingredientListPanel;
    private List<IngredientRow> ingredientRows;
    private JButton addIngredientButton;

    // Tag checkboxes
    private JPanel tagPanel;
    private List<JCheckBox> tagCheckBoxes;

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    // State
    private Recipe editingRecipe; // non-null in edit mode
    private boolean isEditMode;
    private boolean hasUnsavedChanges;
    private Runnable navigationCallback;

    // Snapshot of initial form state for change detection
    private String initialName;
    private String initialDescription;
    private String initialServings;
    private String initialInstructions;
    private List<String> initialTags;
    private List<IngredientSnapshot> initialIngredients;

    public RecipeEditorPanel(RecipeController recipeController, TagController tagController) {
        this.recipeController = recipeController;
        this.tagController = tagController;
        this.ingredientRows = new ArrayList<>();
        this.tagCheckBoxes = new ArrayList<>();
        this.hasUnsavedChanges = false;

        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Main form wrapped in a scroll pane
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Name field
        formPanel.add(createSectionLabel("Recipe Name *"));
        nameField = new JTextField(40);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(nameField);
        nameErrorLabel = createErrorLabel();
        formPanel.add(nameErrorLabel);

        formPanel.add(Box.createVerticalStrut(10));

        // Description field
        formPanel.add(createSectionLabel("Description"));
        descriptionArea = new JTextArea(3, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        formPanel.add(descScroll);

        formPanel.add(Box.createVerticalStrut(10));

        // Servings field
        formPanel.add(createSectionLabel("Servings (number of people, 1-100) *"));
        servingsField = new JTextField(10);
        servingsField.setMaximumSize(new Dimension(200, 30));
        formPanel.add(servingsField);
        servingsErrorLabel = createErrorLabel();
        formPanel.add(servingsErrorLabel);

        formPanel.add(Box.createVerticalStrut(10));

        // Instructions field
        formPanel.add(createSectionLabel("Instructions *"));
        instructionsArea = new JTextArea(6, 40);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        JScrollPane instrScroll = new JScrollPane(instructionsArea);
        instrScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        formPanel.add(instrScroll);
        instructionsErrorLabel = createErrorLabel();
        formPanel.add(instructionsErrorLabel);

        formPanel.add(Box.createVerticalStrut(10));

        // Ingredients section
        formPanel.add(createSectionLabel("Ingredients (1-50) *"));
        ingredientsErrorLabel = createErrorLabel();
        formPanel.add(ingredientsErrorLabel);

        ingredientListPanel = new JPanel();
        ingredientListPanel.setLayout(new BoxLayout(ingredientListPanel, BoxLayout.Y_AXIS));
        formPanel.add(ingredientListPanel);

        addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addIngredientButton.addActionListener(e -> addIngredientRow("", "", ""));
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(addIngredientButton);

        formPanel.add(Box.createVerticalStrut(10));

        // Tags section
        formPanel.add(createSectionLabel("Tags"));
        tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tagPanel);

        formPanel.add(Box.createVerticalStrut(15));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> onCancel());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(buttonPanel);

        JScrollPane mainScroll = new JScrollPane(formPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        // Add change listeners to detect unsaved modifications
        attachChangeListeners();
    }

    /**
     * Attaches listeners to form fields to track unsaved changes.
     */
    private void attachChangeListeners() {
        nameField.getDocument().addDocumentListener(new SimpleDocumentListener(this::markChanged));
        descriptionArea.getDocument().addDocumentListener(new SimpleDocumentListener(this::markChanged));
        servingsField.getDocument().addDocumentListener(new SimpleDocumentListener(this::markChanged));
        instructionsArea.getDocument().addDocumentListener(new SimpleDocumentListener(this::markChanged));
    }

    private void markChanged() {
        hasUnsavedChanges = true;
    }

    /**
     * Sets the panel to create mode with an empty form.
     */
    public void setCreateMode() {
        this.isEditMode = false;
        this.editingRecipe = null;
        clearForm();
        addIngredientRow("", "", "");
        populateTagCheckboxes(new ArrayList<>());
        captureInitialState();
        hasUnsavedChanges = false;
    }

    /**
     * Sets the panel to edit mode, populating fields from the given recipe.
     *
     * @param recipe the recipe to edit
     */
    public void setRecipe(Recipe recipe) {
        this.isEditMode = true;
        this.editingRecipe = recipe;
        clearForm();

        nameField.setText(recipe.getName() != null ? recipe.getName() : "");
        descriptionArea.setText(recipe.getDescription() != null ? recipe.getDescription() : "");
        servingsField.setText(String.valueOf(recipe.getServings()));
        instructionsArea.setText(recipe.getInstructions() != null ? recipe.getInstructions() : "");

        // Populate ingredients
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            for (Ingredient ing : ingredients) {
                addIngredientRow(
                        ing.getName() != null ? ing.getName() : "",
                        String.valueOf(ing.getQuantity()),
                        ing.getUnit() != null ? ing.getUnit() : ""
                );
            }
        } else {
            addIngredientRow("", "", "");
        }

        // Populate tags
        populateTagCheckboxes(recipe.getTags() != null ? recipe.getTags() : new ArrayList<>());

        captureInitialState();
        hasUnsavedChanges = false;
    }

    /**
     * Sets the callback to invoke after successful save or cancel (navigation back to list).
     *
     * @param callback the navigation callback
     */
    public void setNavigationCallback(Runnable callback) {
        this.navigationCallback = callback;
    }

    /**
     * Checks if the form has unsaved changes. If so, prompts the user.
     *
     * @return true if navigation should proceed, false if user chose to stay
     */
    public boolean confirmNavigationAway() {
        if (!hasUnsavedChanges) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Do you want to discard them?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return choice == JOptionPane.YES_OPTION;
    }

    /**
     * Returns whether the form currently has unsaved changes.
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    // ---- Private Methods ----

    /**
     * Clears all form fields and resets state.
     */
    private void clearForm() {
        nameField.setText("");
        descriptionArea.setText("");
        servingsField.setText("");
        instructionsArea.setText("");

        // Clear ingredient rows
        ingredientRows.clear();
        ingredientListPanel.removeAll();
        ingredientListPanel.revalidate();
        ingredientListPanel.repaint();

        // Clear errors
        clearErrors();
    }

    /**
     * Populates tag checkboxes from the available tags, checking those in selectedTags.
     */
    private void populateTagCheckboxes(List<String> selectedTags) {
        tagPanel.removeAll();
        tagCheckBoxes.clear();

        List<String> allTags = tagController.getAllTags();
        for (String tag : allTags) {
            JCheckBox cb = new JCheckBox(tag);
            cb.setSelected(selectedTags.contains(tag));
            cb.addActionListener(e -> markChanged());
            tagCheckBoxes.add(cb);
            tagPanel.add(cb);
        }
        tagPanel.revalidate();
        tagPanel.repaint();
    }

    /**
     * Adds a new ingredient row to the dynamic list.
     */
    private void addIngredientRow(String name, String quantity, String unit) {
        if (ingredientRows.size() >= MAX_INGREDIENTS) {
            JOptionPane.showMessageDialog(this,
                    "Maximum of " + MAX_INGREDIENTS + " ingredients allowed.",
                    "Limit Reached", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IngredientRow row = new IngredientRow(name, quantity, unit);
        ingredientRows.add(row);
        ingredientListPanel.add(row.getPanel());
        ingredientListPanel.revalidate();
        ingredientListPanel.repaint();
        markChanged();
    }

    /**
     * Removes an ingredient row from the dynamic list.
     */
    private void removeIngredientRow(IngredientRow row) {
        ingredientRows.remove(row);
        ingredientListPanel.remove(row.getPanel());
        ingredientListPanel.revalidate();
        ingredientListPanel.repaint();
        markChanged();
    }

    /**
     * Handles the Save button click.
     */
    private void onSave() {
        clearErrors();

        // Build recipe from form
        Recipe recipe = buildRecipeFromForm();
        if (recipe == null) {
            return; // local validation errors already displayed
        }

        // Call controller
        ValidationResult result;
        if (isEditMode) {
            result = recipeController.updateRecipe(recipe);
        } else {
            result = recipeController.createRecipe(recipe);
        }

        if (result.isValid()) {
            // Success
            JOptionPane.showMessageDialog(this,
                    "Recipe saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            hasUnsavedChanges = false;
            if (navigationCallback != null) {
                navigationCallback.run();
            }
        } else {
            // Show validation errors from controller
            displayValidationErrors(result.getErrors());
        }
    }

    /**
     * Builds a Recipe object from the current form field values.
     * Returns null if local validation fails (and displays errors inline).
     */
    private Recipe buildRecipeFromForm() {
        boolean valid = true;

        // Validate name
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameErrorLabel.setText("Recipe name is required");
            nameErrorLabel.setVisible(true);
            valid = false;
        } else if (name.length() > 100) {
            nameErrorLabel.setText("Recipe name must be 100 characters or fewer");
            nameErrorLabel.setVisible(true);
            valid = false;
        }

        // Validate servings
        String servingsText = servingsField.getText().trim();
        int servings = 0;
        try {
            servings = Integer.parseInt(servingsText);
            if (servings < 1 || servings > 100) {
                servingsErrorLabel.setText("Servings must be a whole number between 1 and 100");
                servingsErrorLabel.setVisible(true);
                valid = false;
            }
        } catch (NumberFormatException e) {
            servingsErrorLabel.setText("Servings must be a whole number between 1 and 100");
            servingsErrorLabel.setVisible(true);
            valid = false;
        }

        // Validate instructions
        String instructions = instructionsArea.getText().trim();
        if (instructions.isEmpty()) {
            instructionsErrorLabel.setText("Instructions are required");
            instructionsErrorLabel.setVisible(true);
            valid = false;
        }

        // Validate ingredients
        if (ingredientRows.isEmpty()) {
            ingredientsErrorLabel.setText("At least 1 ingredient is required");
            ingredientsErrorLabel.setVisible(true);
            valid = false;
        } else {
            // Validate each ingredient row
            boolean ingredientError = false;
            for (IngredientRow row : ingredientRows) {
                String ingName = row.getIngredientName().trim();
                String qtyStr = row.getQuantity().trim();
                if (ingName.isEmpty() || ingName.length() > 50) {
                    row.showError("Name must be 1-50 characters");
                    ingredientError = true;
                }
                try {
                    double qty = Double.parseDouble(qtyStr);
                    if (qty < 0.01 || qty > 99999) {
                        row.showError("Quantity must be 0.01-99999");
                        ingredientError = true;
                    }
                } catch (NumberFormatException e) {
                    row.showError("Quantity must be a valid number (0.01-99999)");
                    ingredientError = true;
                }
            }
            if (ingredientError) {
                ingredientsErrorLabel.setText("Fix ingredient errors below");
                ingredientsErrorLabel.setVisible(true);
                valid = false;
            }
        }

        if (!valid) {
            return null;
        }

        // Build recipe
        Recipe recipe = new Recipe();
        if (isEditMode && editingRecipe != null) {
            recipe.setId(editingRecipe.getId());
            // Annotations are preserved by RecipeController.updateRecipe
        } else {
            recipe.setId(UUID.randomUUID());
        }

        recipe.setName(name);
        recipe.setDescription(descriptionArea.getText().trim());
        recipe.setServings(servings);
        recipe.setInstructions(instructions);

        // Build ingredient list
        List<Ingredient> ingredients = new ArrayList<>();
        for (IngredientRow row : ingredientRows) {
            Ingredient ing = new Ingredient(
                    row.getIngredientName().trim(),
                    Double.parseDouble(row.getQuantity().trim()),
                    row.getUnit().trim()
            );
            ingredients.add(ing);
        }
        recipe.setIngredients(ingredients);

        // Build tag list from checked checkboxes
        List<String> selectedTags = new ArrayList<>();
        for (JCheckBox cb : tagCheckBoxes) {
            if (cb.isSelected()) {
                selectedTags.add(cb.getText());
            }
        }
        recipe.setTags(selectedTags);

        return recipe;
    }

    /**
     * Displays validation errors returned by the controller inline.
     */
    private void displayValidationErrors(List<String> errors) {
        for (String error : errors) {
            String lowerError = error.toLowerCase();
            if (lowerError.contains("name")) {
                nameErrorLabel.setText(error);
                nameErrorLabel.setVisible(true);
            } else if (lowerError.contains("serving")) {
                servingsErrorLabel.setText(error);
                servingsErrorLabel.setVisible(true);
            } else if (lowerError.contains("ingredient")) {
                ingredientsErrorLabel.setText(error);
                ingredientsErrorLabel.setVisible(true);
            } else if (lowerError.contains("instruction")) {
                instructionsErrorLabel.setText(error);
                instructionsErrorLabel.setVisible(true);
            } else {
                // Generic error - show at the top name error area
                nameErrorLabel.setText(error);
                nameErrorLabel.setVisible(true);
            }
        }
    }

    /**
     * Handles the Cancel button click.
     */
    private void onCancel() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Are you sure you want to discard them?",
                    "Discard Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return; // User chose not to discard
            }
        }
        hasUnsavedChanges = false;
        if (navigationCallback != null) {
            navigationCallback.run();
        }
    }

    /**
     * Clears all inline error labels.
     */
    private void clearErrors() {
        nameErrorLabel.setVisible(false);
        servingsErrorLabel.setVisible(false);
        ingredientsErrorLabel.setVisible(false);
        instructionsErrorLabel.setVisible(false);
        for (IngredientRow row : ingredientRows) {
            row.clearError();
        }
    }

    /**
     * Captures the initial state of the form for unsaved change detection.
     */
    private void captureInitialState() {
        initialName = nameField.getText();
        initialDescription = descriptionArea.getText();
        initialServings = servingsField.getText();
        initialInstructions = instructionsArea.getText();
        initialTags = getSelectedTags();
        initialIngredients = new ArrayList<>();
        for (IngredientRow row : ingredientRows) {
            initialIngredients.add(new IngredientSnapshot(
                    row.getIngredientName(), row.getQuantity(), row.getUnit()));
        }
    }

    /**
     * Returns a list of currently selected tag names.
     */
    private List<String> getSelectedTags() {
        List<String> tags = new ArrayList<>();
        for (JCheckBox cb : tagCheckBoxes) {
            if (cb.isSelected()) {
                tags.add(cb.getText());
            }
        }
        return tags;
    }

    // ---- Helper UI Methods ----

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel();
        label.setForeground(Color.RED);
        label.setFont(label.getFont().deriveFont(Font.ITALIC, 11f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setVisible(false);
        return label;
    }

    // ---- Inner Classes ----

    /**
     * Represents a single ingredient row in the dynamic ingredient list.
     */
    private class IngredientRow {
        private final JPanel panel;
        private final JTextField nameField;
        private final JTextField quantityField;
        private final JTextField unitField;
        private final JLabel errorLabel;
        private final JButton removeButton;

        public IngredientRow(String name, String quantity, String unit) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            nameField = new JTextField(name, 15);
            nameField.setToolTipText("Ingredient name (1-50 chars)");
            quantityField = new JTextField(quantity, 8);
            quantityField.setToolTipText("Quantity (0.01-99999)");
            unitField = new JTextField(unit, 8);
            unitField.setToolTipText("Unit (e.g., g, ml, cups)");

            removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeIngredientRow(this));

            errorLabel = new JLabel();
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(errorLabel.getFont().deriveFont(Font.ITALIC, 10f));
            errorLabel.setVisible(false);

            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Qty:"));
            panel.add(quantityField);
            panel.add(new JLabel("Unit:"));
            panel.add(unitField);
            panel.add(removeButton);
            panel.add(errorLabel);

            // Attach change listeners
            nameField.getDocument().addDocumentListener(
                    new SimpleDocumentListener(RecipeEditorPanel.this::markChanged));
            quantityField.getDocument().addDocumentListener(
                    new SimpleDocumentListener(RecipeEditorPanel.this::markChanged));
            unitField.getDocument().addDocumentListener(
                    new SimpleDocumentListener(RecipeEditorPanel.this::markChanged));
        }

        public JPanel getPanel() {
            return panel;
        }

        public String getIngredientName() {
            return nameField.getText();
        }

        public String getQuantity() {
            return quantityField.getText();
        }

        public String getUnit() {
            return unitField.getText();
        }

        public void showError(String message) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }

        public void clearError() {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Snapshot of ingredient row state for change detection.
     */
    private static class IngredientSnapshot {
        final String name;
        final String quantity;
        final String unit;

        IngredientSnapshot(String name, String quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    /**
     * Simple DocumentListener that calls a Runnable on any document change.
     */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable onChange;

        SimpleDocumentListener(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }
    }
}
