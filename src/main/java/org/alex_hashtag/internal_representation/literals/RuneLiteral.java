package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;

public class RuneLiteral implements Literal
{
    long value;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("rune");
    }
}
