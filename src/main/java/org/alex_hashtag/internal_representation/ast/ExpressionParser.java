package org.alex_hashtag.internal_representation.ast;

import lombok.Getter;
import org.alex_hashtag.errors.ParsingErrorManager;
import org.alex_hashtag.internal_representation.expression.*;
import org.alex_hashtag.internal_representation.utils.ListUtils;
import org.alex_hashtag.tokenization.Coordinates;
import org.alex_hashtag.tokenization.Token;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.alex_hashtag.tokenization.TokenType.*;


public class ExpressionParser
{
    // -- A small buffer for single-token "peeking" logic --
    private Token storedPeek = null;
    private boolean hasPeek = false;

//    public Expression parseNextExpression(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
//    {
//        Coordinates start = current.coordinates;
//
//        List<Pair<Expression, Token>> expressions = new ArrayList<>();
//        Expression lastExpr= null;
//        Token closingSymbol = null;
//        while (closingSymbol == null || (closingSymbol.type != SEMI_COLON && closingSymbol.type != BRACE_CLOSED))
//        {
//
//
//            lastExpr = switch (current.type)
//            {
//                case IF -> parseIf(iterator, current, errorManager);
//                case FOR -> parseForLoop(iterator, current, errorManager);
//                case WHILE -> parseWhileLoop(iterator, current, errorManager);
//                case DO -> parseDoWhileLoop(iterator, current, errorManager);
//                case LOOP -> parseLoop(iterator, current, errorManager);
//                case BREAK -> parseBreak(iterator, current, errorManager);
//                case CONTINUE -> parseContinue(iterator, current, errorManager);
//                case ECHO -> parseEcho(iterator, current, errorManager);
//                case RETURN -> parseReturn(iterator, current, errorManager);
//                case YIELD -> parseYield(iterator, current, errorManager);
//                case SIZEOF -> parseSizeOf(iterator, current, errorManager);
//                case TYPEOF -> parseTypeOf(iterator, current, errorManager);
//                case SWITCH -> parseSwitch(iterator, current, errorManager);
//                case BRACE_OPEN -> parseNextExpression(iterator, current, errorManager);
//                case CURLY_OPEN -> parseScope(iterator, current, errorManager);
//                case UNSAFE -> parseUnsafe(iterator, current, errorManager);
//                case INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, RUNE_LITERAL , STRING_LITERAL -> parseBasicLiteral(iterator, current, errorManager);
//                case IDENTIFIER -> parseWhateverTheFuckThisMightBe(iterator, current, errorManager);
//                default -> lastExpr = null;
//            };
//
//             closingSymbol = consumeNonComment(iterator);
//
//            expressions.add(Pair.create(lastExpr, closingSymbol));
//        }
//
//        Expression result = expressions.getFirst().getLeft();
//
//        if (expressions.isEmpty())
//            return new EmptyExpression(start);
//
//
//        for (Pair<Token, Expression> junction : ListUtils.flipAndTrim(expressions))
//        {
//            Operator op = Operator.fromToken(junction.getLeft());
//            if (op!=null)
//                result = new BinaryExpression(result, start, op, junction.getRight());
//            else if (junction.getLeft().type==DOT)
//            {
//                if (junction.getRight() instanceof UnitExpression)
//                    result = new FieldAccessExpression(((UnitExpression) junction.getRight()).getIdentifier(), result, start);
//                else if (junction.getRight() instanceof FunctionInvokationExpression)
//                    ;
//            }
//        }
//
//
//        return null; //!temp to remove an error
//    }

    private Expression parseForLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
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
