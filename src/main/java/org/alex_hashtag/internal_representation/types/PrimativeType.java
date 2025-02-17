package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;


public class PrimativeType implements Type
{
    @Getter
    CoordinatesOLD location;
    String name;
    List<String> traits;

    public PrimativeType(String name)
    {
        this.name = name;
    }

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
    public List<String> getTraits()
    {
        return traits;
    }
}
