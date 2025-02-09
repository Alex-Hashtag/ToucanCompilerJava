package org.alex_hashtag.internal_representation.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class AccessChainExpression implements Expression
{
    @Getter
    Coordinates location;
    @Getter
    List<Expression> segments;
    String type;

    public AccessChainExpression(Coordinates location, List<Expression> segments)
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
