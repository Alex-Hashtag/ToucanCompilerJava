package org.alex_hashtag.internal_representation.function;

import lombok.Getter;
import org.alex_hashtag.internal_representation.utils.Locatable;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;


@Getter
public class Implement implements Locatable
{
    CoordinatesOLD location;
    String trait;
    String type;
    List<Function> methods;
    boolean isPublic;
}
