package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.List;
import java.util.Optional;


public class LoopOfExpression implements Expression
{
    Expression numberOfIterations;
    List<Expression> statements;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }
}