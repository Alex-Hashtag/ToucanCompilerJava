package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import lombok.Setter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class IfExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    String type;
    BinaryExpression condition;
    List<Expression> statements;
    IfExpression elseExpression;

    boolean brackets; // For toString() purposes, doesn't actually affect the behavior of the class
    @Setter
    boolean isElse; // For toString() purposes, doesn't actually affect the behavior of the class

    public IfExpression(CoordinatesOLD location, BinaryExpression binaryExpression, List<Expression> statements, IfExpression elseExpr, boolean hasBlock, boolean b)
    {
        this.location = location;
        this.condition = binaryExpression;
        this.statements = statements;
        this.elseExpression = elseExpr;
        this.brackets = hasBlock;
        this.isElse = b;
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

        if (isElse)
            sb.append("else if (");
        else
            sb.append("if (");

        sb.append(condition);

        sb.append(") ");

        if (brackets)
            sb.append("{\n");
        for (Expression statement : statements)
        {
            sb.append(statement.toString());
            sb.append(";"); // Add a semicolon to the end of each statement
        }
        if (brackets)
            sb.append("\n}");

        if (elseExpression != null)
            sb.append("\n").append(elseExpression);

        return sb.toString();
    }
}
