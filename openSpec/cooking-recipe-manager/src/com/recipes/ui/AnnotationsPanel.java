package com.recipes.ui;

import com.recipes.model.Annotation;
import com.recipes.model.Recipe;
import com.recipes.service.RecipeService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AnnotationsPanel extends JPanel {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final RecipeService service;
    private final JPanel entriesPanel = new JPanel();
    private final JButton addBtn = new JButton("+ Add Annotation");

    private Recipe currentRecipe;
    private String editingAnnotationId = null;
    private boolean showingAddForm = false;

    public AnnotationsPanel(RecipeService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder("Annotations"));

        entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(entriesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        south.add(addBtn);
        add(south, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            showingAddForm = true;
            editingAnnotationId = null;
            rebuild();
        });

        showEmpty();
    }

    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        this.editingAnnotationId = null;
        this.showingAddForm = false;
        rebuild();
    }

    public void showEmpty() {
        currentRecipe = null;
        editingAnnotationId = null;
        showingAddForm = false;
        rebuild();
    }

    // ── Rebuild ────────────────────────────────────────────────────────────────

    private void rebuild() {
        entriesPanel.removeAll();

        if (currentRecipe == null) {
            entriesPanel.add(centeredLabel("Select a recipe to view annotations."));
        } else if (currentRecipe.getAnnotations().isEmpty() && !showingAddForm) {
            entriesPanel.add(centeredLabel("No annotations yet."));
        } else {
            for (Annotation a : currentRecipe.getAnnotations()) {
                JPanel entry = a.getId().equals(editingAnnotationId)
                        ? buildEditEntry(a)
                        : buildViewEntry(a);
                entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, entry.getPreferredSize().height + 8));
                entriesPanel.add(entry);
                entriesPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            }
        }

        if (showingAddForm && currentRecipe != null) {
            entriesPanel.add(buildAddForm());
        }

        entriesPanel.revalidate();
        entriesPanel.repaint();
    }

    // ── Entry builders ─────────────────────────────────────────────────────────

    private JPanel buildViewEntry(Annotation a) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 4, 3, 4),
                BorderFactory.createEtchedBorder()));

        JLabel ts = new JLabel(formatTs(a.getCreatedAt()));
        ts.setForeground(Color.GRAY);
        ts.setFont(ts.getFont().deriveFont(10f));

        JButton editBtn = new JButton("Edit");
        editBtn.setMargin(new Insets(1, 6, 1, 6));
        JButton delBtn = new JButton("✕");
        delBtn.setMargin(new Insets(1, 4, 1, 4));
        delBtn.setToolTipText("Delete");
        delBtn.setForeground(new Color(160, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.add(ts, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        btns.add(editBtn);
        btns.add(delBtn);
        header.add(btns, BorderLayout.EAST);

        JTextArea text = new JTextArea(a.getText());
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        p.add(header, BorderLayout.NORTH);
        p.add(text, BorderLayout.CENTER);

        editBtn.addActionListener(e -> {
            editingAnnotationId = a.getId();
            showingAddForm = false;
            rebuild();
        });
        delBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete this annotation?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                currentRecipe.removeAnnotation(a.getId());
                saveQuietly();
                editingAnnotationId = null;
                rebuild();
            }
        });

        return p;
    }

    private JPanel buildEditEntry(Annotation a) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 4, 3, 4),
                BorderFactory.createLineBorder(new Color(100, 130, 200))));

        JLabel ts = new JLabel(formatTs(a.getCreatedAt()));
        ts.setForeground(Color.GRAY);
        ts.setFont(ts.getFont().deriveFont(10f));
        p.add(ts, BorderLayout.NORTH);

        JTextArea editor = new JTextArea(a.getText(), 3, 0);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        p.add(new JScrollPane(editor), BorderLayout.CENTER);

        JLabel err = new JLabel(" ");
        err.setForeground(new Color(180, 0, 0));
        err.setFont(err.getFont().deriveFont(10f));

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.add(err);
        btns.add(cancelBtn);
        btns.add(saveBtn);
        p.add(btns, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            String txt = editor.getText().trim();
            if (txt.isEmpty()) {
                err.setText("Text cannot be empty");
                return;
            }
            // Modify the annotation object directly (same reference held by currentRecipe)
            Annotation live = currentRecipe.findAnnotation(a.getId());
            if (live != null) live.setText(txt);
            saveQuietly();
            editingAnnotationId = null;
            rebuild();
        });
        cancelBtn.addActionListener(e -> {
            editingAnnotationId = null;
            rebuild();
        });

        return p;
    }

    private JPanel buildAddForm() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 4, 3, 4),
                BorderFactory.createLineBorder(new Color(80, 160, 80))));

        p.add(new JLabel("New annotation:"), BorderLayout.NORTH);

        JTextArea addArea = new JTextArea(3, 0);
        addArea.setLineWrap(true);
        addArea.setWrapStyleWord(true);
        p.add(new JScrollPane(addArea), BorderLayout.CENTER);

        JLabel err = new JLabel(" ");
        err.setForeground(new Color(180, 0, 0));
        err.setFont(err.getFont().deriveFont(10f));

        JButton submitBtn = new JButton("Submit");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.add(err);
        btns.add(cancelBtn);
        btns.add(submitBtn);
        p.add(btns, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> {
            String txt = addArea.getText().trim();
            if (txt.isEmpty()) {
                err.setText("Text cannot be empty");
                return;
            }
            currentRecipe.addAnnotation(Annotation.create(txt));
            saveQuietly();
            showingAddForm = false;
            rebuild();
        });
        cancelBtn.addActionListener(e -> {
            showingAddForm = false;
            rebuild();
        });

        return p;
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    private void saveQuietly() {
        try {
            service.save(currentRecipe);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel centeredLabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(Color.GRAY);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private String formatTs(String iso) {
        try {
            return TS_FMT.format(Instant.parse(iso));
        } catch (Exception e) {
            return iso;
        }
    }
}
