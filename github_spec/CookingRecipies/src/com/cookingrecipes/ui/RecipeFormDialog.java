package com.cookingrecipes.ui;

import com.cookingrecipes.model.*;
import com.cookingrecipes.service.RecipeService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class RecipeFormDialog extends JDialog {

    private final RecipeService service;
    private Recipe editingRecipe;

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner servesSpinner;
    private JTextArea instructionsArea;
    private DefaultTableModel ingredientModel;
    private DefaultListModel<String> tagListModel;
    private JTextField newTagField;
    private JLabel nameError;

    private boolean saved = false;

    public RecipeFormDialog(Frame owner, RecipeService service, Recipe recipe) {
        super(owner, recipe == null ? "New Recipe" : "Edit Recipe", true);
        this.service = service;
        this.editingRecipe = recipe;
        buildUI();
        if (recipe != null) populate(recipe);
        pack();
        setMinimumSize(new Dimension(560, 640));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHWEST; lc.insets = new Insets(4,0,4,8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1; fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(4,0,4,0);

        int row = 0;

        // Name
        lc.gridy = row; fc.gridy = row++;
        nameField = new JTextField(30);
        nameError = new JLabel(" ");
        nameError.setForeground(Color.RED);
        nameError.setFont(nameError.getFont().deriveFont(11f));
        JPanel namePanel = new JPanel(new BorderLayout(4,2));
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(nameError, BorderLayout.SOUTH);
        form.add(new JLabel("Name *"), lc); form.add(namePanel, fc);

        // Description
        lc.gridy = row; fc.gridy = row++;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true);
        form.add(new JLabel("Description"), lc);
        form.add(new JScrollPane(descriptionArea), fc);

        // Serves
        lc.gridy = row; fc.gridy = row++;
        servesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        JPanel servesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        servesPanel.add(servesSpinner);
        servesPanel.add(new JLabel("  (0 = not specified)"));
        form.add(new JLabel("Serves"), lc); form.add(servesPanel, fc);

        // Instructions
        lc.gridy = row; fc.gridy = row++;
        instructionsArea = new JTextArea(5, 30);
        instructionsArea.setLineWrap(true); instructionsArea.setWrapStyleWord(true);
        form.add(new JLabel("Instructions"), lc);
        form.add(new JScrollPane(instructionsArea), fc);

        // Ingredients table
        lc.gridy = row; fc.gridy = row++;
        String[] cols = {"Name", "Quantity", "Unit"};
        ingredientModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return true; }
        };
        JTable ingTable = new JTable(ingredientModel);
        ingTable.setFillsViewportHeight(true);
        JButton addRow = new JButton("+ Row");
        JButton removeRow = new JButton("- Row");
        addRow.addActionListener(e -> ingredientModel.addRow(new Object[]{"", "", ""}));
        removeRow.addActionListener(e -> {
            int sel = ingTable.getSelectedRow();
            if (sel >= 0) ingredientModel.removeRow(sel);
        });
        JPanel ingPanel = new JPanel(new BorderLayout(4,4));
        ingPanel.add(new JScrollPane(ingTable), BorderLayout.CENTER);
        JPanel ingBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ingBtns.add(addRow); ingBtns.add(removeRow);
        ingPanel.add(ingBtns, BorderLayout.SOUTH);
        ingPanel.setPreferredSize(new Dimension(400, 140));
        form.add(new JLabel("Ingredients"), lc); form.add(ingPanel, fc);

        // Tags
        lc.gridy = row; fc.gridy = row++;
        tagListModel = new DefaultListModel<>();
        JList<String> tagList = new JList<>(tagListModel);
        tagList.setVisibleRowCount(4);
        newTagField = new JTextField(14);
        JButton addTagBtn = new JButton("Add Tag");
        JButton removeTagBtn = new JButton("Remove");
        addTagBtn.addActionListener(e -> {
            String t = newTagField.getText().trim();
            if (!t.isEmpty() && !containsTag(t)) {
                tagListModel.addElement(t);
                newTagField.setText("");
            }
        });
        removeTagBtn.addActionListener(e -> {
            int sel = tagList.getSelectedIndex();
            if (sel >= 0) tagListModel.remove(sel);
        });
        // Allow adding with Enter
        newTagField.addActionListener(e -> addTagBtn.doClick());

        // Populate existing tags from service
        for (Tag t : service.getAllTags()) {
            if (!containsTag(t.getName())) tagListModel.addElement(t.getName());
        }

        JPanel tagPanel = new JPanel(new BorderLayout(4,4));
        tagPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
        JPanel tagInput = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tagInput.add(new JLabel("New tag:")); tagInput.add(newTagField);
        tagInput.add(addTagBtn); tagInput.add(removeTagBtn);
        tagPanel.add(tagInput, BorderLayout.SOUTH);
        tagPanel.setPreferredSize(new Dimension(400, 130));
        form.add(new JLabel("Tags"), lc); form.add(tagPanel, fc);

        root.add(new JScrollPane(form), BorderLayout.CENTER);

        // Buttons
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancel); btns.add(save);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);

        // Name validation on type
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
        });
    }

    private boolean containsTag(String name) {
        for (int i = 0; i < tagListModel.size(); i++)
            if (tagListModel.get(i).equalsIgnoreCase(name)) return true;
        return false;
    }

    private void validateName() {
        boolean blank = nameField.getText().trim().isEmpty();
        nameField.setBorder(blank
            ? BorderFactory.createLineBorder(Color.RED, 2)
            : UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        nameError.setText(blank ? "Name is required." : " ");
    }

    private void onSave() {
        if (nameField.getText().trim().isEmpty()) {
            validateName();
            nameField.requestFocus();
            return;
        }

        Recipe r = editingRecipe != null ? editingRecipe : new Recipe();
        if (r.getId() == null) r.setId(UUID.randomUUID().toString());
        r.setName(nameField.getText().trim());
        r.setDescription(descriptionArea.getText().trim());
        r.setServes((Integer) servesSpinner.getValue());
        r.setInstructions(instructionsArea.getText().trim());

        // Ingredients
        List<Ingredient> ings = new ArrayList<>();
        for (int row = 0; row < ingredientModel.getRowCount(); row++) {
            String n = str(ingredientModel.getValueAt(row, 0));
            if (!n.isEmpty())
                ings.add(new Ingredient(n,
                    str(ingredientModel.getValueAt(row, 1)),
                    str(ingredientModel.getValueAt(row, 2))));
        }
        r.setIngredients(ings);

        // Tags — only selected ones (checked in list)
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < tagListModel.size(); i++)
            tags.add(new Tag(tagListModel.get(i)));
        r.setTags(tags);

        if (editingRecipe != null) {
            service.updateRecipe(r);
        } else {
            service.createRecipe(r);
        }

        saved = true;
        dispose();
    }

    private void populate(Recipe r) {
        nameField.setText(r.getName());
        descriptionArea.setText(r.getDescription());
        servesSpinner.setValue(r.getServes());
        instructionsArea.setText(r.getInstructions());
        for (Ingredient ing : r.getIngredients())
            ingredientModel.addRow(new Object[]{ing.getName(), ing.getQuantity(), ing.getUnit()});

        // Show only this recipe's tags selected
        tagListModel.clear();
        for (Tag t : r.getTags()) tagListModel.addElement(t.getName());
    }

    private String str(Object o) { return o == null ? "" : o.toString().trim(); }

    public boolean isSaved() { return saved; }
}
