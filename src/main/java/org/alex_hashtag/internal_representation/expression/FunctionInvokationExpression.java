package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.List;
import java.util.Optional;


public class FunctionInvokationExpression implements Expression
{

    String type;
    List<Expression> arguments;

    @Override
    public Optional<Type> getType()
    {
        return Optional.empty();
    }
}
