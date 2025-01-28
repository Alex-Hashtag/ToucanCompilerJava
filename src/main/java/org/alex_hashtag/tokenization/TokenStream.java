package org.alex_hashtag.tokenization;

import lombok.Getter;
import org.alex_hashtag.errors.TokenizationErrorManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.alex_hashtag.tokenization.TokenType.*;


public class TokenStream implements Iterable<Token>
{
    private static final Map<String, TokenType> multiCharOperatorMap = new LinkedHashMap<>();
    private static final Map<String, TokenType> singleCharTokenMap = new HashMap<>();
    private static final Set<String> keywordsSet = new HashSet<>();
    private static final Map<String, TokenType> keywordMap = new HashMap<>();

    static
    {
        // Initialize multi-character operators
        for (TokenType type : List.of(
                BIT_SHIFT_LEFT_EQUALS, BIT_SHIFT_RIGHT_EQUALS, BIT_SHIFT_RIGHT_UNSIGNED_EQUALS,
                BIT_SHIFT_LEFT, BIT_SHIFT_RIGHT, BIT_SHIFT_RIGHT_UNSIGNED,
                EQUALITY, INEQUALITY, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
                ADDITION_ASSIGN, SUBTRACTION_ASSIGN, MULTIPLICATION_ASSIGN,
                DIVISION_ASSIGN, MODULO_ASSIGN, BITWISE_AND_ASSIGN,
                BITWISE_OR_ASSIGN, BITWISE_XOR_ASSIGN, INCREMENT, DECREMENT,
                ARROW, DOUBLE_COLON, LOGICAL_AND, LOGICAL_OR))
        {
            multiCharOperatorMap.put(type.regex, type);
        }
    }

    static
    {
        // Initialize single-character tokens
        singleCharTokenMap.put(";", SEMI_COLON);
        singleCharTokenMap.put(":", COLON);
        singleCharTokenMap.put(".", DOT);
        singleCharTokenMap.put(",", COMMA);
        singleCharTokenMap.put("(", BRACE_OPEN);
        singleCharTokenMap.put(")", BRACE_CLOSED);
        singleCharTokenMap.put("[", BRACKET_OPEN);
        singleCharTokenMap.put("]", BRACKET_CLOSED);
        singleCharTokenMap.put("{", CURLY_OPEN);
        singleCharTokenMap.put("}", CURLY_CLOSED);
        singleCharTokenMap.put("<", ARROW_OPEN);
        singleCharTokenMap.put(">", ARROW_CLOSED);
        singleCharTokenMap.put("=", ASSIGNMENT);
        singleCharTokenMap.put("+", ADDITION);
        singleCharTokenMap.put("-", SUBTRACTION);
        singleCharTokenMap.put("*", MULTIPLICATION);
        singleCharTokenMap.put("/", DIVISION);
        singleCharTokenMap.put("%", MODULO);
        singleCharTokenMap.put("&", BITWISE_AND);
        singleCharTokenMap.put("|", BITWISE_OR);
        singleCharTokenMap.put("^", BITWISE_XOR);
        singleCharTokenMap.put("~", BITWISE_NOT);
        singleCharTokenMap.put("!", LOGICAL_NOT);
        singleCharTokenMap.put("?", QUESTION);
    }

    static
    {
        // Initialize keywords
        String[] keywords = {
                "mutable", "const", "static", "this", "constructor",
                "if", "else", "class", "while", "do", "for",
                "loop", "switch", "null", "continue", "template",
                "yield", "struct", "implement", "implements", "public",
                "private", "protected", "implicit", "extends", "super",
                "abstract", "trait", "enum", "error", "sys", "typeof",
                "unsafe", "sizeof", "typedef", "operations", "import",
                "package", "true", "false", "return", "echo", "inline",
                "break", "macro", "annotation"
        };
        for (String kw : keywords)
        {
            keywordsSet.add(kw);
            keywordMap.put(kw, valueOf(kw.toUpperCase()));
        }
    }

    // Token collection, etc.
    public final List<Token> tokens;
    // Reference to our manager
    private TokenizationErrorManager errorManager;
    @Getter
    private List<ImportDeclaration> imports;
    @Getter
    private String packageName = null;
    @Getter
    private String filename;
    @Getter
    private String source;

    /**
     * Constructor used when creating a TokenStream from existing tokens.
     */
    public TokenStream(String filename, String input, List<Token> tokens)
    {
        this.filename = filename;
        this.source = input;
        this.tokens = tokens;
    }

    /**
     * Constructor that tokenizes the input string.
     */
    public TokenStream(Path filePath, String input)
    {
        this.filename = filePath.toString();
        this.source = input;
        this.imports = new ArrayList<>();
        this.tokens = new LinkedList<>();

        // Initialize the error manager with the file path and file contents
        this.errorManager = new TokenizationErrorManager(
                filePath.toAbsolutePath().toString(),  // full file path
                input                            // entire text for line-by-line references
        );

        tokens.add(Token.getStart());

        // Normalize line endings
        input = input.replace("\r\n", "\n").replace("\r", "\n");

        StringBuilder nonImportLines = new StringBuilder();
        String[] lines = input.split("\n", -1);
        for (String line : lines)
        {
            String trimmed = line.trim();
            if (trimmed.startsWith("package "))
            {
                String afterPkg = trimmed.substring("package ".length()).trim();
                if (afterPkg.endsWith(";"))
                {
                    afterPkg = afterPkg.substring(0, afterPkg.length() - 1).trim();
                }
                this.packageName = afterPkg;
                nonImportLines.append("\n");
            }
            else if (trimmed.startsWith("import "))
            {
                parseImport(false, trimmed.substring("import ".length()).trim());
                nonImportLines.append("\n");
            }
            else if (trimmed.startsWith("static import "))
            {
                parseImport(true, trimmed.substring("static import ".length()).trim());
                nonImportLines.append("\n");
            }
            else
            {
                nonImportLines.append(line).append('\n');
            }
        }

        // Now tokenize everything else
        tokenize(nonImportLines.toString());

        // Example post-check: no package? => error
        if (this.packageName == null)
        {
            errorManager.reportError(
                    new TokenizationErrorManager.TokenizationError(
                            TokenizationErrorManager.ErrorType.MISSING_PACKAGE,
                            "Source file does not begin with a 'package' statement.",
                            1,
                            1,
                            "N/A",
                            "Add a package declaration, e.g. 'package com.example;'"
                    )
            );
        }

        if (errorManager.hasErrors())
        {
            errorManager.printErrors(System.err);
            System.exit(1);
        }
    }

    private void parseImport(boolean isStatic, String importLine)
    {
        if (importLine.endsWith(";"))
        {
            importLine = importLine.substring(0, importLine.length() - 1).trim();
        }
        if (importLine.endsWith(".*"))
        {
            String base = importLine.substring(0, importLine.length() - 2).trim();
            imports.add(new ImportDeclaration(isStatic, base, "*"));
        }
        else
        {
            int lastDot = importLine.lastIndexOf('.');
            if (lastDot < 0)
            {
                imports.add(new ImportDeclaration(isStatic, importLine, null));
            }
            else
            {
                String base = importLine.substring(0, lastDot);
                String member = importLine.substring(lastDot + 1);
                imports.add(new ImportDeclaration(isStatic, base, member));
            }
        }
    }

    private void tokenize(String input)
    {
        int index = 0;
        int row = 1;
        int column = 0;

        loop:
        while (index < input.length())
        {
            char currentChar = input.charAt(index);

            // Handle whitespace
            if (currentChar == ' ' || currentChar == '\t' || currentChar == '\r')
            {
                column++;
                index++;
                continue;
            }
            if (currentChar == '\n')
            {
                row++;
                column = 0;
                index++;
                continue;
            }

            // Handle comments
            if (input.startsWith("//", index))
            {
                // Single-line comment
                int startColumn = column;
                int startIndex = index;
                index += 2;
                column += 2;
                while (index < input.length() && input.charAt(index) != '\n')
                {
                    index++;
                    column++;
                }
                String commentText = input.substring(startIndex, index);
                tokens.add(Token.stored(row, startColumn, COMMENT, commentText));
                continue;
            }
            if (input.startsWith("/*", index))
            {
                // Multi-line comment
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index += 2;
                column += 2;
                while (index < input.length())
                {
                    if (input.startsWith("*/", index))
                    {
                        index += 2;
                        column += 2;
                        break;
                    }
                    else
                    {
                        if (input.charAt(index) == '\n')
                        {
                            row++;
                            column = 0;
                        }
                        else
                        {
                            column++;
                        }
                        index++;
                    }
                }
                String commentText = input.substring(startIndex, index);
                tokens.add(Token.stored(startRow, startColumn, COMMENT, commentText));
                continue;
            }

            // Handle triple-quoted strings ("""...""")
            if (input.startsWith("\"\"\"", index))
            {
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index += 3;
                column += 3;
                while (index < input.length())
                {
                    if (input.startsWith("\"\"\"", index))
                    {
                        index += 3;
                        column += 3;
                        break;
                    }
                    else
                    {
                        if (input.charAt(index) == '\n')
                        {
                            row++;
                            column = 0;
                        }
                        else
                        {
                            column++;
                        }
                        index++;
                    }
                }
                String stringText = input.substring(startIndex, index);
                tokens.add(Token.stored(startRow, startColumn, STRING_LITERAL, stringText));
                continue;
            }

            // Handle normal strings
            if (currentChar == '"')
            {
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index++;
                column++;
                while (index < input.length())
                {
                    if (index >= input.length()) break;
                    char c = input.charAt(index);
                    if (c == '\\')
                    {
                        // Skip escaped char
                        index += 2;
                        column += 2;
                    }
                    else if (c == '"')
                    {
                        index++;
                        column++;
                        break;
                    }
                    else
                    {
                        if (c == '\n')
                        {
                            // Found newline before closing quote => unclosed string
                            tokens.add(Token.stored(startRow, startColumn, INVALID,
                                    input.substring(startIndex, index)));

                            errorManager.reportError(new TokenizationErrorManager.TokenizationError(
                                    TokenizationErrorManager.ErrorType.UNCLOSED_STRING,
                                    "String literal not closed before newline.",
                                    row, column,
                                    input.substring(startIndex, index)
                            ));

                            row++;
                            column = 0;
                            continue loop;
                        }
                        column++;
                        index++;
                    }
                }
                String stringText = input.substring(startIndex, index);
                tokens.add(Token.stored(startRow, startColumn, STRING_LITERAL, stringText));
                continue;
            }

            // Handle character literals
            if (currentChar == '\'')
            {
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index++;
                column++;
                while (index < input.length())
                {
                    if (index >= input.length()) break;
                    char c = input.charAt(index);
                    if (c == '\\')
                    {
                        index += 2;
                        column += 2;
                    }
                    else if (c == '\'')
                    {
                        index++;
                        column++;
                        break;
                    }
                    else
                    {
                        if (c == '\n')
                        {
                            // Possibly unclosed char literal
                            tokens.add(Token.stored(startRow, startColumn, INVALID,
                                    input.substring(startIndex, index)));

                            errorManager.reportError(new TokenizationErrorManager.TokenizationError(
                                    TokenizationErrorManager.ErrorType.INVALID_CHAR_LITERAL,
                                    "Unclosed character literal before newline.",
                                    row, column,
                                    input.substring(startIndex, index)
                            ));

                            row++;
                            column = 0;
                            continue loop;
                        }
                        index++;
                        column++;
                    }
                }
                String charText = input.substring(startIndex, index);

                // Check if it's more than one character (heuristic)
                if (charText.length() > 4)
                {
                    tokens.add(Token.stored(startRow, startColumn, INVALID, charText));
                    errorManager.reportError(new TokenizationErrorManager.TokenizationError(
                            TokenizationErrorManager.ErrorType.INVALID_CHAR_LITERAL,
                            "Character literal has multiple characters.",
                            startRow, startColumn,
                            charText
                    ));
                }
                else
                {
                    tokens.add(Token.stored(startRow, startColumn, CHAR_LITERAL, charText));
                }
                continue;
            }

            // Handle annotation usage: e.g. @Getter
            if (currentChar == '@')
            {
                int startRow = row;
                int startColumn = column;
                index++;
                column++;

                StringBuilder annotationName = new StringBuilder();
                while (index < input.length())
                {
                    char nc = input.charAt(index);
                    if (!Character.isLetterOrDigit(nc) && nc != '_')
                        break;
                    annotationName.append(nc);
                    index++;
                    column++;
                }
                String combined = "@" + annotationName;
                tokens.add(Token.stored(startRow, startColumn, ANNOTATION_USE, combined));
                continue;
            }

            // ** Detect '$(' first => MACRO_REPEAT_OPEN **
            if (input.startsWith("$(", index))
            {
                int startColumn = column;
                tokens.add(Token.basic(row, startColumn, MACRO_REPEAT_OPEN));
                index += 2; // skip '$('
                column += 2;
                continue;
            }

            // Handle macro usage: e.g. sum!(...)
            {
                int lookAheadIndex = index;
                Matcher macroCheck = Pattern.compile(IDENTIFIER.regex).matcher(input.substring(lookAheadIndex));
                if (macroCheck.lookingAt())
                {
                    String possibleMacroName = macroCheck.group();
                    int nameLen = possibleMacroName.length();
                    if (lookAheadIndex + nameLen < input.length()
                            && input.charAt(lookAheadIndex + nameLen) == '!')
                    {
                        char nextSym = (lookAheadIndex + nameLen + 1 < input.length())
                                ? input.charAt(lookAheadIndex + nameLen + 1)
                                : '\0';

                        if (nextSym == '(' || nextSym == '[' || nextSym == '{')
                        {
                            int startRow = row;
                            int startColumn = column;
                            // Advance over macro name + '!'
                            index += nameLen + 1;
                            column += nameLen + 1;

                            StringBuilder macroContent = new StringBuilder(possibleMacroName + "!");
                            Deque<Character> stack = new ArrayDeque<>();
                            stack.push(nextSym);
                            macroContent.append(nextSym);
                            index++;
                            column++;

                            while (!stack.isEmpty() && index < input.length())
                            {
                                char c = input.charAt(index);
                                macroContent.append(c);
                                index++;
                                column++;
                                if (c == '(' || c == '[' || c == '{')
                                {
                                    stack.push(c);
                                }
                                else if (c == ')' || c == ']' || c == '}')
                                {
                                    char open = stack.peek();
                                    if ((open == '(' && c == ')')
                                            || (open == '[' && c == ']')
                                            || (open == '{' && c == '}'))
                                    {
                                        stack.pop();
                                    }
                                    else
                                    {
                                        // Mismatched bracket => optional error check
                                        stack.pop();
                                    }
                                }
                            }
                            tokens.add(Token.stored(startRow, startColumn, MACRO_USE, macroContent.toString()));
                            continue;
                        }
                    }
                }
            }

            // Handle multi-character operators
            boolean matchedOperator = false;
            for (Map.Entry<String, TokenType> entry : multiCharOperatorMap.entrySet())
            {
                String op = entry.getKey();
                if (input.startsWith(op, index))
                {
                    int startColumn = column;
                    tokens.add(Token.basic(row, startColumn, entry.getValue()));
                    index += op.length();
                    column += op.length();
                    matchedOperator = true;
                    break;
                }
            }
            if (matchedOperator)
                continue;

            // Check for macro variables: $foo
            if (currentChar == '$')
            {
                Matcher macroVarMatcher = Pattern.compile(MACRO_VARIABLE.regex)
                        .matcher(input.substring(index));
                if (macroVarMatcher.lookingAt())
                {
                    String macroVar = macroVarMatcher.group();
                    int startColumn = column;
                    tokens.add(Token.stored(row, startColumn, MACRO_VARIABLE, macroVar));
                    index += macroVar.length();
                    column += macroVar.length();
                    continue;
                }
            }

            // Match float literal
            Matcher floatMatcher = Pattern.compile(FLOAT_LITERAL.regex).matcher(input.substring(index));
            if (floatMatcher.lookingAt())
            {
                int startColumn = column;
                String floatLiteral = floatMatcher.group();
                tokens.add(Token.stored(row, startColumn, FLOAT_LITERAL, floatLiteral));
                index += floatLiteral.length();
                column += floatLiteral.length();
                continue;
            }

            // Match int literal
            Matcher intMatcher = Pattern.compile(INT_LITERAL.regex).matcher(input.substring(index));
            if (intMatcher.lookingAt())
            {
                int startColumn = column;
                String intLiteral = intMatcher.group();
                tokens.add(Token.stored(row, startColumn, INT_LITERAL, intLiteral));
                index += intLiteral.length();
                column += intLiteral.length();
                continue;
            }

            // Handle identifiers / keywords
            Matcher idMatcher = Pattern.compile(IDENTIFIER.regex).matcher(input.substring(index));
            if (idMatcher.lookingAt())
            {
                String word = idMatcher.group();
                int startColumn = column;

                // Check macro-specific keywords
                if (word.equals("expression"))
                {
                    tokens.add(Token.basic(row, startColumn, MACRO_EXPR));
                }
                else if (word.equals("identifier"))
                {
                    tokens.add(Token.basic(row, startColumn, MACRO_IDENT));
                }
                else if (keywordsSet.contains(word))
                {
                    tokens.add(Token.basic(row, startColumn, keywordMap.get(word)));
                }
                else
                {
                    // Normal identifier
                    tokens.add(Token.stored(row, startColumn, IDENTIFIER, word));
                }

                index += word.length();
                column += word.length();
                continue;
            }

            // Handle single-char operators/punctuations
            String ch = String.valueOf(input.charAt(index));
            if (singleCharTokenMap.containsKey(ch))
            {
                int startColumn = column;
                tokens.add(Token.basic(row, startColumn, singleCharTokenMap.get(ch)));
                index++;
                column++;
                continue;
            }

            // If we reach here, it's an invalid token
            int startColumn = column;
            tokens.add(Token.stored(row, startColumn, INVALID, String.valueOf(input.charAt(index))));
            errorManager.reportError(new TokenizationErrorManager.TokenizationError(
                    TokenizationErrorManager.ErrorType.INVALID_TOKEN,
                    "Unrecognized token encountered.",
                    row, column,
                    String.valueOf(input.charAt(index))
            ));
            index++;
            column++;
        }

        tokens.add(Token.getEnd()); // END token
    }

    /**
     * Provides a string representation of all tokens.
     * Useful for writing tokens to an output file.
     *
     * @return A formatted string of all tokens.
     */
    public String getTokensAsString()
    {
        StringBuilder sb = new StringBuilder();

        // Define headers
        String[] headers = {"Type", "Content", "Row", "Column"};
        int[] columnWidths = new int[headers.length];

        // Initialize column widths based on headers
        for (int i = 0; i < headers.length; i++)
        {
            columnWidths[i] = headers[i].length();
        }

        // Calculate maximum width for each column based on token data
        for (Token token : tokens)
        {
            columnWidths[0] = Math.max(columnWidths[0], token.type.toString().length());
            String content = token.internal.orElse("");
            columnWidths[1] = Math.max(columnWidths[1], content.length());
            String rowStr = String.valueOf(token.coordinates.row());
            columnWidths[2] = Math.max(columnWidths[2], rowStr.length());
            String colStr = String.valueOf(token.coordinates.column());
            columnWidths[3] = Math.max(columnWidths[3], colStr.length());
        }

        // Define a format string for the table rows
        String format = String.format("    %%-%ds | %%-%ds | %%-%ds | %%-%ds\n",
                columnWidths[0], columnWidths[1], columnWidths[2], columnWidths[3]);

        // Append headers
        sb.append("    ");
        for (int i = 0; i < headers.length; i++)
        {
            sb.append(String.format("%-" + columnWidths[i] + "s", headers[i]));
            if (i < headers.length - 1) sb.append(" | ");
        }
        sb.append("\n");

        // Append a separator line
        sb.append("    ");
        for (int i = 0; i < headers.length; i++)
        {
            sb.append("-".repeat(columnWidths[i]));
            if (i < headers.length - 1) sb.append("-+-");
        }
        sb.append("\n");

        // Append each token's data
        for (Token token : tokens)
        {
            String type = token.type.toString();
            String content = token.internal.orElse("");
            String rowStr = String.valueOf(token.coordinates.row());
            String colStr = String.valueOf(token.coordinates.column());

            sb.append(String.format(format, type, content, rowStr, colStr));
        }

        return sb.toString();
    }

    /**
     * Overrides the default toString method to provide a detailed string representation of the TokenStream.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TokenStream {\n");
        sb.append("  Filename: ").append(filename).append("\n");
        sb.append("  Package Name: ").append(packageName != null ? packageName : "N/A").append("\n");
        sb.append("  Imports:\n");
        if (imports != null && !imports.isEmpty())
        {
            for (ImportDeclaration imp : imports)
            {
                sb.append("    - ").append(imp).append("\n");
            }
        }
        else
        {
            sb.append("    N/A\n");
        }
        sb.append("  Tokens:\n");
        sb.append(getTokensAsString());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public @NotNull Iterator<Token> iterator()
    {
        return Collections.unmodifiableList(tokens).iterator();
    }

    @Override
    public void forEach(Consumer<? super Token> action)
    {
        Collections.unmodifiableList(tokens).forEach(action);
    }

    @Override
    public Spliterator<Token> spliterator()
    {
        return Collections.unmodifiableList(tokens).spliterator();
    }
}
