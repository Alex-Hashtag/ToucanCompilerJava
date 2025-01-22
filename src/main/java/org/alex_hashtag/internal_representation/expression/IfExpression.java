package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class IfExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    BooleanExpression condition;
    List<Expression> statements;
    IfExpression elseExpression;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
