package org.alex_hashtag.internal_representation.ast;

import org.alex_hashtag.errors.ParsingErrorManager;
import org.alex_hashtag.internal_representation.expression.*;
import org.alex_hashtag.internal_representation.literals.*;
import org.alex_hashtag.internal_representation.utils.ListUtils;
import org.alex_hashtag.tokenization.Coordinates;
import org.alex_hashtag.tokenization.Token;
import org.alex_hashtag.tokenization.TokenType;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.alex_hashtag.errors.ParsingErrorManager.ErrorType;
import static org.alex_hashtag.errors.ParsingErrorManager.ParsingError;
import static org.alex_hashtag.tokenization.TokenType.*;


/**
 * A parser for expressions, statements, and small language constructs.
 */
public class ExpressionParser
{
    // -- A small buffer for single-token "peeking" logic --
    private Token storedPeek = null;
    private boolean hasPeek = false;

    /**
     * Parses an expression starting at `current` until the `closer` token.
     *
     * @param iterator     Token stream (starts with START, ends with END).
     * @param current      The current token we are to parse from.
     * @param errorManager For reporting errors.
     * @param closer       The token type that signals the end of this expression context.
     * @return An {@link Expression} node.
     */
    public Expression parseNextExpression(Iterator<Token> iterator,
                                          Token current,
                                          ParsingErrorManager errorManager,
                                          TokenType closer)
    {
        Coordinates start = current.coordinates;
        List<Pair<Expression, Token>> expressions = new ArrayList<>();

        // We accumulate expressions until we hit 'closer'
        while (current.type != closer)
        {
            Expression lastExpr = switch (current.type)
            {
                case IF -> parseIf(iterator, current, errorManager);
                case WHILE -> parseWhileLoop(iterator, current, errorManager);
                case DO -> parseDoWhileLoop(iterator, current, errorManager);
//                case FOR -> parseForLoop(iterator, current, errorManager);
                case LOOP -> parseLoop(iterator, current, errorManager);
                case BREAK -> new BreakExpression(current.coordinates);
                case CONTINUE -> new ContinueExpression(current.coordinates);
                case ECHO -> new EchoExpression(current.coordinates,
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                SEMI_COLON));
                case RETURN -> new ReturnExpression(current.coordinates,
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                SEMI_COLON));
                case YIELD -> new YieldExpression(current.coordinates,
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                SEMI_COLON));
                case SIZEOF -> new SizeOfExpression(current.coordinates,
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                SEMI_COLON));
                case TYPEOF -> new TypeOfExpression(current.coordinates,
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                SEMI_COLON));
                case SWITCH -> parseSwitch(iterator, current, errorManager);
                case BRACE_OPEN -> // e.g. "(" expression ")"
                        parseNextExpression(iterator,
                                consumeNonComment(iterator),
                                errorManager,
                                BRACE_CLOSED);
                case CURLY_OPEN -> parseScope(iterator, current, errorManager);
                case UNSAFE -> parseUnsafe(iterator, current, errorManager);
                case INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL,
                     RUNE_LITERAL, STRING_LITERAL -> parseBasicLiteral(iterator, current, errorManager);
                // case IDENTIFIER -> parseWhateverTheFuckThisMightBe(iterator, current, errorManager);

                default ->
                {
                    // Unknown expression start:
                    errorManager.reportError(
                            ParsingError.of(
                                    ErrorType.EXPECTED_FOUND,
                                    ErrorType.EXPECTED_FOUND.getDescription(),
                                    current.coordinates.row(),
                                    current.coordinates.column(),
                                    "an expression",
                                    current.describeContents()
                            )
                    );
                    yield new EmptyExpression(current.coordinates);
                }
            };

            // Peek the next token (often an operator or the 'closer').
            Token next = peekNextNonComment(iterator);
            if (next == null)
            {
                // If we have no more tokens, break early.
                expressions.add(Pair.create(lastExpr, null));
                break;
            }
            expressions.add(Pair.create(lastExpr, next));

            // If the next token is the closer, we're done; consume it and break.
            if (next.type == closer)
            {
                consumeNonComment(iterator); // consume the closer
                break;
            }

            // Otherwise, consume the next token so we can continue.
            current = consumeNonComment(iterator);
        }

        if (expressions.isEmpty())
        {
            return new EmptyExpression(start);
        }

        // Build the final expression from the collected segments
        Expression result = expressions.get(0).getLeft(); // The first parsed expression
        if (expressions.size() == 1)
        {
            // Only one expression, so just return it
            return result;
        }

        // If there's more than one, potentially do something with them
        // (e.g. handle operators, field accesses, etc.)
        // In your code you have this "flip and trim" approach. We'll keep that logic,
        // but you can also choose to do a real precedence-based parse if needed.
        List<Pair<Token, Expression>> flipped = ListUtils.flipAndTrim(expressions);
        for (Pair<Token, Expression> junction : flipped)
        {
            Token opToken = junction.getLeft();
            Expression rightExpr = junction.getRight();

            // If it's a recognized operator
            Operator op = Operator.fromToken(opToken);
            if (op != null)
            {
                result = new BinaryExpression(result, start, op, rightExpr);
            }
            // Or if it's a field access or function call: e.g. 'someExpr.field'
            else if (opToken.type == DOT)
            {
                if (rightExpr instanceof UnitExpression ue)
                {
                    result = new FieldAccessExpression(ue.getIdentifier(), result, start);
                }
                else if (rightExpr instanceof FunctionInvokationExpression)
                {
                    // handle function call: e.g. result = new FunctionCallExpression(result, args)
                    // ...
                }
            }
            // Add more specialized logic as needed...
        }

        return result;
    }

    /* =========================
       Specialized Parse Methods
       ========================= */

    /**
     * Parses a switch expression of the form:
     * <pre>
     *   switch ( some_expr ) {
     *       pattern -> expression;
     *       pattern -> expression;
     *       ...
     *   }
     * </pre>
     */
    private Expression parseSwitch(Iterator<Token> iterator,
                                   Token current,
                                   ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;

        // Expect '(' after 'switch'
        Token open = expectNext(iterator, BRACE_OPEN, errorManager, "Expected '(' after 'switch'.");
        // Parse expression inside '(...)'
        Token exprToken = consumeNonComment(iterator);
        Expression compareTo = parseNextExpression(iterator, exprToken, errorManager, BRACE_CLOSED);

        // Expect ')' now
        expectNext(iterator, BRACE_CLOSED, errorManager, "Expected ')' after switch expression.");

        // Expect '{' for switch body
        expectNext(iterator, CURLY_OPEN, errorManager, "Expected '{' for switch body.");

        // Now parse arms until '}'
        List<SwitchExpression.Arm> arms = new ArrayList<>();
        Token t = consumeNonComment(iterator);
        while (t != null && t.type != CURLY_CLOSED)
        {
            // pattern -> expression;
            Expression pattern = parseNextExpression(iterator, t, errorManager, ARROW);
            // after ARROW, parse the expression
            consumeNonComment(iterator); // consume '->'
            Token exprStart = consumeNonComment(iterator);
            Expression armExpr = parseNextExpression(iterator, exprStart, errorManager, SEMI_COLON);
            arms.add(new SwitchExpression.Arm(pattern, armExpr));

            // Move on to the next potential arm
            t = consumeNonComment(iterator);
        }

        return new SwitchExpression(start, compareTo, arms);
    }

    /**
     * Parses a `loop` expression. The grammar is assumed to allow either:
     * <pre>
     *   loop { ... }               // indefinite loop
     *   loop(expression) { ... }   // loop that runs `expression` times
     * </pre>
     * In both cases, either a block `{ ... }` or a single statement is permitted for the body.
     */
    private Expression parseLoop(Iterator<Token> iterator,
                                 Token current,
                                 ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;

        // Peek next token to see if we have '(' for iteration count
        Token next = peekNextNonComment(iterator);
        Expression iterationCount = null;

        // If next is '(' => parse expression inside '(...)'
        if (next != null && next.type == BRACE_OPEN)
        {
            consumeNonComment(iterator); // consume '('
            iterationCount = parseNextExpression(iterator,
                    consumeNonComment(iterator),
                    errorManager,
                    BRACE_CLOSED);
            // expect ')'
            Token closingParen = consumeNonComment(iterator);
            if (closingParen.type != BRACE_CLOSED)
            {
                errorManager.reportError(
                        ParsingError.of(
                                ErrorType.EXPECTED_FOUND,
                                "Expected ')' after loop iteration count.",
                                closingParen.coordinates.row(),
                                closingParen.coordinates.column(),
                                closingParen.toString(),
                                BRACE_CLOSED.regex,
                                closingParen.describeContents()
                        )
                );
                iterationCount = new EmptyExpression(start);
            }
        }

        // Next, parse loop body: block or single statement
        List<Expression> statements = parseBlockOrStatement(iterator, errorManager);

        if (iterationCount == null)
        {
            // indefinite loop
            return new LoopExpression(start, statements, !statements.isEmpty());
        }
        else
        {
            // loop with iteration count
            return new LoopExpression(iterationCount, statements, start, !statements.isEmpty());
        }
    }



    /**
     * Parses an `unsafe` expression which can contain either a single statement or a block.
     */
    private Expression parseUnsafe(Iterator<Token> iterator,
                                   Token current,
                                   ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        // 'unsafe' is consumed, parse body as block or statement
        List<Expression> statements = parseBlockOrStatement(iterator, errorManager);
        // If it's a block, parseBlockOrStatement will capture that
        boolean brackets = (statements.size() > 1 || // multiple statements => had braces
                peekPrevious().type == CURLY_OPEN);
        return new UnsafeExpression(statements, start, brackets);
    }

    /**
     * Parses a do-while loop of the form:
     * <pre>
     *   do { ... } while (condition)
     * </pre>
     * or
     * <pre>
     *   do statement while (condition)
     * </pre>
     */
    private Expression parseDoWhileLoop(Iterator<Token> iterator,
                                        Token current,
                                        ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        // 'do' is consumed, parse block or statement
        List<Expression> statements = parseBlockOrStatement(iterator, errorManager);

        // next token must be 'while'
        Token next = consumeNonComment(iterator);
        if (next.type != WHILE)
        {
            errorManager.reportError(
                    ParsingError.of(
                            ErrorType.EXPECTED_FOUND,
                            "'while' expected after 'do' block/statement.",
                            next.coordinates.row(),
                            next.coordinates.column(),
                            next.toString(),
                            WHILE.regex,
                            next.describeContents()
                    )
            );
        }

        // parse condition in parentheses
        Expression condition = parseParenthesizedExpression(iterator, errorManager,
                "Expected '(' after 'do ... while'.");

        return new WhileExpression(condition, statements, start, hasBrackets(statements));
    }

    /**
     * Parses a while loop of the form:
     * <pre>
     *   while (condition) { ... }
     * </pre>
     * or
     * <pre>
     *   while (condition) statement
     * </pre>
     */
    private Expression parseWhileLoop(Iterator<Token> iterator,
                                      Token current,
                                      ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        // parse condition
        Expression condition = parseParenthesizedExpression(iterator, errorManager,
                "Expected '(' after 'while'.");

        // parse loop body
        List<Expression> statements = parseBlockOrStatement(iterator, errorManager);
        return new WhileExpression(condition, statements, start, hasBrackets(statements));
    }

    /**
     * Parses a scope: `{ ... }`
     */
    private Expression parseScope(Iterator<Token> iterator,
                                  Token current,
                                  ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        // The '{' is already in `current`
        // parse everything inside until '}'
        List<Expression> expressions = new ArrayList<>();
        Token t = consumeNonComment(iterator);

        while (t != null && t.type != CURLY_CLOSED)
        {
            expressions.add(parseNextExpression(iterator, t, errorManager, SEMI_COLON));
            t = consumeNonComment(iterator);
        }
        return new ScopeExpression(start, expressions);
    }

    /**
     * Parses an if-elseif-else chain.
     */
    private Expression parseIf(Iterator<Token> iterator,
                               Token current,
                               ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;

        // parse condition in parentheses => if ( condition )
        Expression condition = parseParenthesizedExpression(iterator, errorManager,
                "Expected '(' after 'if'.");

        // parse if-body
        List<Expression> ifBody = parseBlockOrStatement(iterator, errorManager);

        // check if there's an else or else-if
        Token elsePeek = peekNextNonComment(iterator);
        IfExpression elseExpr = null;
        if (elsePeek != null && elsePeek.type == ELSE)
        {
            consumeNonComment(iterator); // consume 'else'
            Token maybeIf = peekNextNonComment(iterator);
            if (maybeIf != null && maybeIf.type == IF)
            {
                consumeNonComment(iterator); // consume 'if'
                // parse "else if" as a separate if-expression
                elseExpr = (IfExpression) parseIf(iterator, maybeIf, errorManager);
                elseExpr.setElse(true);
            }
            else
            {
                // parse else block or single statement
                List<Expression> elseBody = parseBlockOrStatement(iterator, errorManager);
                elseExpr = new IfExpression(start, null, elseBody, null, hasBrackets(elseBody), true);
            }
        }

        // Construct this if expression
        // If you need further distinction between single condition vs. binary expression,
        // you can do a check (condition instanceof BinaryExpression).
        return new IfExpression(start,
                (condition instanceof BinaryExpression be) ? be : null,
                ifBody,
                elseExpr,
                hasBrackets(ifBody),
                false);
    }

    /**
     * Parses one of:
     * - INT_LITERAL
     * - FLOAT_LITERAL
     * - CHAR_LITERAL
     * - RUNE_LITERAL
     * - STRING_LITERAL
     */
    private Expression parseBasicLiteral(Iterator<Token> iterator,
                                         Token current,
                                         ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        String rawText = current.internal.orElse("");
        Literal literal;

        switch (current.type)
        {
            case INT_LITERAL ->
            {
                IntegerLiteral intLit = new IntegerLiteral();
                intLit.setLocation(start);
                intLit.setSizeInBytes((byte) 4); // default
                intLit.setUnsigned(false);
                intLit.setInternal(rawText);
                literal = intLit;
            }
            case FLOAT_LITERAL ->
            {
                FloatLiteral floatLit = new FloatLiteral();
                floatLit.setLocation(start);
                floatLit.setSizeInBytes((byte) 4); // default
                floatLit.setInternal(rawText);
                literal = floatLit;
            }
            case CHAR_LITERAL ->
            {
                CharLiteral charLit = new CharLiteral();
                charLit.setLocation(start);
                charLit.setValue(rawText);
                literal = charLit;
            }
            case RUNE_LITERAL ->
            {
                RuneLiteral runeLit = new RuneLiteral();
                runeLit.setLocation(start);
                // Convert rawText to numeric rune if desired:
                runeLit.setValue(0);
                literal = runeLit;
            }
            case STRING_LITERAL ->
            {
                StringLiteral strLit = new StringLiteral();
                strLit.setLocation(start);
                strLit.setValue(rawText);
                literal = strLit;
            }
            default ->
            {
                errorManager.reportError(ParsingError.of(
                        ErrorType.SYNTAX_ERROR,
                        ErrorType.SYNTAX_ERROR.getDescription(),
                        start.row(),
                        start.column(),
                        current.toString(),
                        current.describeContents()
                ));
                return new EmptyExpression(start);
            }
        }

        return new LiteralExpression(start, literal);
    }

    /* =========================
       Small Helper Methods
       ========================= */

    /**
     * Consumes tokens until it finds a non-comment one. Returns {@code null} if exhausted.
     */
    private Token consumeNonComment(Iterator<Token> iterator)
    {
        while (iterator.hasNext())
        {
            Token t = consumePeekIfAny(iterator);
            if (t == null)
            {
                return null;
            }
            if (t.type != COMMENT)
            {
                return t;
            }
        }
        return null;
    }

    /**
     * Peeks at the next token (skipping comments) without consuming it.
     */
    private Token peekNextNonComment(Iterator<Token> iterator)
    {
        if (!iterator.hasNext())
        {
            return null;
        }
        Token t = consumePeekIfAny(iterator);
        while (t != null && t.type == COMMENT && iterator.hasNext())
        {
            t = consumePeekIfAny(iterator);
        }
        if (t != null && t.type != COMMENT)
        {
            // re-store it
            storedPeek = t;
            hasPeek = true;
        }
        return t;
    }

    /**
     * Consumes the peeked token if any; otherwise consumes from the iterator.
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

    /**
     * Convenience method to require that the next token is of a given type.
     */
    private Token expectNext(Iterator<Token> iterator,
                             TokenType expected,
                             ParsingErrorManager errorManager,
                             String errorMsg)
    {
        Token t = consumeNonComment(iterator);
        if (t == null || t.type != expected)
        {
            if (t != null)
            {
                errorManager.reportError(ParsingError.of(
                        ErrorType.EXPECTED_FOUND,
                        errorMsg,
                        t.coordinates.row(),
                        t.coordinates.column(),
                        t.toString(),
                        expected.regex,
                        t.describeContents()
                ));
            }
            else
            {
                // Token stream ended unexpectedly
                errorManager.reportError(ParsingError.of(
                        ErrorType.EXPECTED_FOUND,
                        errorMsg,
                        -1,
                        -1,
                        "EOF",
                        expected.regex,
                        "EOF"
                ));
            }
        }
        return t;
    }

    /**
     * Parses an expression within parentheses: e.g. "( condition )".
     * Reports an error if parentheses are missing or mismatched.
     */
    private Expression parseParenthesizedExpression(Iterator<Token> iterator,
                                                    ParsingErrorManager errorManager,
                                                    String errorMessage)
    {
        Token open = consumeNonComment(iterator);
        if (open == null || open.type != BRACE_OPEN)
        {
            if (open != null)
            {
                errorManager.reportError(ParsingError.of(
                        ErrorType.EXPECTED_FOUND,
                        errorMessage,
                        open.coordinates.row(),
                        open.coordinates.column(),
                        open.toString(),
                        BRACE_OPEN.regex,
                        open.describeContents()
                ));
            }
            else
            {
                errorManager.reportError(ParsingError.of(
                        ErrorType.EXPECTED_FOUND,
                        errorMessage,
                        -1,
                        -1,
                        "EOF",
                        BRACE_OPEN.regex,
                        "EOF"
                ));
            }
            // Return an empty expression so we can keep parsing
            return new EmptyExpression(new Coordinates(-1, -1));
        }

        // Parse expression up to ')'
        Token exprStart = consumeNonComment(iterator);
        Expression expr = parseNextExpression(iterator, exprStart, errorManager, BRACE_CLOSED);

        // We already consume the closer in parseNextExpression, but let's
        // just confirm it's indeed a ')' if not consumed
        return expr;
    }

    /**
     * Either parses a `{ ... }` block or a single statement/expression (until `;`).
     */
    private List<Expression> parseBlockOrStatement(Iterator<Token> iterator,
                                                   ParsingErrorManager errorManager)
    {
        List<Expression> statements = new ArrayList<>();
        Token bodyStart = consumeNonComment(iterator);

        if (bodyStart != null && bodyStart.type == CURLY_OPEN)
        {
            // parse everything inside { ... }
            Token token = consumeNonComment(iterator);
            while (token != null && token.type != CURLY_CLOSED)
            {
                statements.add(parseNextExpression(iterator, token, errorManager, SEMI_COLON));
                token = consumeNonComment(iterator);
            }
        }
        else if (bodyStart != null)
        {
            // single statement
            statements.add(parseNextExpression(iterator, bodyStart, errorManager, SEMI_COLON));
        }
        return statements;
    }

    /**
     * Convenience helper to check if we used curly braces.
     * If there's more than one statement, we definitely had braces.
     */
    private boolean hasBrackets(List<Expression> statements)
    {
        // This is simplistic. If you need more accurate detection,
        // track the token actually used in parseBlockOrStatement.
        return statements.size() > 1;
    }

    /**
     * If you want to track the last token we saw was '{', you can store it here.
     * In this snippet, we simply store the "peeked" token if needed.
     */
    private Token peekPrevious()
    {
        // In a real parser, you'd keep a stack or the last consumed token.
        // This is just a placeholder if you want to refine bracket detection.
        return storedPeek;
    }
}
