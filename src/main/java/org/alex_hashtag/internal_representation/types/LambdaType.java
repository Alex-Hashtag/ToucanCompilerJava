package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class LambdaType implements Type, Generic
{
    @Getter
    Coordinates location;
    static String st_name = "lambda<type[]>";
    String name;
    static List<String> traits;
    static List<VariableDeclarationExpression> genericArguments = List.of(new VariableDeclarationExpression("T", Mutability.CONST, "type[]"));

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Type getReferenced()
    {
        return new LambdaType("&" + name);
    }

    @Override
    public Type getArray()
    {
        return new LambdaType(name + "[]");
    }

    @Override
    public Type getReferencedArray()
    {
        return new LambdaType("&" + name + "[]");
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


    public LambdaType(String name)
    {
        this.name = name;
    }
}
