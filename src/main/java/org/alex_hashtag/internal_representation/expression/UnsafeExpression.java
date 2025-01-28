package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class UnsafeExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    List<Expression> expressions;

    boolean brackets; // For toString() purposes, doesn't actually affect the behavior of the class


    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("unsafe");

        if (brackets)
            sb.append("\n{");
        else
            sb.append(" ");

        for (int i = 0; i < expressions.size(); i++)
        {
            sb.append(expressions.get(i));
            if (i < expressions.size() - 1)
                sb.append(", ");
        }

        if (brackets)
            sb.append("};\n");

        return sb.toString();
    }

}
