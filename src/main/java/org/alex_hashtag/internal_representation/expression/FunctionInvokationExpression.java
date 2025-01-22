package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class FunctionInvokationExpression implements Expression
{
    @Getter
    Coordinates location;

    String type;
    Expression identifier;
    List<Expression> arguments;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
