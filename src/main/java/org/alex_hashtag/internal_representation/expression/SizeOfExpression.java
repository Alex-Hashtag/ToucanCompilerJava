package org.alex_hashtag.internal_representation.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;

@AllArgsConstructor
public class SizeOfExpression implements Expression
{
    @Getter
    Coordinates location;
    Expression type;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("usize");
    }

    @Override
    public String toString()
    {
        return "sizeof " + type;
    }
}
