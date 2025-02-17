package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class AccessChainExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    @Getter
    List<Expression> segments;
    String type;

    public AccessChainExpression(CoordinatesOLD location, List<Expression> segments)
    {
        this.location = location;
        this.segments = segments;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
