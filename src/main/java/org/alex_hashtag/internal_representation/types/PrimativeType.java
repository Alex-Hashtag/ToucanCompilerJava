package org.alex_hashtag.internal_representation.types;

import java.util.List;

public class PrimativeType implements Type
{
    String name;

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public List<TraitType> getTraits()
    {
        return List.of();
    }

    public PrimativeType(String name)
    {
        this.name = name;
    }
}
