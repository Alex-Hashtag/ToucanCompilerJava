package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class ReturnExpression implements Expression
{
    @Getter
    Coordinates location;
    Expression returnThis;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        return "return " + returnThis;
    }
}
