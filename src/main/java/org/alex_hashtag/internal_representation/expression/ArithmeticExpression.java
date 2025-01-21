package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.expression.operators.ArithmeticOperator;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;


public class ArithmeticExpression implements Expression
{
    String type;
    Expression left;
    Expression right;
    ArithmeticOperator operator;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
