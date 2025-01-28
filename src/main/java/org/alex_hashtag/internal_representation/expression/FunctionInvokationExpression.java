package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class FunctionInvokationExpression implements Expression
{
    @Getter
    Coordinates location;
    String type;
    Expression identifier;
    List<Expression> arguments;

    boolean methodInvocation; // For toString() purposes, doesn't actually affect the behavior of the class

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (methodInvocation)
        {
            sb.append(arguments.getFirst());
            sb.append(".");
            sb.append(identifier);
            sb.append("(");
            arguments.stream().skip(1).forEach(arg -> sb.append(arg).append(", "));
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }
        else
        {
            sb.append(identifier);
            sb.append("(");
            arguments.forEach(arg -> sb.append(arg).append(", "));
            if (!arguments.isEmpty())
                sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }

        return sb.toString();
    }
}
