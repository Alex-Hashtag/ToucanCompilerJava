package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.util.Locatable;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;


public class Annotation implements Locatable
{

    @Getter
    Coordinates location;
    @Getter
    String identifier;
    VariableDeclarationExpression argument;
    List<Function> methods;
}
