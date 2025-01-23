package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class FloatLiteral implements Literal
{
    @Getter
    Coordinates location;
    byte sizeInBytes;
    double internal;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("float" + sizeInBytes * 8);
    }
}
