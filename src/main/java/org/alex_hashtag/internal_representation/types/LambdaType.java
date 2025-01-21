package org.alex_hashtag.internal_representation.types;

import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;

import java.util.List;
import java.util.Optional;


public class LambdaType implements Type, Generic
{
    String name = "lambda<type[]>";
    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public Type getReferenced()
    {
        return null;
    }

    @Override
    public Type getArray()
    {
        return null;
    }

    @Override
    public Type getReferencedArray()
    {
        return null;
    }

    @Override
    public List<TraitType> getTraits()
    {
        return List.of();
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> genericArguments()
    {
        return Optional.empty();
    }
}
