package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.literals.Literal;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class LiteralExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    String type;
    Literal literal;

    public LiteralExpression(CoordinatesOLD location, Literal literal)
    {
        this.location = location;
        this.literal = literal;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        return literal.toString();
    }
}
