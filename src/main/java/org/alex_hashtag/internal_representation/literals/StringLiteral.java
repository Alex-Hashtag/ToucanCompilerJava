package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;

public class StringLiteral implements Literal
{
    String value;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("string");
    }
}
