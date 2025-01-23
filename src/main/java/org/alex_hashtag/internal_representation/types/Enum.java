package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.*;


public class Enum implements Type, Annotatable, Generic
{
    Set<Annotation> annotations = new HashSet<>();
    @Getter
    Coordinates location;
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    @Setter
    String name;
    List<String> traits;
    List<EnumVariant> variants;
    List<Function> methods;

    @Override
    public Map<String, String> getProperties()
    {
        return Map.of();
    }

    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("visibility", List.of("public", "private"));
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> getGenericArguments()
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
        Enum newEnum = this.clone();
        newEnum.setName("&" + newEnum.name);
        return newEnum;
    }

    @SneakyThrows
    @Override
    public Type getArray()
    {
        Enum newEnum = this.clone();
        newEnum.setName(newEnum.name + "[]");
        return newEnum;
    }

    @SneakyThrows
    @Override
    public Type getReferencedArray()
    {
        Enum newEnum = this.clone();
        newEnum.setName("&" + newEnum.name + "[]");
        return newEnum;
    }

    @Override
    public List<String> getTraits()
    {
        return List.of();
    }

    @Override
    protected Enum clone() throws CloneNotSupportedException
    {
        return (Enum) super.clone();
    }

    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }

    static class EnumVariant implements Annotatable
    {
        Set<Annotation> annotations = new HashSet<>();
        Map<String, String> properties = new HashMap<>();
        String identifier;
        List<VariableDeclarationExpression> arguments;

        @Override
        public Map<String, String> getProperties()
        {
            return properties;
        }

        @Override
        public Map<String, List<String>> getDefaults()
        {
            return Map.of();
        }

        @Override
        public Optional<Set<Annotation>> getAnnotations()
        {
            return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
        }
    }
}
