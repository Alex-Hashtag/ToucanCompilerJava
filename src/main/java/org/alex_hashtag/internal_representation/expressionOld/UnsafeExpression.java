package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class UnsafeExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    String type;
    List<Expression> statements;

    boolean brackets; // For toString() purposes, doesn't actually affect the behavior of the class

    public UnsafeExpression(List<Expression> statements, CoordinatesOLD start, boolean brackets)
    {
        this.location = start;
        this.statements = statements;
        this.brackets = brackets;
    }


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

        for (int i = 0; i < statements.size(); i++)
        {
            sb.append(statements.get(i));
            if (i < statements.size() - 1)
                sb.append(", ");
        }

        if (brackets)
            sb.append("};\n");

        return sb.toString();
    }

}
