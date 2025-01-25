package org.alex_hashtag.errors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class TokenizationErrorManager
{

    private final List<TokenizationError> errors = new ArrayList<>();
    private final String fileName;        // store which file we're tokenizing
    private List<String> sourceLines;     // for line-by-line references

    public TokenizationErrorManager(String fileName, String source)
    {
        this.fileName = fileName;
        setSource(source);
    }

    private void setSource(String source)
    {
        this.sourceLines = List.of(source.split("\n", -1));
    }

    public void reportError(TokenizationError error)
    {
        errors.add(error);
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public void printErrors(PrintStream out)
    {
        for (TokenizationError error : errors)
        {
            printSingleError(out, error);
        }
    }

    private void printSingleError(PrintStream out, TokenizationError error)
    {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_BOLD = "\u001B[1m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_YELLOW = "\u001B[33m";

        // Show file name in the error line
        out.println(ANSI_BOLD + ANSI_RED + "error [" + fileName + "]: " + ANSI_RESET + error.getMessage());
        out.printf("  --> line %d:%d%n", error.getLine(), error.getColumn());

        if (error.getLine() > 0 && error.getLine() <= sourceLines.size())
        {
            String lineText = sourceLines.get(error.getLine() - 1);
            out.println("   |");
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

    public enum ErrorType
    {
        MISSING_PACKAGE("Missing package statement at the start of the file."),
        WRONG_PACKAGE_PATH("Package declaration does not match the file system structure."),
        INVALID_TOKEN("Unrecognized or invalid token."),
        UNCLOSED_STRING("String literal is not properly closed."),
        INVALID_CHAR_LITERAL("Character literal has multiple characters or invalid escape sequence."),
        INVALID_SYNTAX("Unexpected syntax error.");

        private final String description;

        ErrorType(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }

    public static class TokenizationError
    {
        private final ErrorType type;
        private final String message;
        private final int line;
        private final int column;
        private final String token;
        private final String hint;

        public TokenizationError(ErrorType type,
                                 String message,
                                 int line,
                                 int column,
                                 String token)
        {
            this(type, message, line, column, token, null);
        }

        public TokenizationError(ErrorType type,
                                 String message,
                                 int line,
                                 int column,
                                 String token,
                                 String hint)
        {
            this.type = type;
            this.message = message;
            this.line = line;
            this.column = column;
            this.token = token;
            this.hint = hint;
        }

        public ErrorType getType()
        {
            return type;
        }

        public String getMessage()
        {
            return message;
        }

        public int getLine()
        {
            return line;
        }

        public int getColumn()
        {
            return column;
        }

        public String getToken()
        {
            return token;
        }

        public String getHint()
        {
            return hint;
        }
    }
}
