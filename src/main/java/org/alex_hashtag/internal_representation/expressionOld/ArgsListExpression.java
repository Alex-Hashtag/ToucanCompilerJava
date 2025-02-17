package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class ArgsListExpression implements Expression
{
    @Getter
    private CoordinatesOLD location;
    private String type;
    @Getter
    private List<Expression> args;

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName(type);
    }
}
