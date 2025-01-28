package org.alex_hashtag.internal_representation.types;

import org.alex_hashtag.internal_representation.utils.Locatable;

import java.util.List;


public interface Type extends Locatable
{
    String getName();

    Type getReferenced();

    Type getArray();

    Type getReferencedArray();

    List<String> getTraits();

}
