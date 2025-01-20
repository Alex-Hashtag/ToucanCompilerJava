package org.alex_hashtag.internal_representation.expression.operators;

public enum ArithmeticOperator
{
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    ;

    private final String operator;

    ArithmeticOperator(String c)
    {
        this.operator = c;
    }

    public String getOperator()
    {
        return operator;
    }
}
