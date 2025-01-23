package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class TypeOfExpression implements Expression
{
    @Getter
    Coordinates location;
    Expression expr;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("type");
    }
}
