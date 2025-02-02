package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class LoopExpression implements Expression
{
    @Getter
    Coordinates location;
    List<Expression> statements;


    @Getter
    private boolean brackets;

    public LoopExpression(boolean brackets, Coordinates location, List<Expression> statements)
    {
        this.brackets = brackets;
        this.location = location;
        this.statements = statements;
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
        sb.append("loop");
        if (statements.size() > 0)
        {
            sb.append("\n{\n");
            for (Expression statement : statements)
            {
                sb.append(statement.toString());
                sb.append(";\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }
}
