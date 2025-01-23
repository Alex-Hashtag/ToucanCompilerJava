package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


/**
 * Expression to be used in on the right side of each arm of the Macro in order to later be replaced by the proper type
 */
@Getter
public class MacroExpression implements Expression
{

    @Getter
    Coordinates location;
    String name;

    @Override
    public Optional<Type> getType()
    {
        return Optional.empty();
    }
}
