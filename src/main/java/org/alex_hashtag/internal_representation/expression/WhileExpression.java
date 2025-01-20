package org.alex_hashtag.internal_representation.expression;

import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

public class WhileExpression implements Expression
{

    BooleanExpression condition;

    @Override
    public Type getType()
    {
        return TypeRegistry.searchByName("void").get();
    }
}
