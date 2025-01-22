package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.operators.BitwiseOperator;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class BitwiseExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    Expression left;
    Expression right;
    BitwiseOperator operator;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}

