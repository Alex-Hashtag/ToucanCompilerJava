package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class ScopeExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    List<Expression> expressions;


    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Expression expression : expressions)
        {
            sb.append(expression.toString());
            sb.append(";\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
