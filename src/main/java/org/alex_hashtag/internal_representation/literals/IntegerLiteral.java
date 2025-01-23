package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class IntegerLiteral implements Literal
{
    @Getter
    Coordinates location;
    byte sizeInBytes;
    boolean unsigned;
    long internal;

    @Override
    public Optional<Type> getType()
    {
        StringBuilder type = new StringBuilder("int");
        if (unsigned) type.insert(0, "u");
        type.append(sizeInBytes * 8);
        return TypeRegistry.searchByName(type.toString());
    }
}
