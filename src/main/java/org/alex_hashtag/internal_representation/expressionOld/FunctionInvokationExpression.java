package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class FunctionInvokationExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    String type;
    Expression identifier;
    AccessChainExpression arguments;

    boolean methodInvocation; // For toString() purposes, doesn't actually affect the behavior of the class

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    public FunctionInvokationExpression(Expression identifier, AccessChainExpression arguments, CoordinatesOLD location, boolean methodInvocation)
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (methodInvocation)
        {
            sb.append(arguments.getSegments().getFirst());
            sb.append(".");
            sb.append(identifier);
            sb.append("(");
            arguments.getSegments().stream().skip(1).forEach(arg -> sb.append(arg).append(", "));
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }
        else
        {
            sb.append(identifier);
            sb.append("(");
            arguments.getSegments().forEach(arg -> sb.append(arg).append(", "));
            if (!arguments.getSegments().isEmpty())
                sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }

        return sb.toString();
    }
}
