package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class LoopOfExpression implements Expression
{
    @Getter
    Coordinates location;
    Expression numberOfIterations;
    List<Expression> statements;


    private final boolean brackets;

    public LoopOfExpression(Expression numberOfIterations, List<Expression> statements, Coordinates start, boolean hasBrackets)
    {
        this.numberOfIterations = numberOfIterations;
        this.statements = statements;
        this.location = start;
        this.brackets = hasBrackets;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("loop (");
        sb.append(numberOfIterations);
        sb.append(")");
        sb.append("\n{\n");
        for (Expression statement : statements)
        {
            sb.append(statement.toString());
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
