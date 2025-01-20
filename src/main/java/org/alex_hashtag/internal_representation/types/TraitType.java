package org.alex_hashtag.internal_representation.types;

import java.util.List;

public class TraitType implements Type
{
    String name;
    List<TraitType> traits;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public List<TraitType> getTraits()
    {
        return traits;
    }
}
