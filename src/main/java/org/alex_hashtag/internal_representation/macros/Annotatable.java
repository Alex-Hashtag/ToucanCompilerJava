package org.alex_hashtag.internal_representation.macros;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface Annotatable
{
    Map<String, String> getProperties();

    Map<String, List<String>> getDefaults();

    Optional<Set<Annotation>> getAnnotations();
}
