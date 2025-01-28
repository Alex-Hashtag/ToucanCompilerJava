package org.alex_hashtag.internal_representation.expression;

public enum Operator
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
    NOT_EQUAL_TO("!=");

    private final String operator;

    Operator(String c)
    {
        this.operator = c;
    }

    public String getOperator()
    {
        return operator;
    }
}
