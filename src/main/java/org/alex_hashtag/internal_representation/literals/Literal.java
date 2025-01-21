package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.Optional;


public interface Literal
{
    Optional<Type> getType();
}
