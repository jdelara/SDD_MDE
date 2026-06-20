package com.cookingrecipes.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class SearchBar extends JPanel {

    private final JTextField field = new JTextField(22);
    private Consumer<String> listener;

    public SearchBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        add(new JLabel("Search:"));
        field.setToolTipText("Search recipes by name, description, ingredients, or instructions");
        add(field);
        JButton btn = new JButton("Search");
        btn.addActionListener(e -> fireSearch());
        field.addActionListener(e -> fireSearch());
        add(btn);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> { field.setText(""); fireSearch(); });
        add(clear);
    }

    public void addSearchListener(Consumer<String> l) { this.listener = l; }

    private void fireSearch() {
        if (listener != null) listener.accept(field.getText().trim());
    }
}
