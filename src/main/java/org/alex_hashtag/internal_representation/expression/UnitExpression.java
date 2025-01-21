package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.Optional;


public class UnitExpression implements Expression
{
    String type;
    String identifier;

    @Override
    public Optional<Type> getType()
    {
        return Optional.empty();
    }
}
