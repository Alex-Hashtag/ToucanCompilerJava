package org.alex_hashtag.errors;

import lombok.Getter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class ParsingErrorManager
{

    private final List<ParsingError> errors = new ArrayList<>();
    private final String fileName;        // store which file we're tokenizing
    private List<String> sourceLines;     // for line-by-line references

    public ParsingErrorManager(String fileName, String source)
    {
        this.fileName = fileName;
        setSource(source);
    }

    private void setSource(String source)
    {
        this.sourceLines = List.of(source.split("\n", -1));
    }

    public void reportError(ParsingError error)
    {
        errors.add(error);
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public void printErrors(PrintStream out)
    {
        for (ParsingError error : errors)
        {
            printSingleError(out, error);
        }
    }

    private void printSingleError(PrintStream out, ParsingError error)
    {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_BOLD = "\u001B[1m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_YELLOW = "\u001B[33m";

        // Show file name in the error line
        out.println(ANSI_BOLD + ANSI_RED + "error [" + fileName + "]: " + ANSI_RESET + '\n' + error.getMessage());
        out.printf("  --> line %d:%d%n", error.getLine(), error.getColumn());

        out.println("   |");

        // Find the nearest non-empty line above the error line
        int nearestNonEmptyLine = -1;
        for (int i = error.getLine() - 2; i >= 0; i--) // Start from the line above the error
        {
            if (!sourceLines.get(i).trim().isEmpty())
            {
                nearestNonEmptyLine = i + 1; // Line numbers are 1-based
                break;
            }
        }

        // Print the nearest non-empty line above the error, if it exists
        if (nearestNonEmptyLine > 0)
        {
            String nearestLineText = sourceLines.get(nearestNonEmptyLine - 1);
            out.printf("%2d | %s%n", nearestNonEmptyLine, nearestLineText);
        }

        // Print the current line with the error
        if (error.getLine() > 0 && error.getLine() <= sourceLines.size())
        {
            String lineText = sourceLines.get(error.getLine() - 1);
            out.printf("%2d | %s%n", error.getLine(), lineText);

            // Underline the offending token
            int underlineStart = error.getColumn() - 1;
            int caretCount = Math.max(1, error.getToken().length());

            StringBuilder underline = new StringBuilder("   | ");
            for (int i = 0; i < underlineStart; i++)
            {
                underline.append(' ');
            }
            for (int i = 0; i < caretCount; i++)
            {
                underline.append('^');
            }
            out.println(ANSI_BOLD + ANSI_GREEN + underline + ANSI_RESET);
        }

        if (error.getHint() != null && !error.getHint().isEmpty())
        {
            out.println(ANSI_YELLOW + "  = help: " + error.getHint() + ANSI_RESET);
        }

        out.println();
    }


    @Getter
    public enum ErrorType
    {
        EXPECTED_FOUND("Expected %s found %s");

        private final String description;

        ErrorType(String description)
        {
            this.description = description;
        }

    }

    @Getter
    public static class ParsingError
    {
        private final ErrorType type;
        private final String message;
        private final int line;
        private final int column;
        private final String token;
        private final String hint;

        public ParsingError(ErrorType type,
                            String message,
                            int line,
                            int column,
                            String token,
                            String hint,
                            String... args)
        {
            this.type = type;
            this.message = String.format(message, args);
            this.line = line;
            this.column = column;
            this.token = token;
            this.hint = hint;
        }

        public static ParsingError withHint(
                ErrorType type,
                String message,
                int line,
                int column,
                String token,
                String hint,
                String... args
        )
        {
            return new ParsingError(type, message, line, column, token, hint, args);
        }

        public static ParsingError of(
                ErrorType type,
                String message,
                int line,
                int column,
                String token,
                String hint,
                String... args
        )
        {
            return new ParsingError(type, message, line, column, token, hint, args);
        }

    }
}
