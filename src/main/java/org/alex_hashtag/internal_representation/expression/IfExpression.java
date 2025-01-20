package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.List;

public class IfExpression implements Expression
{
    Type type;
    BooleanExpression condition;
    List<Expression> statements;

    @Override
    public Type getType()
    {
        return type;
    }
}
