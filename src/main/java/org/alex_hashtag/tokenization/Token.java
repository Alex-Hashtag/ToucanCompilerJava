package org.alex_hashtag.tokenization;

import java.util.Optional;

import static org.alex_hashtag.tokenization.TokenType.*;


public class Token
{
    public Coordinates coordinates;
    public TokenType type;
    public Optional<String> internal;

    private Token(Coordinates coordinates, TokenType type)
    {
        this.coordinates = coordinates;
        this.type = type;
        this.internal = Optional.empty();
    }

    private Token(Coordinates coordinates, TokenType type, String internal)
    {
        this.coordinates = coordinates;
        this.type = type;
        this.internal = internal.describeConstable();
    }

    public static Token getStart()
    {
        return new Token(new Coordinates(0, 0), START);
    }

    public static Token getEnd()
    {
        return new Token(new Coordinates(0, 0), END);
    }

    public static Token basic(int row, int column, TokenType type)
    {
        return new Token(new Coordinates(row, column), type);
    }

    public static Token stored(int row, int column, TokenType type, String internal)
    {
        return new Token(new Coordinates(row, column), type, internal);
    }


    public static Token from(String sequence, int row, int column)
    {
        for (TokenType type : TokenType.values())
            if (sequence.matches(type.regex))
            {
                if (type.isStored())
                    return new Token(new Coordinates(row, column), type, sequence);
                else return new Token(new Coordinates(row, column), type);
            }

        return new Token(new Coordinates(row, column), INVALID, sequence);
    }
}
