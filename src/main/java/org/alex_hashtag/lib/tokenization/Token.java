package org.alex_hashtag.lib.tokenization;

import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

public sealed interface Token
        permits Token.Keyword, Token.Delimiter, Token.Operator, Token.Literal, Token.Identifier, Token.Comment, Token.Start, Token.End, Token.NewLine, Token.Invalid
{
    record Keyword(CoordinatesOLD position, String value) implements Token
    {
    }

    record Delimiter(CoordinatesOLD position, String value) implements Token
    {
    }

    record Operator(CoordinatesOLD position, String value) implements Token
    {
    }

    record Literal(CoordinatesOLD position, String type, String value) implements Token
    {
    }

    record Identifier(CoordinatesOLD position, String type, String value) implements Token
    {
    }

    record Comment(CoordinatesOLD position, String value) implements Token
    {
    }

    record Start(CoordinatesOLD position) implements Token
    {
    }

    record End(CoordinatesOLD position) implements Token
    {
    }

    record NewLine(CoordinatesOLD position) implements Token
    {
    }

    record Invalid(CoordinatesOLD position, String value) implements Token
    {
    }
}
