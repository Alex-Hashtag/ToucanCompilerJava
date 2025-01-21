package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.expression.operators.BooleanOperator;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;


public class BooleanExpression implements Expression
{
    Expression left;
    Expression right;
    BooleanOperator operator;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("bool");
    }
}
