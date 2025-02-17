package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class YieldExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    Expression yieldThis;

    public YieldExpression(CoordinatesOLD coordinates, Expression expression)
    {
        this.location = coordinates;
        this.yieldThis = expression;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        return "yield " + yieldThis;
    }
}
