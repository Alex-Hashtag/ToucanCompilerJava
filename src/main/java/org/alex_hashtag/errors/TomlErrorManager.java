package org.alex_hashtag.errors;

import java.util.ArrayList;
import java.util.List;


public class TomlErrorManager
{
    private final List<String> errors = new ArrayList<>();

    /**
     * Adds an error message to the manager.
     *
     * @param message The error message to add.
     */
    public void addError(String message)
    {
        errors.add(message);
    }

    /**
     * Adds an error message with context.
     *
     * @param context The context where the error occurred.
     * @param message The error message.
     */
    public void addError(String context, String message)
    {
        errors.add(context + ": " + message);
    }

    /**
     * Checks if any errors have been recorded.
     *
     * @return True if there are errors, false otherwise.
     */
    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    /**
     * Prints all recorded errors to the console.
     */
    public void printErrors()
    {
        System.err.println("Compilation Failed with the following errors:");
        for (String error : errors)
        {
            System.err.println("  - " + error);
        }
    }

    /**
     * Throws an exception if there are any recorded errors.
     *
     * @throws IllegalStateException If errors are present.
     */
    public void throwIfErrors()
    {
        if (hasErrors())
        {
            printErrors();
            throw new IllegalStateException("Failed to parse TOML configuration due to errors.");
        }
    }
}
