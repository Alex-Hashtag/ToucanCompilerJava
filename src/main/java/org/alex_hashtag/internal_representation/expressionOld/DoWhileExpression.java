package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.internal_representation.utils.StringUtils;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;


public class DoWhileExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    List<Expression> statements;
    Expression condition;


    boolean brackets; // For toString() purposes, doesn't actually affect the behavior of the class

    public DoWhileExpression(Expression condition, List<Expression> statements, CoordinatesOLD location, boolean brackets)
    {
        this.brackets = brackets;
        this.condition = condition;
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
        StringUtils.appendIndented(sb, 0, "do\n");
        if (brackets)
            StringUtils.appendIndented(sb, 0, "{\n");
        for (Expression statement : statements)
        {
            StringUtils.appendIndented(sb, 1, statement.toString() + ";\n");
        }
        if (brackets)
            StringUtils.appendIndented(sb, 0, "}\n");
        StringUtils.appendIndented(sb, 0, "while (");
        sb.append(condition.toString());
        sb.append(");\n");
        return sb.toString();
    }
}
