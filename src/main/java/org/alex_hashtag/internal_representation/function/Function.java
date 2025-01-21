package org.alex_hashtag.internal_representation.function;

import org.alex_hashtag.internal_representation.annotations.Annotatable;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.types.Generic;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.*;


public class Function implements Generic, Annotatable
{
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    String type;
    List<VariableDeclarationExpression> arguments = new ArrayList<>();
    List<Expression> body;

    @Override
    public Map<String, String> properties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> defaults()
    {
        return Map.of("inline", List.of("true", "false"),
                "visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "const", "static"));
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> genericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
