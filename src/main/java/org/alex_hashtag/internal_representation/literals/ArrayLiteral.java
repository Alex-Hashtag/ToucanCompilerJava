package org.alex_hashtag.internal_representation.literals;

import jdk.jfr.Unsigned;
import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class ArrayLiteral implements Literal
{
    @Getter
    CoordinatesOLD location;
    String type;
    @Unsigned
    long size;
    List<Expression> entries;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
