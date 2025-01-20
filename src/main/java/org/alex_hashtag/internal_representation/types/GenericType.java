package org.alex_hashtag.internal_representation.types;

import java.util.List;

public class GenericType implements Type
{
    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public List<TraitType> getTraits()
    {
        return List.of();
    }
}
