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

    boolean arrayAccess;

    public FieldAccessExpression(String fieldName, Expression identifier, Coordinates location, boolean arrayAccess)
    {
        this.fieldName = fieldName;
        this.identifier = identifier;
        this.location = location;
        this.arrayAccess = arrayAccess;
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
