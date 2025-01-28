package org.alex_hashtag.internal_representation.types;

public enum Mutability
{
    MUTABLE,
    IMMUTABLE,
    CONST;

    @Override
    public String toString()
    {
        return switch (this)
        {
            case MUTABLE -> "mutable ";
            case IMMUTABLE -> "";
            case CONST -> "const ";
        };
    }
}
