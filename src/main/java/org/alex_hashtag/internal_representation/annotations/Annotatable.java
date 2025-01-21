package org.alex_hashtag.internal_representation.annotations;

import java.util.List;
import java.util.Map;


public interface Annotatable
{
    Map<String, String> properties();
    Map<String, List<String>> defaults();
}
