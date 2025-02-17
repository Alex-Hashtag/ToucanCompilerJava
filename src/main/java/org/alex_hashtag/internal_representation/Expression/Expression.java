package org.alex_hashtag.internal_representation.Expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeHolder;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.lib.results.Result;
import org.alex_hashtag.lib.tokenization.Coordinates;
import org.alex_hashtag.lib.tokenization.Token;


public interface Expression
{
    record Empty(Coordinates coordinates) implements Expression {}
    record Binary(Coordinates coordinates, Expression left, Expression right, Token.Operator operator) implements Expression {}


    //! PROPER RETURN TYPE EVALUATION WILL BE SAVED FOR SEMATIC ANALYSIS PHASE
    default TypeHolder getType()
    {
        return switch (this)
        {
            case Empty(Coordinates coordinates) -> new TypeHolder.Resolved(coordinates, "void");
            case Binary(Coordinates coordinates, Expression left, Expression right, Token.Operator operator) -> {
                TypeHolder leftType = left.getType();
                TypeHolder rightType = right.getType();
                if (!(leftType instanceof TypeHolder.Resolved))
                    yield  leftType;
                if (!(rightType instanceof TypeHolder.Resolved))
                    yield  rightType;

                if (((TypeHolder.Resolved) leftType).name() != ((TypeHolder.Resolved) rightType).name())
                    yield new TypeHolder.IncompatibleTypesInBinaryExpression(coordinates, ((TypeHolder.Resolved) leftType).name(), ((TypeHolder.Resolved) rightType).name());

                throw new IllegalStateException("Unexpected value: " + this);
                
            }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}
