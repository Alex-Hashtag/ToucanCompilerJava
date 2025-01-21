package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.List;
import java.util.Optional;


public class SwitchExpression implements Expression
{

    String type;
    Expression compaterTo;
    List<Arm> arms;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    public static class Arm
    {
        Expression pattern;
        Expression expression;
    }
}
