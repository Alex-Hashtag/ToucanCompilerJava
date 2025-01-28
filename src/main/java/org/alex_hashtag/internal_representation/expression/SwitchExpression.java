package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class SwitchExpression implements Expression
{

    @Getter
    Coordinates location;
    String type;
    Expression compaterTo;
    List<Arm> arms;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("switch (");
        sb.append(compaterTo);
        sb.append(")\n{\n");
        for (Arm arm : arms)
        {
            sb.append(arm);
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static class Arm
    {
        Expression pattern;
        Expression expression;

        @Override
        public String toString()
        {
            return "case " + pattern + " -> " + expression;
        }
    }
}
