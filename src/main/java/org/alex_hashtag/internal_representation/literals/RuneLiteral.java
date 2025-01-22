package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;

public class RuneLiteral implements Literal
{
    @Getter
    Coordinates location;
    long value;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("rune");
    }
}
