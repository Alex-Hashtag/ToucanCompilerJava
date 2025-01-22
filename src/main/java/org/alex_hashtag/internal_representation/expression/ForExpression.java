package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class ForExpression implements Expression
{
    @Getter
    Coordinates location;
    List<Expression> initialisations;
    BooleanExpression condition;
    List<Expression> updates;
    List<Expression> statements;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }
}
