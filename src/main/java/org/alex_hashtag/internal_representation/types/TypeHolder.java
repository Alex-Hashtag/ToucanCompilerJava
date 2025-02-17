package org.alex_hashtag.internal_representation.types;

import org.alex_hashtag.lib.tokenization.Coordinates;


public interface TypeHolder
{
    record Resolved(Coordinates position, String name) implements TypeHolder {}
    record IncompatibleTypesInBinaryExpression(Coordinates position, String leftType, String rightType) implements TypeHolder {}
}
