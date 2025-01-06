package org.alex_hashtag.tokenization;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.alex_hashtag.tokenization.TokenType.*;

public class TokenStream
{
    // Multi-char operator patterns remain the same
    private static final Map<String, TokenType> multiCharOperatorMap = new LinkedHashMap<>();

    // Instead of storing single-character tokens by their "regex" string,
    // we now store the actual literal character.
    private static final Map<String, TokenType> singleCharTokenMap = new HashMap<>();

    private static final Set<String> keywordsSet = new HashSet<>();
    private static final Map<String, TokenType> keywordMap = new HashMap<>();

    static
    {
        // Multi-character operators
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
        // SINGLE-CHAR LITERALS:
        // The string keys here are actual characters we expect in the source code.
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
        // Keywords and types
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

    public final LinkedList<Token> tokens;

    public TokenStream(String input)
    {
        this.tokens = new LinkedList<>();
        tokens.add(Token.getStart()); // START token

        int index = 0;
        int row = 0;
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

            // --- Simplified: Handle annotation usage: @Getter
            //     We do NOT parse any parentheses. We just store @ + annotationName.
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
                    if (lookAheadIndex + nameLen < input.length() &&
                            input.charAt(lookAheadIndex + nameLen) == '!')
                    {
                        // Then check if after '!' there's '(', '[', or '{'
                        char nextSym = (lookAheadIndex + nameLen + 1 < input.length())
                                ? input.charAt(lookAheadIndex + nameLen + 1) : '\0';

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
                                        if ((open == '(' && c == ')') ||
                                                (open == '[' && c == ']') ||
                                                (open == '{' && c == '}'))
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
                            // We have the entire macro usage
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

            // Try to match float literal
            Matcher floatMatcher = Pattern.compile(FLOAT_LITERAL.regex)
                    .matcher(input.substring(index));
            if (floatMatcher.lookingAt())
            {
                int startColumn = column;
                String floatLiteral = floatMatcher.group();
                tokens.add(Token.stored(row, startColumn, FLOAT_LITERAL, floatLiteral));
                index += floatLiteral.length();
                column += floatLiteral.length();
                continue;
            }

            // Try to match int literal
            Matcher intMatcher = Pattern.compile(INT_LITERAL.regex)
                    .matcher(input.substring(index));
            if (intMatcher.lookingAt())
            {
                int startColumn = column;
                String intLiteral = intMatcher.group();
                tokens.add(Token.stored(row, startColumn, INT_LITERAL, intLiteral));
                index += intLiteral.length();
                column += intLiteral.length();
                continue;
            }

            // Handle identifiers and keywords (incl. macro keywords expression/identifier)
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

    public void printTokens()
    {
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
}
