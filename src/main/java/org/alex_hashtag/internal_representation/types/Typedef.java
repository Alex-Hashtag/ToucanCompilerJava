package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.*;


public class Typedef implements Type, Generic, Annotatable
{
    @Getter
    Coordinates location;
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    @Setter
    String name;
    List<String> traits;
    String innerType;
    boolean isOutsideCastable;
    List<Function> methods;

    @Override
    public Map<String, String> properties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> defaults()
    {
        return Map.of("visibility", List.of("public", "private"));
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> genericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    public String getName()
    {
        return this.name;
    }

    @SneakyThrows
    @Override
    public Type getReferenced()
    {
        Typedef newTypedef = this.clone();
        newTypedef.setName("&" + newTypedef.name);
        return newTypedef;
    }

    @SneakyThrows
    @Override
    public Type getArray()
    {
        Typedef newTypedef = this.clone();
        newTypedef.setName(newTypedef.name + "[]");
        return newTypedef;
    }

    @SneakyThrows
    @Override
    public Type getReferencedArray()
    {
        Typedef newTypedef = this.clone();
        newTypedef.setName("&" + newTypedef.name + "[]");
        return newTypedef;
    }

    @Override
    public List<String> getTraits()
    {
        return traits;
    }

    @Override
    protected Typedef clone() throws CloneNotSupportedException
    {
        return (Typedef) super.clone();
    }
}
