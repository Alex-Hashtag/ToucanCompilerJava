package org.alex_hashtag.internal_representation.function;

import lombok.Getter;
import org.alex_hashtag.internal_representation.utils.Locatable;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;


public class Implement implements Locatable
{
    @Getter
    Coordinates location;
    String trait;
    String type;
    List<Function> methods;
}
