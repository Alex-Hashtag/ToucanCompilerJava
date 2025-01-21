package org.alex_hashtag.internal_representation.types;

import java.util.List;

public interface Type
{
    String getName();
    Type getReferenced();
    Type getArray();
    Type getReferencedArray();

    List<TraitType> getTraits();

}
