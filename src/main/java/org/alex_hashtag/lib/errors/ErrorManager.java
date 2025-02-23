package org.alex_hashtag.lib.errors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic error manager for handling compilation errors.
 * @param <T> The type of errors managed (must implement CompilerError).
 */
public class ErrorManager<T extends CompilerError> {

    private final List<T> errors = new ArrayList<>();
    private final String fileName;
    private final List<String> sourceLines;

    public ErrorManager(String fileName, String source) {
        this.fileName = fileName;
        this.sourceLines = List.of(source.split("\n", -1));
    }

    public void reportError(T error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<T> getErrors() {
        return errors;
    }

    public void printErrors(PrintStream out) {
        for (T error : errors) {
            printSingleError(out, error);
        }
    }

    private void printSingleError(PrintStream out, T error) {
        out.println(ANSI.BOLD + ANSI.RED + "error [" + fileName + "]: " + ANSI.RESET);
        out.println(error.getMessage());
        out.printf("  --> line %d:%d%n", error.getLine(), error.getColumn());

        if (error.getLine() > 0 && error.getLine() <= sourceLines.size()) {
            String lineText = sourceLines.get(error.getLine() - 1);
            out.println("   |");
            out.printf("%2d | %s%n", error.getLine(), lineText);

            // Underline the offending token
            int underlineStart = error.getColumn() - 1;
            int caretCount = Math.max(1, error.getToken().length());
            StringBuilder underline = new StringBuilder("   | ");

            underline.append(" ".repeat(underlineStart));
            underline.append(ANSI.BOLD + ANSI.GREEN + "^".repeat(caretCount) + ANSI.RESET);
            out.println(underline);
        }

        if (error.getHint() != null && !error.getHint().isEmpty()) {
            out.println(ANSI.YELLOW + "  = help: " + error.getHint() + ANSI.RESET);
        }

        out.println();
    }

    /**
     * ANSI escape codes for colored output.
     */
    private static class ANSI {
        static final String RED = "\u001B[31m";
        static final String GREEN = "\u001B[32m";
        static final String YELLOW = "\u001B[33m";
        static final String BOLD = "\u001B[1m";
        static final String RESET = "\u001B[0m";
    }
}
