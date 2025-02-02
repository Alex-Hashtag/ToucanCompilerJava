package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class FieldAccessExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    Expression identifier;
    String fieldName;

    public FieldAccessExpression(String fieldName, Expression identifier, Coordinates location)
    {
        this.fieldName = fieldName;
        this.identifier = identifier;
        this.location = location;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        return identifier + "." + fieldName;
    }
}
