package com.recipemanager.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging errors and warnings to the application's error log file.
 * Log file location: {user.home}/.recipe-manager/error.log
 * Log format: [ISO-8601 timestamp] [LEVEL] message
 */
public class ErrorLogger {

    private static final Path LOG_FILE = Paths.get(
            System.getProperty("user.home"), ".recipe-manager", "error.log");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ErrorLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Logs a WARN level message.
     *
     * @param message the warning message to log
     */
    public static void warn(String message) {
        log("WARN", message);
    }

    /**
     * Logs an ERROR level message.
     *
     * @param message the error message to log
     */
    public static void error(String message) {
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        try {
            Path parentDir = LOG_FILE.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            String entry = String.format("[%s] [%s] %s%n",
                    LocalDateTime.now().format(FORMATTER), level, message);

            Files.writeString(LOG_FILE, entry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Last resort: print to stderr if we can't write to the log file
            System.err.println("Failed to write to error log: " + e.getMessage());
        }
    }
}
