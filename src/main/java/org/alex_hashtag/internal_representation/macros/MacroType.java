package org.alex_hashtag.internal_representation.macros;


import lombok.Getter;
import org.alex_hashtag.internal_representation.types.PrimativeType;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;


/**
 * Type to be used in on the right side of each arm of the Macro in order to later be replaced by the proper type
 */
public class MacroType implements Type
{
    @Getter
    Coordinates location;
    String name;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Type getReferenced()
    {
        return new PrimativeType("&" + this.name);
    }

    @Override
    public Type getArray()
    {
        return new PrimativeType(this.name + "[]");
    }

    @Override
    public Type getReferencedArray()
    {
        return new PrimativeType("&" + this.name + "[]");
    }

    @Override
    public List<String> getTraits()
    {
        return List.of();
    }
}
