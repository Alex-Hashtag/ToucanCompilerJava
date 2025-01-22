package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.*;


public class Class implements Type, Generic, Annotatable
{

    @Getter
    Coordinates location;
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    @Setter
    String name;
    String extendsThis;
    List<String> traits;
    List<Field> fields;
    List<Function> methods;
    List<Type> storedTypes;
    List<Annotation> innerAnnotations;
    List<Macro> innerMacros;

    @Override
    public Optional<List<VariableDeclarationExpression>> genericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    @Override
    public String getName()
    {
        return this.name;
    }


    @SneakyThrows
    @Override
    public Type getReferenced()
    {
        Class newClass = this.clone();
        newClass.setName("&" + newClass.name);
        return newClass;
    }

    @SneakyThrows
    @Override
    public Type getArray()
    {
        Class newClass = this.clone();
        newClass.setName(newClass.name + "[]");
        return newClass;
    }

    @SneakyThrows
    @Override
    public Type getReferencedArray()
    {
        Class newClass = this.clone();
        newClass.setName("&" + newClass.name + "[]");
        return newClass;
    }

    @Override
    public List<String> getTraits()
    {
        return traits;
    }

    @Override
    public Map<String, String> properties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> defaults()
    {
        return Map.of("visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "namespace", "static", "abstract"));
    }

    @Override
    protected Class clone() throws CloneNotSupportedException
    {
        return (Class) super.clone();
    }
}
