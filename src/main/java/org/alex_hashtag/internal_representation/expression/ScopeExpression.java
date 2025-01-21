package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.List;
import java.util.Optional;


public class ScopeExpression implements Expression
{
    String type;
    List<Expression> expressions;


    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}