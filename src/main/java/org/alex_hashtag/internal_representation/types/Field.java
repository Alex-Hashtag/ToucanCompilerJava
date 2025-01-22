package org.alex_hashtag.internal_representation.types;

import lombok.Getter;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Field implements Annotatable
{
    @Getter
    Coordinates location;
    Map<String, String> properties = new HashMap<>();
    String type;
    String identifier;
    Expression defaultValue;


    @Override
    public Map<String, String> properties()
    {
        return properties;
    }

    @Override
    public Map<String, List<String>> defaults()
    {
        return Map.of("visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "const", "static_mutable", "static"));
    }
}
