package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;

public class FloatLiteral implements Literal
{
    byte sizeInBytes;
    double internal;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("float" + sizeInBytes * 8);
    }
}
