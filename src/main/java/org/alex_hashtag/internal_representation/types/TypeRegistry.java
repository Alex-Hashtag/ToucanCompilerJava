package org.alex_hashtag.internal_representation.types;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TypeRegistry
{
    static Set<Type> registeredTypes = new HashSet<>();

    public static boolean registerType(Type type)
    {
        return registeredTypes.add(type) && registeredTypes.add(type.getArray()) && registeredTypes.add(type.getReferencedArray()) && registeredTypes.add(type.getReferenced());
    }

    public static Optional<Type> searchByName(final String name)
    {
        return registeredTypes.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst();
    }

    static {
        registerType(new PrimativeType("int8"));
        registerType(new PrimativeType("int16"));
        registerType(new PrimativeType("int32"));
        registerType(new PrimativeType("int64"));
        registerType(new PrimativeType("int128"));
        registerType(new PrimativeType("uint8"));
        registerType(new PrimativeType("uint16"));
        registerType(new PrimativeType("uint32"));
        registerType(new PrimativeType("uint64"));
        registerType(new PrimativeType("uint128"));
        registerType(new PrimativeType("usize"));
        registerType(new PrimativeType("float16"));
        registerType(new PrimativeType("float32"));
        registerType(new PrimativeType("float64"));
        registerType(new PrimativeType("float80"));
        registerType(new PrimativeType("float128"));
        registerType(new PrimativeType("bool"));
        registerType(new PrimativeType("char"));
        registerType(new PrimativeType("rune"));
        registerType(new PrimativeType("string"));
        registerType(new PrimativeType("void"));
        registerType(new PrimativeType("type"));
    }
}

