package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


@Getter
public class EmptyExpression implements Expression
{
    CoordinatesOLD location;

    public EmptyExpression(CoordinatesOLD location)
    {
        this.location = location;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        return " ";
    }
}
