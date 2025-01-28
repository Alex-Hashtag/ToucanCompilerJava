package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class ForExpression implements Expression
{
    @Getter
    Coordinates location;
    List<Expression> initialisations;
    Expression condition;
    List<Expression> updates;
    List<Expression> statements;

    boolean brackets; // For toString() purposes, doesn't actually affect the behavior of the class

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("for (");
        for (int i = 0; i < initialisations.size(); i++)
        {
            sb.append(initialisations.get(i));
            if (i < initialisations.size() - 1)
                sb.append(", ");
        }

        sb.append("; ");

        sb.append(condition);

        sb.append("; ");

        for (int i = 0; i < updates.size(); i++)
        {
            sb.append(updates.get(i));
            if (i < updates.size() - 1)
                sb.append(", ");
        }

        if (brackets)
            sb.append("{\n");
        for (Expression statement : statements)
        {
            sb.append(statement.toString());
            sb.append(";"); // Add a semicolon to the end of each statement
            sb.append("\n");
        }
        if (brackets)
            sb.append("}\n");
        return sb.toString();
    }
}
