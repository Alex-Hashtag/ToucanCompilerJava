package org.alex_hashtag.internal_representation.macros;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class AnnotationRegistry
{
    static Set<Annotation> registeredAnnotation = new HashSet<>();

    public static boolean registerAnnotation(Annotation annotation)
    {
        return registeredAnnotation.add(annotation);
    }

    public static Optional<Annotation> searchByName(final String name)
    {
        return registeredAnnotation.stream()
                .filter(t -> t.getIdentifier().equals(name))
                .findFirst();
    }
}

