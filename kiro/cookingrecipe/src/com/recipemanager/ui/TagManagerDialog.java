package com.recipemanager.ui;

import com.recipemanager.controller.TagController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for managing tags: creating new tags and deleting existing ones.
 * Validates tag names (1-30 chars, unique case-insensitive) and confirms before deletion.
 */
public class TagManagerDialog extends JDialog {

    private static final int TAG_NAME_MAX_LENGTH = 30;

    private final TagController tagController;

    private JPanel tagListPanel;
    private JTextField createTagField;
    private JLabel errorLabel;

    public TagManagerDialog(Frame owner, TagController tagController) {
        super(owner, "Manage Tags", true);
        this.tagController = tagController;

        initComponents();
        refreshTagList();
        pack();
        setMinimumSize(new Dimension(400, 350));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tag list area (scrollable)
        tagListPanel = new JPanel();
        tagListPanel.setLayout(new BoxLayout(tagListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(tagListPanel);
        scrollPane.setPreferredSize(new Dimension(380, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Existing Tags"));
        add(scrollPane, BorderLayout.CENTER);

        // Create tag section at the bottom
        JPanel createPanel = new JPanel(new BorderLayout(5, 5));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New Tag"));

        JPanel inputRow = new JPanel(new BorderLayout(5, 0));
        createTagField = new JTextField(20);
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> onCreateTag());
        inputRow.add(createTagField, BorderLayout.CENTER);
        inputRow.add(createButton, BorderLayout.EAST);

        // Allow Enter key to submit
        createTagField.addActionListener(e -> onCreateTag());

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 11f));

        createPanel.add(inputRow, BorderLayout.NORTH);
        createPanel.add(errorLabel, BorderLayout.SOUTH);
        add(createPanel, BorderLayout.SOUTH);

        // Close button at the top right
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        topPanel.add(closeButton);
        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Refreshes the tag list panel with current tags from the TagController.
     */
    private void refreshTagList() {
        tagListPanel.removeAll();
        List<String> tags = tagController.getAllTags();

        if (tags.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tags defined");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tagListPanel.add(emptyLabel);
        } else {
            for (String tag : tags) {
                JPanel tagRow = createTagRow(tag);
                tagRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                tagListPanel.add(tagRow);
            }
        }

        tagListPanel.revalidate();
        tagListPanel.repaint();
    }

    /**
     * Creates a row panel for a single tag with its name and a Delete button.
     */
    private JPanel createTagRow(String tagName) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel nameLabel = new JLabel(tagName);
        JButton deleteButton = new JButton("Delete");
        deleteButton.setMargin(new Insets(2, 8, 2, 8));
        deleteButton.addActionListener(e -> onDeleteTag(tagName));

        row.add(nameLabel, BorderLayout.CENTER);
        row.add(deleteButton, BorderLayout.EAST);
        return row;
    }

    /**
     * Handles tag creation with validation.
     * Validates: 1-30 chars, unique case-insensitive name.
     */
    private void onCreateTag() {
        String name = createTagField.getText().trim();

        // Validate length
        if (name.isEmpty() || name.length() > TAG_NAME_MAX_LENGTH) {
            errorLabel.setText("Tag name must be 1 to " + TAG_NAME_MAX_LENGTH + " characters");
            return;
        }

        // Attempt creation (TagController returns false on duplicate)
        boolean created = tagController.createTag(name);
        if (!created) {
            errorLabel.setText("Tag already exists");
            return;
        }

        // Success - clear input and error, refresh list
        errorLabel.setText(" ");
        createTagField.setText("");
        refreshTagList();
    }

    /**
     * Handles tag deletion with confirmation dialog.
     */
    private void onDeleteTag(String tagName) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the tag \"" + tagName + "\"?\n"
                        + "It will be removed from all recipes.",
                "Confirm Tag Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            tagController.deleteTag(tagName);
            refreshTagList();
        }
    }

    /**
     * Returns the error label for testing purposes.
     */
    public JLabel getErrorLabel() {
        return errorLabel;
    }

    /**
     * Returns the create tag text field for testing purposes.
     */
    public JTextField getCreateTagField() {
        return createTagField;
    }
}
