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


public class Typedef implements Type, Generic, Annotatable
{
    Set<Annotation> annotations = new HashSet<>();
    @Getter
    CoordinatesOLD location;
    Map<String, String> properties = new HashMap<>();
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    @Getter
    @Setter
    String name;
    List<String> traits;
    String innerType;
    boolean isOutsideCastable;
    List<Function> methods;

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("visibility", List.of("public", "private"));
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
