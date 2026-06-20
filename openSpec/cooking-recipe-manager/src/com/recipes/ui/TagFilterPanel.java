package com.recipes.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TagFilterPanel extends JPanel {

    private final JPanel checkboxPanel = new JPanel();
    private final Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();
    private Runnable filterListener;

    public TagFilterPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Filter by Tag"));
        setPreferredSize(new Dimension(180, 0));

        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(checkboxPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFocusable(false);
        clearBtn.addActionListener(e -> clearAll());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        south.add(clearBtn);
        add(south, BorderLayout.SOUTH);
    }

    public void setFilterListener(Runnable listener) {
        this.filterListener = listener;
    }

    /** Refresh the checkbox list. Previously-selected tags that still exist are preserved. */
    public void updateTags(List<String> allTags, Set<String> keepSelected) {
        Set<String> selected = keepSelected != null ? keepSelected : Collections.emptySet();
        checkboxPanel.removeAll();
        checkboxes.clear();
        for (String tag : allTags) {
            JCheckBox cb = new JCheckBox(tag);
            cb.setSelected(selected.contains(tag));
            cb.addActionListener(e -> {
                if (filterListener != null) filterListener.run();
            });
            checkboxes.put(tag, cb);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            row.add(cb);
            checkboxPanel.add(row);
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    public Set<String> getSelectedTags() {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, JCheckBox> e : checkboxes.entrySet()) {
            if (e.getValue().isSelected()) result.add(e.getKey());
        }
        return result;
    }

    private void clearAll() {
        checkboxes.values().forEach(cb -> cb.setSelected(false));
        if (filterListener != null) filterListener.run();
    }
}
