package org.alex_hashtag.internal_representation.expression.operators;

public enum BooleanOperator
{
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

    BooleanOperator(String c)
    {
        this.operator = c;
    }

    public String getOperator()
    {
        return operator;
    }
}
