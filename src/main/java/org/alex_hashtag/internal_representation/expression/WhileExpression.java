package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class WhileExpression implements Expression
{

    @Getter
    Coordinates location;
    BinaryExpression condition;
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

        sb.append("while (");
        sb.append(condition);
        sb.append(")");

        if (brackets)
            sb.append("{\n");
        for (Expression statement : statements)
        {
            sb.append(statement.toString());
            sb.append(";"); // Add a semicolon to the end of each statement
        }
        if (brackets)
            sb.append("\n}");

        return sb.toString();
    }
}
