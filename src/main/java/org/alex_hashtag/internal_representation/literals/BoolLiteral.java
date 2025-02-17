package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class BoolLiteral implements Literal
{
    @Getter
    CoordinatesOLD location;
    boolean value;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("bool");
    }
}
