package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.util.Locatable;

import java.util.Optional;


public interface Expression
{
    public Optional<Type> getType();
}
