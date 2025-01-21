package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Mutability;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;


public class VariableDeclarationAssignmentExpression implements Expression
{
    Mutability mutability;
    String type;
    String identifier;
    Expression assignment;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }
}