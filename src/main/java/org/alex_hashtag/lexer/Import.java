package org.alex_hashtag.lexer;

import org.alex_hashtag.internal_representation.types.TypeHolder;

import java.util.List;

/**
 * Represents an import statement with its type.
 */
public record Import(Type type, List<String> fragments)
{
    interface Type
    {
        record Class() implements Type {};
        record Enum() implements Type {};
        record Trait() implements Type {};
        record Var(TypeHolder type) implements Type {};
        record Func(TypeHolder type) implements Type {};
        record Macro() implements Type {};
        record Annotation() implements Type {};
    }
}