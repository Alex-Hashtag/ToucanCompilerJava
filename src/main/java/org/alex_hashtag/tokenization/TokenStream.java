package org.alex_hashtag.tokenization;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.alex_hashtag.tokenization.TokenType.*;


public class TokenStream
{
    private static final Map<String, TokenType> multiCharOperatorMap = new LinkedHashMap<>();
    private static final Map<String, TokenType> singleCharTokenMap = new HashMap<>();
    private static final Set<String> keywordsSet = new HashSet<>();
    private static final Map<String, TokenType> keywordMap = new HashMap<>();

    static
    {
        // Multi-character operators
        for (TokenType type : List.of(BIT_SHIFT_LEFT_EQUALS, BIT_SHIFT_RIGHT_EQUALS, BIT_SHIFT_RIGHT_UNSIGNED_EQUALS, BIT_SHIFT_LEFT,
                BIT_SHIFT_RIGHT, BIT_SHIFT_RIGHT_UNSIGNED, EQUALITY, INEQUALITY, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, ADDITION_ASSIGN,
                SUBTRACTION_ASSIGN, MULTIPLICATION_ASSIGN, DIVISION_ASSIGN, MODULO_ASSIGN, BITWISE_AND_ASSIGN, BITWISE_OR_ASSIGN,
                BITWISE_XOR_ASSIGN, INCREMENT, DECREMENT, ARROW, DOUBLE_COLON, LOGICAL_AND, LOGICAL_OR))
            multiCharOperatorMap.put(type.regex, type);
    }

    static
    {
        // Single-character tokens
        for (TokenType type : List.of(SEMI_COLON, COLON, BRACE_OPEN, BRACE_CLOSED, BRACKET_OPEN, BRACKET_CLOSED, CURLY_OPEN, CURLY_CLOSED,
                LESS_THAN, GREATER_THAN, ASSIGNMENT, ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO, BITWISE_AND, BITWISE_OR,
                BITWISE_XOR, BITWISE_NOT, LOGICAL_NOT, DOT, COMMA))
            singleCharTokenMap.put(type.regex, type);
    }

    static
    {
        // Keywords and types
        String[] keywords = {
                "void", "int8", "int16", "int32", "int64", "int128",
                "uint8", "uint16", "uint32", "uint64", "uint128",
                "float16", "float32", "float64", "float80", "float128",
                "bool", "char", "rune", "string", "type", "var", "lambda",
                "mutable", "const", "static",
                "this", "constructor", "if", "else", "class", "namespace",
                "while", "do", "for", "loop", "switch", "null", "continue",
                "template", "yield", "struct", "implement", "implements",
                "public", "private", "protected", "implicit", "extends",
                "super", "abstract", "trait", "enum", "error",
                "sys", "typeof", "unsafe", "sizeof", "typedef", "operations",
                "import", "package", "true", "false", "return", "echo",
                "inline", "break", "macro", "annotation"
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

        //* Puts a START token to signify the chain start
        tokens.add(Token.getStart());

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

            // Handle multiline strings
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

            // Handle strings
            if (input.charAt(index) == '"')
            {
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index++;
                column++;
                while (index < input.length())
                {
                    char c = input.charAt(index);
                    if (c == '\\')
                    {
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
                            tokens.add(Token.stored(startRow, startColumn, INVALID, input.substring(startIndex, index)));
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
            if (input.charAt(index) == '\'')
            {
                int startRow = row;
                int startColumn = column;
                int startIndex = index;
                index++;
                column++;
                while (index < input.length())
                {
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
            if (matchedOperator) continue;

            // Handle number literals
            // Try to match float literal
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

            // Try to match int literal
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
            Matcher idMatcher = Pattern.compile(IDENTIFIER.regex).matcher(input.substring(index));
            if (idMatcher.lookingAt())
            {
                String word = idMatcher.group();
                int startColumn = column;
                if (keywordsSet.contains(word))
                {
                    // It's a keyword
                    tokens.add(Token.basic(row, startColumn, keywordMap.get(word)));
                }
                else
                {
                    // It's an identifier
                    tokens.add(Token.stored(row, startColumn, IDENTIFIER, word));
                }
                index += word.length();
                column += word.length();
                continue;
            }

            // Handle single-character operators and punctuations
            String ch = String.valueOf(input.charAt(index));
            if (singleCharTokenMap.containsKey(ch))
            {
                int startColumn = column;
                tokens.add(Token.basic(row, startColumn, singleCharTokenMap.get(ch)));
                index++;
                column++;
                continue;
            }

            // If none matched, create INVALID token
            int startColumn = column;
            tokens.add(Token.stored(row, startColumn, INVALID, String.valueOf(input.charAt(index))));
            index++;
            column++;
        }

        //* Puts an END token to signify the chain end
        tokens.add(Token.getEnd());
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
