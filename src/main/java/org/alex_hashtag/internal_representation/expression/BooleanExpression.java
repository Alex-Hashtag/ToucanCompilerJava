package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.operators.BooleanOperator;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class BooleanExpression implements Expression
{
    @Getter
    Coordinates location;
    Expression left;
    Expression right;
    BooleanOperator operator;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("bool");
    }
}

