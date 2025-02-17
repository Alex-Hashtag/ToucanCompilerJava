package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.*;


public class Field implements Annotatable
{
    Set<Annotation> annotations = new HashSet<>();
    @Getter
    CoordinatesOLD location;
    Map<String, String> properties = new HashMap<>();
    String type;
    String identifier;
    Expression defaultValue;


    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "const", "static_mutable", "static"));
    }

    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }
}
