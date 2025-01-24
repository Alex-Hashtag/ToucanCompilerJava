package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class MacroRepetitionExpression implements Expression
{
    @Getter
    Coordinates location;
    List<Expression> expressions;
    // -1 = 0 or 1
    // 0 = 0 or more
    // 1 = 1 or more
    byte numberOfRepetitions;


    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }
}
