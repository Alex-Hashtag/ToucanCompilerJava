package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.*;


public class Class implements Type, Generic, Annotatable
{

    Set<Annotation> annotations = new HashSet<>();
    @Getter
    CoordinatesOLD location;
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    @Setter
    String name;
    String extendsThis;
    List<String> traits;
    List<Field> fields;
    List<Function> methods;

    @Override
    public Optional<List<VariableDeclarationExpression>> getGenericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
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
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "static", "abstract"));
    }

    @Override
    protected Class clone() throws CloneNotSupportedException
    {
        return (Class) super.clone();
    }
}
