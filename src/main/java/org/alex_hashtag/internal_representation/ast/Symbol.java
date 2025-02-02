package org.alex_hashtag.internal_representation.ast;

public enum Symbol
{
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    BITWISE_XOR("^"),
    BITWISE_NOT("~"),
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    LOGICAL_AND("and"),
    LOGICAL_OR("or"),
    LOGICAL_NOT("!"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    EQUAL_TO("=="),
    NOT_EQUAL_TO("!="),
    ASSIGN("="),
    INCREMENT("++"),
    DECREMENT("--"),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    BITWISE_AND_ASSIGN("&="),
    BITWISE_OR_ASSIGN("|="),
    BITWISE_XOR_ASSIGN("^="),
    BIT_SHIFT_LEFT_ASSIGN("<<="),
    BIT_SHIFT_RIGHT_ASSIGN(">>="),
    BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN(">>>="),
    BRACKET_OPEN("("),
    BRACKET_CLOSE(")"),
    CURLY_OPEN("{"),
    CURLY_CLOSE("}"),
    SQUARE_OPEN("["),
    SQUARE_CLOSE("]"),
    COMMA(","),
    SEMICOLON(";");

    private final String symbol;

    Symbol(String c)
    {
        this.symbol = c;
    }

    public String getSymbol()
    {
        return symbol;
    }
}
