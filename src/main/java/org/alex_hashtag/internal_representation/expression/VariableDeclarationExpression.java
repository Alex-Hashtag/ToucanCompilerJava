package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Mutability;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


public class VariableDeclarationExpression implements Expression
{
    @Getter
    Coordinates location;
    Mutability mutability;
    String type;
    String identifier;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    public VariableDeclarationExpression(String identifier, Mutability mutability, String type)
    {
        this.identifier = identifier;
        this.mutability = mutability;
        this.type = type;
    }
}
