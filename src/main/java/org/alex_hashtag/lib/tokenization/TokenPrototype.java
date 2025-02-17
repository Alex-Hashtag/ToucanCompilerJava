package org.alex_hashtag.lib.tokenization;

public sealed interface TokenPrototype
        permits TokenPrototype.Comment, TokenPrototype.Delimeter, TokenPrototype.End, TokenPrototype.Identifier, TokenPrototype.Keyword, TokenPrototype.Literal, TokenPrototype.NewLine, TokenPrototype.Operator, TokenPrototype.Start
{
    record Keyword(String value) implements TokenPrototype
    {
    }

    record Delimeter(String value) implements TokenPrototype
    {
    }

    record Operator(String value) implements TokenPrototype
    {
    }

    record Literal(String type, String regex) implements TokenPrototype
    {
    }

    record Identifier(String type, String regex) implements TokenPrototype
    {
    }

    record Comment(String regex) implements TokenPrototype
    {
    }

    record Start() implements TokenPrototype
    {
    }

    record End() implements TokenPrototype
    {
    }

    record NewLine() implements TokenPrototype
    {
    }
}
