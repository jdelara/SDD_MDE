package com.cookingrecipes;

import com.cookingrecipes.service.RecipeService;
import com.cookingrecipes.storage.RecipeStore;
import com.cookingrecipes.ui.MainWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        RecipeStore store = new RecipeStore();
        store.load();
        RecipeService service = new RecipeService(store);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainWindow(service).setVisible(true);
        });
    }
}
