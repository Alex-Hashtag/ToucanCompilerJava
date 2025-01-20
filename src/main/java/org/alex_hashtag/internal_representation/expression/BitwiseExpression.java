package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.expression.operators.BitwiseOperator;
import org.alex_hashtag.internal_representation.types.Type;

public class BitwiseExpression implements Expression
{
    Type type;
    Expression left;
    Expression right;
    BitwiseOperator operator;

    @Override
    public Type getType()
    {
        return null;
    }
}
