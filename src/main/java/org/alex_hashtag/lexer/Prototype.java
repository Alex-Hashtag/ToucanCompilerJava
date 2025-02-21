package org.alex_hashtag.lexer;

import org.alex_hashtag.internal_representation.types.TypeHolder;

import java.util.List;
import java.util.Map;

public interface Prototype
{
    record Variable(TypeHolder type, String name) implements Prototype {}
    record Function(TypeHolder type, String name) implements Prototype {}
    record Class(String name) implements Prototype {}
    record Enum(String name, Map<String, TypeHolder> variants) implements Prototype {}
    record Trait(String name) implements Prototype {}
}
