package org.alex_hashtag.internal_representation.types;

import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;

import java.util.List;
import java.util.Optional;

public interface Generic
{
    Optional<List<VariableDeclarationExpression>> genericArguments();
}
