package com.recipemanager.ui;

import com.recipemanager.controller.RecipeController;
import com.recipemanager.model.Annotation;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.service.ValidationResult;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel displaying full recipe details in read-only mode.
 * Shows name, description (if non-empty), ingredients, servings, instructions,
 * tags, and annotations (newest-first). Provides annotation input and
 * edit/delete action buttons.
 */
public class RecipeDetailPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ANNOTATION_MAX_LENGTH = 2000;

    private final RecipeController recipeController;

    private Recipe currentRecipe;

    // Detail display components
    private JLabel nameLabel;
    private JLabel descriptionLabel;
    private JPanel ingredientsPanel;
    private JLabel servingsLabel;
    private JTextArea instructionsArea;
    private JLabel tagsLabel;

    // Annotations section
    private JPanel annotationsSection;
    private JLabel annotationsSectionHeader;
    private JPanel annotationsListPanel;

    // Add annotation area
    private JTextArea annotationInput;
    private JButton addAnnotationButton;
    private JLabel annotationErrorLabel;

    // Action buttons
    private JButton editButton;
    private JButton deleteButton;
    private JButton backButton;

    // Callbacks
    private Consumer<Recipe> editListener;
    private Consumer<Recipe> deleteListener;
    private Runnable backListener;

    public RecipeDetailPanel(RecipeController recipeController) {
        this.recipeController = recipeController;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Main content in a scrollable panel
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Name
        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(nameLabel);
        mainContent.add(Box.createVerticalStrut(8));

        // Description (hidden when empty)
        descriptionLabel = new JLabel();
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.ITALIC));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(descriptionLabel);
        mainContent.add(Box.createVerticalStrut(8));

        // Servings
        servingsLabel = new JLabel();
        servingsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(servingsLabel);
        mainContent.add(Box.createVerticalStrut(8));

        // Ingredients section
        JLabel ingredientsHeader = new JLabel("Ingredients:");
        ingredientsHeader.setFont(ingredientsHeader.getFont().deriveFont(Font.BOLD));
        ingredientsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(ingredientsHeader);
        mainContent.add(Box.createVerticalStrut(4));

        ingredientsPanel = new JPanel();
        ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
        ingredientsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(ingredientsPanel);
        mainContent.add(Box.createVerticalStrut(8));

        // Instructions
        JLabel instructionsHeader = new JLabel("Instructions:");
        instructionsHeader.setFont(instructionsHeader.getFont().deriveFont(Font.BOLD));
        instructionsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(instructionsHeader);
        mainContent.add(Box.createVerticalStrut(4));

        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setBackground(getBackground());
        instructionsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionsArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        mainContent.add(instructionsArea);
        mainContent.add(Box.createVerticalStrut(8));

        // Tags
        tagsLabel = new JLabel();
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(tagsLabel);
        mainContent.add(Box.createVerticalStrut(12));

        // Annotations section (hidden when no annotations)
        annotationsSection = new JPanel();
        annotationsSection.setLayout(new BoxLayout(annotationsSection, BoxLayout.Y_AXIS));
        annotationsSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        annotationsSectionHeader = new JLabel("Annotations:");
        annotationsSectionHeader.setFont(annotationsSectionHeader.getFont().deriveFont(Font.BOLD));
        annotationsSectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        annotationsSection.add(annotationsSectionHeader);
        annotationsSection.add(Box.createVerticalStrut(4));

        annotationsListPanel = new JPanel();
        annotationsListPanel.setLayout(new BoxLayout(annotationsListPanel, BoxLayout.Y_AXIS));
        annotationsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        annotationsSection.add(annotationsListPanel);

        mainContent.add(annotationsSection);
        mainContent.add(Box.createVerticalStrut(12));

        // Add annotation area
        JPanel addAnnotationPanel = new JPanel();
        addAnnotationPanel.setLayout(new BoxLayout(addAnnotationPanel, BoxLayout.Y_AXIS));
        addAnnotationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addAnnotationPanel.setBorder(BorderFactory.createTitledBorder("Add Annotation"));

        annotationInput = new JTextArea(3, 40);
        annotationInput.setLineWrap(true);
        annotationInput.setWrapStyleWord(true);
        JScrollPane annotationScroll = new JScrollPane(annotationInput);
        annotationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        annotationScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        addAnnotationPanel.add(annotationScroll);
        addAnnotationPanel.add(Box.createVerticalStrut(4));

        annotationErrorLabel = new JLabel(" ");
        annotationErrorLabel.setForeground(Color.RED);
        annotationErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addAnnotationPanel.add(annotationErrorLabel);
        addAnnotationPanel.add(Box.createVerticalStrut(4));

        addAnnotationButton = new JButton("Add Annotation");
        addAnnotationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addAnnotationButton.addActionListener(e -> onAddAnnotation());
        addAnnotationPanel.add(addAnnotationButton);

        mainContent.add(addAnnotationPanel);

        // Scroll pane for the main content
        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(mainScroll, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            if (backListener != null) {
                backListener.run();
            }
        });
        buttonPanel.add(backButton);

        editButton = new JButton("Edit");
        editButton.addActionListener(e -> {
            if (editListener != null && currentRecipe != null) {
                editListener.accept(currentRecipe);
            }
        });
        buttonPanel.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            if (deleteListener != null && currentRecipe != null) {
                deleteListener.accept(currentRecipe);
            }
        });
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the panel with the given recipe's details.
     *
     * @param recipe the recipe to display
     */
    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        if (recipe == null) {
            return;
        }

        // Name
        nameLabel.setText(recipe.getName());

        // Description (hide if null or empty)
        String description = recipe.getDescription();
        if (description == null || description.trim().isEmpty()) {
            descriptionLabel.setVisible(false);
        } else {
            descriptionLabel.setText(description);
            descriptionLabel.setVisible(true);
        }

        // Servings
        servingsLabel.setText("Servings: " + recipe.getServings());

        // Ingredients in original order
        ingredientsPanel.removeAll();
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients != null) {
            for (Ingredient ing : ingredients) {
                String text = ing.getQuantity() + " " + ing.getUnit() + " " + ing.getName();
                JLabel ingLabel = new JLabel("  • " + text);
                ingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                ingredientsPanel.add(ingLabel);
            }
        }

        // Instructions
        instructionsArea.setText(recipe.getInstructions());

        // Tags
        List<String> tags = recipe.getTags();
        if (tags != null && !tags.isEmpty()) {
            tagsLabel.setText("Tags: " + String.join(", ", tags));
            tagsLabel.setVisible(true);
        } else {
            tagsLabel.setText("Tags: none");
            tagsLabel.setVisible(true);
        }

        // Annotations (newest-first, hidden if none)
        refreshAnnotations();

        // Clear annotation input
        annotationInput.setText("");
        annotationErrorLabel.setText(" ");

        revalidate();
        repaint();
    }

    /**
     * Refreshes the annotations display using the controller's sorted method.
     */
    private void refreshAnnotations() {
        annotationsListPanel.removeAll();

        if (currentRecipe == null) {
            annotationsSection.setVisible(false);
            return;
        }

        List<Annotation> annotations = recipeController.getAnnotationsSorted(currentRecipe.getId());

        if (annotations.isEmpty()) {
            annotationsSection.setVisible(false);
        } else {
            annotationsSection.setVisible(true);
            for (Annotation annotation : annotations) {
                JPanel annotationPanel = new JPanel();
                annotationPanel.setLayout(new BoxLayout(annotationPanel, BoxLayout.Y_AXIS));
                annotationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                annotationPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));

                JLabel dateLabel = new JLabel(annotation.getCreatedAt().format(DATE_FORMATTER));
                dateLabel.setFont(dateLabel.getFont().deriveFont(Font.ITALIC, 11f));
                dateLabel.setForeground(Color.GRAY);
                dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                annotationPanel.add(dateLabel);

                JTextArea textArea = new JTextArea(annotation.getText());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setBackground(getBackground());
                textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                annotationPanel.add(textArea);

                annotationsListPanel.add(annotationPanel);
                annotationsListPanel.add(Box.createVerticalStrut(4));
            }
        }

        annotationsSection.revalidate();
        annotationsSection.repaint();
    }

    /**
     * Handles the "Add Annotation" button click.
     * Validates the input and calls the controller to persist.
     */
    private void onAddAnnotation() {
        if (currentRecipe == null) {
            return;
        }

        String text = annotationInput.getText();

        // Client-side validation: non-empty, non-whitespace, 1-2000 chars
        if (text == null || text.trim().isEmpty()) {
            annotationErrorLabel.setText("Annotation text is required and cannot be only whitespace.");
            return;
        }

        if (text.length() > ANNOTATION_MAX_LENGTH) {
            annotationErrorLabel.setText("Annotation must be between 1 and 2000 characters.");
            return;
        }

        // Call controller to validate and persist
        ValidationResult result = recipeController.addAnnotation(currentRecipe.getId(), text);

        if (result.isValid()) {
            // Success: clear input and refresh annotations
            annotationInput.setText("");
            annotationErrorLabel.setText(" ");
            refreshAnnotations();
        } else {
            // Failure: show error, retain text in input field
            List<String> errors = result.getErrors();
            String errorMessage = errors.isEmpty() ? "Failed to save annotation." : errors.get(0);
            annotationErrorLabel.setText(errorMessage);
            // Text remains in the input field (not cleared)
        }
    }

    /**
     * Sets the listener for the "Edit" button.
     *
     * @param listener consumer that receives the current recipe
     */
    public void setEditListener(Consumer<Recipe> listener) {
        this.editListener = listener;
    }

    /**
     * Sets the listener for the "Delete" button.
     *
     * @param listener consumer that receives the current recipe
     */
    public void setDeleteListener(Consumer<Recipe> listener) {
        this.deleteListener = listener;
    }

    /**
     * Sets the listener for the "Back" button.
     *
     * @param listener runnable to execute on back action
     */
    public void setBackListener(Runnable listener) {
        this.backListener = listener;
    }

    /**
     * Returns the currently displayed recipe.
     */
    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }
}
