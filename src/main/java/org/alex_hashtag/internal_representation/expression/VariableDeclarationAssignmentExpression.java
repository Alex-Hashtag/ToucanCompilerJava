package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Mutability;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class VariableDeclarationAssignmentExpression implements Expression
{
    @Getter
    Coordinates location;
    Mutability mutability;
    String type;
    String identifier;
    Expression assignment;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        return mutability + type + " " + identifier + " = " + assignment;
    }
}
