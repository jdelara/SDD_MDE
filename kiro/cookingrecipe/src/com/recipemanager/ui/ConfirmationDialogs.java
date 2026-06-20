package com.recipemanager.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable confirmation dialogs for destructive actions.
 */
public class ConfirmationDialogs {

    private ConfirmationDialogs() {
        // Utility class - no instantiation
    }

    /**
     * Shows a confirmation dialog asking the user to confirm deletion of a recipe.
     *
     * @param parent the parent component for the dialog
     * @param recipeName the name of the recipe to delete
     * @return true if the user confirmed the deletion, false if cancelled
     */
    public static boolean confirmDeleteRecipe(Component parent, String recipeName) {
        String message = "Are you sure you want to delete '" + recipeName + "'?";
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}
