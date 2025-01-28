package org.alex_hashtag.internal_representation.ast;

import org.alex_hashtag.errors.ParsingErrorManager;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.expression.ForExpression;
import org.alex_hashtag.tokenization.Token;
import static org.alex_hashtag.tokenization.TokenType.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExpressionParser
{
    // -- A small buffer for single-token "peeking" logic --
    private Token storedPeek = null;
    private boolean hasPeek = false;

    public Expression parseNextExpression(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        if (current.type.equals(FOR))
            return parseForLoop(iterator, current, errorManager);




        return null; //!temp to remove an error
    }

    public Expression parseForLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        current = consumeNonComment(iterator);
        if (!current.type.equals(BRACE_OPEN))
            errorManager.reportError(new ParsingErrorManager.ParsingError(
                    ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                    ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                    current.coordinates.row(),
                    current.coordinates.column(),
                    current.toString(),
                    "A 'for' loop must be opened with a brace, please add one",
                    "(",
                    current.describeContents()
            ));

        List<Expression> initialization = new ArrayList<>();
        initialization.add(parseNextExpression(iterator, current, errorManager));
        Expression condition;
        List<Expression> update = new ArrayList<>();


        current = consumeNonComment(iterator);
        while (current.type.equals(COMMA))
        {
            initialization.add(parseNextExpression(iterator, current, errorManager));
            current = consumeNonComment(iterator);


        }

        return null; //!temp to remove an error
    }

    private Token consumeNonComment(Iterator<Token> iterator)
    {
        while (iterator.hasNext())
        {
            Token t = consumePeekIfAny(iterator);
            if (t == null)
                return null;
            if (t.type != COMMENT)
                return t;
        }
        return null;
    }

    /**
     * A minimal "peek" approach to see the next non-comment token
     * without consuming it from the iterator.
     */
    private Token peekNextNonComment(Iterator<Token> iterator)
    {
        if (!iterator.hasNext())
            return null;
        Token t = consumePeekIfAny(iterator);
        while (t != null && t.type == COMMENT && iterator.hasNext())
        {
            t = consumePeekIfAny(iterator);
        }
        if (t != null && t.type != COMMENT)
        {
            // Re-store it for future consumption
            storedPeek = t;
            hasPeek = true;
        }
        return t;
    }

    /**
     * Consumes the peeked token if any; otherwise, consumes from the iterator.
     */
    private Token consumePeekIfAny(Iterator<Token> iterator)
    {
        if (hasPeek)
        {
            hasPeek = false;
            return storedPeek;
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}
