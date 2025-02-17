package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.*;


public class Struct implements Type, Annotatable
{
    Set<Annotation> annotations = new HashSet<>();
    @Getter
    CoordinatesOLD location;
    Map<String, String> properties = new HashMap<>();
    @Setter
    String name;
    List<String> traits;
    List<Field> fields;
    List<Function> methods;

    @Override
    public String getName()
    {
        return this.name;
    }


    @SneakyThrows
    @Override
    public Type getReferenced()
    {
        Struct newClass = this.clone();
        newClass.setName("&" + newClass.name);
        return newClass;
    }

    @SneakyThrows
    @Override
    public Type getArray()
    {
        Struct newClass = this.clone();
        newClass.setName(newClass.name + "[]");
        return newClass;
    }

    @SneakyThrows
    @Override
    public Type getReferencedArray()
    {
        Struct newClass = this.clone();
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
        return Map.of("visibility", List.of("public", "private"));
    }


    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }

    @Override
    protected Struct clone() throws CloneNotSupportedException
    {
        return (Struct) super.clone();
    }
}
