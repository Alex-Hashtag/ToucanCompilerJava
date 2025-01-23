package org.alex_hashtag.tokenization;

import lombok.Getter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.alex_hashtag.tokenization.TokenType.*;

/**
 * The TokenStream class is responsible for tokenizing input into a sequence of tokens.
 * It manages various mappings for single-character tokens, multi-character operators, and keywords.
 * This class is designed to handle the parsing of input strings based on predefined token types.
 */
public class TokenStream
{
    // Keep your existing static maps, etc.
    private static final Map<String, TokenType> multiCharOperatorMap = new LinkedHashMap<>();
    private static final Map<String, TokenType> singleCharTokenMap = new HashMap<>();
    private static final Set<String> keywordsSet = new HashSet<>();
    private static final Map<String, TokenType> keywordMap = new HashMap<>();

    static
    {
        // (Unchanged) multi-char operators
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
        // (Unchanged) single-char tokens
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
    }

    static
    {
        // (Unchanged) keywords
        String[] keywords = {
                "void", "int8", "int16", "int32", "int64", "int128",
                "uint8", "uint16", "uint32", "uint64", "uint128",
                "float16", "float32", "float64", "float80", "float128",
                "bool", "char", "rune", "string", "type", "var", "lambda",
                "mutable", "const", "static", "this", "constructor",
                "if", "else", "class", "namespace", "while", "do", "for",
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
            keywordMap.put(kw, TokenType.valueOf(kw.toUpperCase()));
        }
    }

    // The existing tokens list
    public final LinkedList<Token> tokens;
    @Getter
    private final List<ImportDeclaration> imports = new ArrayList<>();
    // ----------------------------------------------
    // New fields for the package name & import list:
    // ----------------------------------------------
    @Getter
    private String packageName = null;

    /**
     * Parses the given input string into a stream of tokens. The method analyzes the input for
     * various types of lexical elements (e.g., comments, string literals, character literals,
     * annotations, macros) and stores them as tokens with their corresponding details such as
     * type and location (row and column).
     *
     * @param input The raw input string to be tokenized. This string will be parsed for
     *              syntactical elements such as whitespace, comments, string literals,
     *              annotations, macro usages, and more.
     */
    public TokenStream(String input)
    {
        this.tokens = new LinkedList<>();
        tokens.add(Token.getStart()); // START token

        // Normalize line endings to \n to ensure consistent row counting
        input = input.replace("\r\n", "\n").replace("\r", "\n");

        // We'll accumulate lines that are *not* package/import in here,
        // but keep line numbering correct by inserting blank lines for
        // any line we skip (package/import).
        StringBuilder nonImportLines = new StringBuilder();

        // Process the input line by line
        String[] lines = input.split("\n", -1); // The -1 limit ensures trailing empty strings are included
        for (String line : lines)
        {
            String trimmed = line.trim();

            if (trimmed.startsWith("package "))
            {
                // Something like: package com.example.coolStuff;
                String afterPkg = trimmed.substring("package ".length()).trim();
                // Typically a package line ends with a semicolon in many languages,
                // if you want to allow that, you can strip it out:
                if (afterPkg.endsWith(";"))
                {
                    afterPkg = afterPkg.substring(0, afterPkg.length() - 1).trim();
                }
                this.packageName = afterPkg; // store it

                // Insert a blank line in place so row #s line up
                nonImportLines.append("\n");
            }
            else if (trimmed.startsWith("import "))
            {
                // Could be "import something.*;" or "import something;"
                parseImport(false, trimmed.substring("import ".length()).trim());
                nonImportLines.append("\n");
            }
            else if (trimmed.startsWith("static import "))
            {
                // Could be "static import something.member;"
                parseImport(true, trimmed.substring("static import ".length()).trim());
                nonImportLines.append("\n");
            }
            else
            {
                // Not a package/import line. Keep it for normal tokenization.
                nonImportLines.append(line).append('\n');
            }
        }

        // Now tokenize everything else, with line numbering intact.
        tokenize(nonImportLines.toString());
    }

    private void parseImport(boolean isStatic, String importLine)
    {
        // Typically, lines might end with a semicolon: e.g. import foo.bar.*;
        if (importLine.endsWith(";"))
        {
            importLine = importLine.substring(0, importLine.length() - 1).trim();
        }
        // If there's a trailing .*:
        if (importLine.endsWith(".*"))
        {
            String base = importLine.substring(0, importLine.length() - 2).trim();
            // star import
            imports.add(new ImportDeclaration(isStatic, base, "*"));
        }
        else
        {
            // Might be something like "toucan.util.Math.Constants"
            // or "toucan.util.Math.Constants.PI"
            // We'll see if there's a final dot segment
            int lastDot = importLine.lastIndexOf('.');
            if (lastDot < 0)
            {
                // no dot => entire thing is the base
                imports.add(new ImportDeclaration(isStatic, importLine, null));
            }
            else
            {
                // We can treat the portion after the last dot as "member" if user wants
                // In Toucan we can do: static import foo.Bar.BAZ
                String base = importLine.substring(0, lastDot);
                String member = importLine.substring(lastDot + 1);
                // If the 'member' part starts with uppercase and your language always puts
                // uppercase for classes, etc., you can interpret that differently. For now, let's store it as-is.
                imports.add(new ImportDeclaration(isStatic, base, member));
            }
        }
    }

    /**
     * Actually does the tokenization on the leftover lines
     * (plus blank lines where we skipped package/import).
     */
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
                // Single-line
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
                // Multi-line
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

            // Handle multi-line strings (""" ... """)
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

            // Handle regular strings
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
                            // Unclosed string
                            tokens.add(Token.stored(startRow, startColumn, INVALID,
                                    input.substring(startIndex, index)));
                            row++;
                            column = 0;
                            continue loop;
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
                String charText = input.substring(startIndex, index);
                tokens.add(Token.stored(startRow, startColumn, CHAR_LITERAL, charText));
                continue;
            }

            // Handle annotation usage: @Getter
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
                    // Stop if non-alphanumeric and not underscore
                    if (!Character.isLetterOrDigit(nc) && nc != '_')
                    {
                        break;
                    }
                    annotationName.append(nc);
                    index++;
                    column++;
                }

                String combined = "@" + annotationName;
                tokens.add(Token.stored(startRow, startColumn, ANNOTATION_USE, combined));
                continue;
            }

            // --- Handle macro usage: e.g. sum!(...) ---
            {
                int lookAheadIndex = index;
                Matcher macroCheck = Pattern.compile(IDENTIFIER.regex).matcher(input.substring(lookAheadIndex));
                if (macroCheck.lookingAt())
                {
                    String possibleMacroName = macroCheck.group();
                    int nameLen = possibleMacroName.length();
                    // Check if next char is '!'
                    if (lookAheadIndex + nameLen < input.length()
                            && input.charAt(lookAheadIndex + nameLen) == '!')
                    {
                        // Then check if after '!' there's '(', '[', or '{'
                        char nextSym = (lookAheadIndex + nameLen + 1 < input.length())
                                ? input.charAt(lookAheadIndex + nameLen + 1)
                                : '\0';

                        if (nextSym == '(' || nextSym == '[' || nextSym == '{')
                        {
                            // We found a macro usage
                            int startRow = row;
                            int startColumn = column;

                            // Move the real index forward by nameLen + 1 (for '!')
                            index += nameLen + 1;
                            column += nameLen + 1;

                            // Build the macro content
                            StringBuilder macroContent = new StringBuilder(possibleMacroName + "!");
                            // Parse the bracketed content with nesting
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
                                    if (!stack.isEmpty())
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
                                            // Mismatched bracket - treat as error or keep going
                                            stack.pop();
                                        }
                                    }
                                }
                            }
                            // Entire macro usage
                            tokens.add(Token.stored(startRow, startColumn, MACRO_USE, macroContent.toString()));
                            continue;
                        }
                    }
                }
            }

            // Handle multi-character operators (like '<<', '>>', '&&', '||', etc.)
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
            if (matchedOperator) continue;

            // Check for macro variables: starts with '$'
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

            // Handle identifiers and keywords
            Matcher idMatcher = Pattern.compile(IDENTIFIER.regex)
                    .matcher(input.substring(index));
            if (idMatcher.lookingAt())
            {
                String word = idMatcher.group();
                int startColumn = column;

                // Check for special macro keywords
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
                    // It's a normal identifier
                    tokens.add(Token.stored(row, startColumn, IDENTIFIER, word));
                }

                index += word.length();
                column += word.length();
                continue;
            }

            // Handle single-character operators/punctuations
            String ch = String.valueOf(input.charAt(index));
            if (singleCharTokenMap.containsKey(ch))
            {
                int startColumn = column;
                tokens.add(Token.basic(row, startColumn, singleCharTokenMap.get(ch)));
                index++;
                column++;
                continue;
            }

            // If none matched, it's invalid
            int startColumn = column;
            tokens.add(Token.stored(row, startColumn, INVALID, String.valueOf(input.charAt(index))));
            index++;
            column++;
        }

        tokens.add(Token.getEnd()); // END token
    }

    // For debugging
    public void printTokens()
    {
        System.out.println("Package: " + packageName);
        System.out.println("Imports:");
        for (ImportDeclaration imp : imports)
        {
            System.out.println("  - " + imp);
        }
        System.out.println("\nTokens:");
        for (Token token : tokens)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Type: ").append(token.type);
            token.internal.ifPresent(s -> sb.append(", Content: ").append(s));
            sb.append(", Row: ").append(token.coordinates.row());
            sb.append(", Column: ").append(token.coordinates.column());
            System.out.println(sb);
        }
    }

    public String getTokensAsString()
    {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens)
        {
            sb.append("Type: ").append(token.type);
            token.internal.ifPresent(content -> sb.append(", Content: ").append(content));
            sb.append(", Row: ").append(token.coordinates.row());
            sb.append(", Column: ").append(token.coordinates.column());
            sb.append("\n");
        }
        return sb.toString();
    }
}
