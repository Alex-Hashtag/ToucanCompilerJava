package org.alex_hashtag.internal_representation.function;

import org.alex_hashtag.internal_representation.expressionOld.Expression;
import org.alex_hashtag.internal_representation.expressionOld.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.types.Generic;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.*;



public class Function implements Generic, Annotatable
{
    Set<Annotation> annotations = new HashSet<>();
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    String type;
    List<VariableDeclarationExpression> arguments = new ArrayList<>();
    List<Expression> body;

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }


    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("inline", List.of("true", "false"),
                "visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "const", "static", "abstract"),
                "constructor", List.of("true", "false"),
                "overrides", List.of("true", "false"),
                "unsafe", List.of("true", "false"));
    }

    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> getGenericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
