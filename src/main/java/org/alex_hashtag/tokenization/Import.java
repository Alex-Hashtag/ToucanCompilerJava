package org.alex_hashtag.tokenization;

import java.util.List;

/**
 * Represents an import statement with its type.
 */
public record Import(List<String> fragments, ImportType type)
{
    public enum ImportType { TYPE, FUNCTION, MACRO, ANNOTATION }
}