package org.alex_hashtag.internal_representation.expressionOld;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Mutability;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;


public class VariableDeclarationExpression implements Expression
{
    @Getter
    CoordinatesOLD location;
    Mutability mutability;
    String type;
    String identifier;

    public VariableDeclarationExpression(String identifier, Mutability mutability, String type)
    {
        this.identifier = identifier;
        this.mutability = mutability;
        this.type = type;
    }

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString()
    {
        return mutability + type + " " + identifier;
    }
}
