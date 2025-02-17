package org.alex_hashtag.internal_representation.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;

@AllArgsConstructor
public class SizeOfExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
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
