package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.util.Locatable;

import java.util.Optional;


public interface Literal extends Locatable
{
    Optional<Type> getType();
}
