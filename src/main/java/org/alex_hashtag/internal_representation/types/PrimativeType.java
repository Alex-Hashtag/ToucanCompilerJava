package org.alex_hashtag.internal_representation.types;

import java.util.List;

public class PrimativeType implements Type
{
    String name;
    List<TraitType> traits;

    @Override
    public String getName()
    {
        return this.name;
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
    public List<TraitType> getTraits()
    {
        return traits;
    }

    public PrimativeType(String name)
    {
        this.name = name;
    }
}
