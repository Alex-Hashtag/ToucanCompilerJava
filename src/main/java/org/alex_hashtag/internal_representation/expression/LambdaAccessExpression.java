package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class LambdaAccessExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    String type;
    Expression typeExpression;
    String functionName;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        return typeExpression + "::" + functionName;
    }
}
