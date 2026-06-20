package com.cookingrecipes.ui;

import com.cookingrecipes.model.Tag;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public class TagFilterPanel extends JPanel {

    private static final String ALL = "All";
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    public TagFilterPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Filter by Tag"));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model.addElement(ALL);
        list.setSelectedIndex(0);
        add(new JScrollPane(list), BorderLayout.CENTER);
        setPreferredSize(new Dimension(160, 0));
    }

    public void setTags(List<Tag> tags) {
        String selected = list.getSelectedValue();
        model.clear();
        model.addElement(ALL);
        tags.stream()
            .map(Tag::getName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .forEach(model::addElement);
        // Restore selection
        if (selected != null) {
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).equalsIgnoreCase(selected)) {
                    list.setSelectedIndex(i);
                    return;
                }
            }
        }
        list.setSelectedIndex(0);
    }

    public void addFilterListener(ListSelectionListener l) {
        list.addListSelectionListener(l);
    }

    /** Returns null when "All" is selected. */
    public Tag getSelectedTag() {
        String sel = list.getSelectedValue();
        if (sel == null || sel.equals(ALL)) return null;
        return new Tag(sel);
    }
}
