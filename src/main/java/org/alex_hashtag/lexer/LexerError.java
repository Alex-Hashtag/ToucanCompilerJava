package org.alex_hashtag.lexer;

import org.alex_hashtag.lib.errors.CompilerError;
import org.alex_hashtag.lib.tokenization.Token;


public record LexerError(String message, int line, int column, Token token, String hint) implements CompilerError
{
    @Override
    public String getMessage()
    {
        return message;
    }

    @Override
    public int getLine()
    {
        return line;
    }

    @Override
    public int getColumn()
    {
        return column;
    }

    @Override
    public String getToken()
    {
        return token.getValue();
    }

    @Override
    public String getHint()
    {
        return hint;
    }
}
