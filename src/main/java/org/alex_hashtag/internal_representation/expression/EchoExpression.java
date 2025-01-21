package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.Optional;


public class EchoExpression implements Expression
{
    Expression expr;
    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName("void");
    }
}
