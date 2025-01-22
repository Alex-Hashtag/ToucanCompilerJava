package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;
import java.util.Optional;


public class TraitType implements Type, Generic
{
    @Getter
    Coordinates location;
    String name;
    List<String> traits;
    List<VariableDeclarationExpression> genericArguments;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Type getReferenced()
    {
        return new PrimativeType("&" + this.name);
    }

    @Override
    public Type getArray()
    {
        return new PrimativeType(this.name + "[]");
    }

    @Override
    public Type getReferencedArray()
    {
        return new PrimativeType("&" + this.name + "[]");
    }

    @Override
    public List<String> getTraits()
    {
        return traits;
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> genericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }
}
