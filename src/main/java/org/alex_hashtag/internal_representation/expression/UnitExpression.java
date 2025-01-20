package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;

public class UnitExpression implements Expression
{
    Type type;
    String literal;

    @Override
    public Type getType()
    {
        return type;
    }
}
