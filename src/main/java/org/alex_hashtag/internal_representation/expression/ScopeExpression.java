package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.List;

public class ScopeExpression implements Expression
{
    Type type;
    List<Expression> expressions;


    @Override
    public Type getType()
    {
        return type;
    }
}
