package org.alex_hashtag.lib.tokenization;



public sealed interface Token
        permits Token.Keyword, Token.Delimiter, Token.Operator, Token.Literal, Token.Identifier, Token.Comment, Token.Start, Token.End, Token.NewLine, Token.Invalid
{
    record Keyword(Coordinates position, String value) implements Token
    {
    }

    record Delimiter(Coordinates position, String value) implements Token
    {
    }

    record Operator(Coordinates position, String value) implements Token
    {
    }

    record Literal(Coordinates position, String type, String value) implements Token
    {
    }

    record Identifier(Coordinates position, String type, String value) implements Token
    {
    }

    record Comment(Coordinates position, String value) implements Token
    {
    }

    record Start(Coordinates position) implements Token
    {
    }

    record End(Coordinates position) implements Token
    {
    }

    record NewLine(Coordinates position) implements Token
    {
    }

    record Invalid(Coordinates position, String value) implements Token
    {
    }
}
