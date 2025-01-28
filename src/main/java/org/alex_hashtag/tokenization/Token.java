package org.alex_hashtag.tokenization;

import java.util.Optional;

import static org.alex_hashtag.tokenization.TokenType.*;


public class Token
{
    public Coordinates coordinates;
    public TokenType type;
    public Optional<String> internal;

    private Token(Coordinates coordinates, TokenType type)
    {
        this.coordinates = coordinates;
        this.type = type;
        this.internal = Optional.empty();
    }

    private Token(Coordinates coordinates, TokenType type, String internal)
    {
        this.coordinates = coordinates;
        this.type = type;
        this.internal = Optional.ofNullable(internal);
    }

    public static Token getStart()
    {
        return new Token(new Coordinates(0, 0), START);
    }

    public static Token getEnd()
    {
        return new Token(new Coordinates(0, 0), END);
    }

    public static Token basic(int row, int column, TokenType type)
    {
        return new Token(new Coordinates(row, column), type);
    }

    public static Token stored(int row, int column, TokenType type, String internal)
    {
        return new Token(new Coordinates(row, column), type, internal);
    }

    public static Token from(String sequence, int row, int column)
    {
        for (TokenType type : TokenType.values())
            if (!type.regex.isEmpty() && sequence.matches(type.regex))
            {
                if (type.isStored())
                    return new Token(new Coordinates(row, column), type, sequence);
                else
                    return new Token(new Coordinates(row, column), type);
            }

        return new Token(new Coordinates(row, column), INVALID, sequence);
    }

    public String describeContents()
    {
        return switch (type)
        {
            case IDENTIFIER -> "identifier '" + internal.orElse("") + "'";
            case INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, RUNE_LITERAL, STRING_LITERAL ->
                    type.name().toLowerCase().replace("_literal", "") + " literal '" + internal.orElse("") + "'";
            case COMMENT -> "comment";
            case SEMI_COLON, COLON, DOT, COMMA, BRACE_OPEN, BRACE_CLOSED, BRACKET_OPEN, BRACKET_CLOSED, CURLY_OPEN,
                 CURLY_CLOSED,
                 ARROW_OPEN, ARROW_CLOSED, ASSIGNMENT, ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO,
                 LOGICAL_AND, LOGICAL_OR,
                 LOGICAL_NOT, EQUALITY, INEQUALITY, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,
                 BIT_SHIFT_LEFT,
                 BIT_SHIFT_RIGHT, BIT_SHIFT_RIGHT_UNSIGNED, BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT,
                 BIT_SHIFT_LEFT_EQUALS,
                 BIT_SHIFT_RIGHT_EQUALS, BIT_SHIFT_RIGHT_UNSIGNED_EQUALS, ADDITION_ASSIGN, SUBTRACTION_ASSIGN,
                 MULTIPLICATION_ASSIGN,
                 DIVISION_ASSIGN, MODULO_ASSIGN, BITWISE_AND_ASSIGN, BITWISE_OR_ASSIGN, BITWISE_XOR_ASSIGN, INCREMENT,
                 DECREMENT, DOUBLE_COLON -> "operator '" + type.name().toLowerCase().replace("_", " ") + "'";
            case TRUE, FALSE -> "boolean literal '" + type.name().toLowerCase() + "'";
            case VAR, IF, ELSE, WHILE, DO, FOR, LOOP, SWITCH, CONTINUE, BREAK, YIELD, RETURN, INLINE, ECHO, STRUCT,
                 TYPEDEF, ENUM, TRAIT,
                 IMPLEMENT, TEMPLATE, OPERATIONS, CLASS, CONSTRUCTOR, IMPLICIT, EXTENDS, IMPLEMENTS, THIS, SUPER,
                 ABSTRACT, NULL, PUBLIC,
                 PRIVATE, PROTECTED, SYS, TYPEOF, SIZEOF, UNSAFE, PACKAGE, IMPORT, MACRO, ANNOTATION ->
                    "keyword '" + type.name().toLowerCase() + "'";
            default -> "special character or unknown token";
        };
    }

    @Override
    public String toString()
    {
        return switch (type)
        {
            case IDENTIFIER -> "identifier '" + internal.orElse("") + "'";
            case INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, RUNE_LITERAL, STRING_LITERAL -> internal.orElse("") + "'";
            case SEMI_COLON, COLON, DOT, COMMA, BRACE_OPEN, BRACE_CLOSED, BRACKET_OPEN, BRACKET_CLOSED, CURLY_OPEN,
                 CURLY_CLOSED,
                 ARROW_OPEN, ARROW_CLOSED, ASSIGNMENT, ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO,
                 LOGICAL_AND, LOGICAL_OR,
                 LOGICAL_NOT, EQUALITY, INEQUALITY, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,
                 BIT_SHIFT_LEFT,
                 BIT_SHIFT_RIGHT, BIT_SHIFT_RIGHT_UNSIGNED, BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT,
                 BIT_SHIFT_LEFT_EQUALS,
                 BIT_SHIFT_RIGHT_EQUALS, BIT_SHIFT_RIGHT_UNSIGNED_EQUALS, ADDITION_ASSIGN, SUBTRACTION_ASSIGN,
                 MULTIPLICATION_ASSIGN,
                 DIVISION_ASSIGN, MODULO_ASSIGN, BITWISE_AND_ASSIGN, BITWISE_OR_ASSIGN, BITWISE_XOR_ASSIGN, INCREMENT,
                 DECREMENT, DOUBLE_COLON -> " " + type.name().toLowerCase().replace("_", " ") + " ";
            case TRUE, FALSE, VAR, IF, ELSE, WHILE, DO, FOR, LOOP, SWITCH, CONTINUE, BREAK, YIELD, RETURN, INLINE, ECHO,
                 STRUCT, TYPEDEF, ENUM, TRAIT, IMPLEMENT, TEMPLATE, OPERATIONS, CLASS, CONSTRUCTOR, IMPLICIT, EXTENDS,
                 IMPLEMENTS, THIS, SUPER, ABSTRACT, NULL, PUBLIC, PRIVATE, PROTECTED, SYS, TYPEOF, SIZEOF, UNSAFE,
                 PACKAGE, IMPORT, MACRO, ANNOTATION -> type.name().toLowerCase();
            default -> internal.orElse("");
        };
    }
}
