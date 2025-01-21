package org.alex_hashtag.internal_representation.literals;

import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.expression.VariableAssignmentExpression;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.List;
import java.util.Optional;


public class EnumLiteral implements Literal
{

    String type;
    List<Expression> arguments;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
