package com.recipes;

import com.recipes.service.RecipeService;
import com.recipes.ui.MainFrame;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        File recipesDir = resolveRecipesDir(args);
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            RecipeService service = new RecipeService(recipesDir);
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }

    private static File resolveRecipesDir(String[] args) {
        if (args.length > 0) {
            return new File(args[0]);
        }
        return new File(System.getProperty("user.home"), "recipes");
    }
}
