package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.internal_representation.utils.StringUtils;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class DoWhileExpression implements Expression
{
    @Getter
    Coordinates location;
    List<Expression> statements;
    BinaryExpression condition;


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
