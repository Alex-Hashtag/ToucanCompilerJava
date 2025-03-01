package org.alex_hashtag.internal_representation.expressionOld;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;

@AllArgsConstructor
public class TypeOfExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    Expression expr;


    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("type");
    }

    @Override
    public String toString()
    {
        return "typeof " + expr;
    }
}
