package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class ArgsListExpression implements Expression
{
    @Getter
    private Coordinates location;
    private String type;
    @Getter
    private List<Expression> args;

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName(type);
    }
}
