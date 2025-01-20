package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.expression.operators.BooleanOperator;
import org.alex_hashtag.internal_representation.types.Type;

public class BooleanExpression implements Expression
{
    Type type; // ! Make boolean always
    Expression left;
    Expression right;
    BooleanOperator operator;

    @Override
    public Type getType()
    {
        return null;
    }
}
