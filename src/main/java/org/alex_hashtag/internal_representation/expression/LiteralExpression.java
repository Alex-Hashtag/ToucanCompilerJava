package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.literals.Literal;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;


public class LiteralExpression implements Expression
{
    String type;
    Literal literal;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}