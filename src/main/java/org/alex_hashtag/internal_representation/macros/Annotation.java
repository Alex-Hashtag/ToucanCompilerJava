package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expressionOld.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.utils.Locatable;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;


public class Annotation implements Locatable
{

    @Getter
    CoordinatesOLD location;
    @Getter
    String identifier;
    VariableDeclarationExpression argument;
    List<Function> methods;
}
