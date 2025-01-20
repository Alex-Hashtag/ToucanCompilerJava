package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.expression.operators.ArithmeticOperator;
import org.alex_hashtag.internal_representation.types.Type;

public class ArithmeticExpression implements Expression
{
    Type type;
    Expression left;
    Expression right;
    ArithmeticOperator operator;

    @Override
    public Type getType()
    {
        return type;
    }
}
