package org.alex_hashtag.internal_representation.ast;

import lombok.Getter;
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
import java.util.Objects;

import static org.alex_hashtag.tokenization.TokenType.*;


public class ExpressionParser
{
    // -- A small buffer for single-token "peeking" logic --
    private Token storedPeek = null;
    private boolean hasPeek = false;

    public Expression parseNextExpression(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager, TokenType closer)
    {
        Coordinates start = current.coordinates;

        List<Pair<Expression, Token>> expressions = new ArrayList<>();
        Expression lastExpr= null;
        Token closingSymbol = null;
        while (current.type!=closer)
        {


            lastExpr = switch (current.type)
            {
                case IF -> parseIf(iterator, current, errorManager);
                case FOR -> parseForLoop(iterator, current, errorManager);
                case WHILE -> parseWhileLoop(iterator, current, errorManager);
                case DO -> parseDoWhileLoop(iterator, current, errorManager);
                case LOOP -> parseLoop(iterator, current, errorManager);
                case BREAK -> new BreakExpression(current.coordinates);
                case CONTINUE -> new ContinueExpression(current.coordinates);
                case ECHO -> new EchoExpression(current.coordinates, parseNextExpression(iterator, current, errorManager, closer));
                case RETURN -> new ReturnExpression(current.coordinates, parseNextExpression(iterator, current, errorManager, closer));
                case YIELD -> new YieldExpression(current.coordinates, parseNextExpression(iterator, current, errorManager, closer));
                case SIZEOF -> new SizeOfExpression(current.coordinates, parseNextExpression(iterator, current, errorManager, closer));
                case TYPEOF -> new TypeOfExpression(current.coordinates, parseNextExpression(iterator, current, errorManager, closer));
//                case SWITCH -> parseSwitch(iterator, current, errorManager);
                case BRACE_OPEN -> parseNextExpression(iterator, current, errorManager, BRACE_CLOSED);
                case CURLY_OPEN -> parseScope(iterator, current, errorManager);
                case UNSAFE -> parseUnsafe(iterator, current, errorManager);
                case INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, RUNE_LITERAL , STRING_LITERAL -> parseBasicLiteral(iterator, current, errorManager);
//                case IDENTIFIER -> parseWhateverTheFuckThisMightBe(iterator, current, errorManager);
                default -> null;
            };

             closingSymbol = peekNextNonComment(iterator);

            expressions.add(Pair.create(lastExpr, closingSymbol));
        }

        Expression result = expressions.getFirst().getLeft();

        if (expressions.isEmpty())
            return new EmptyExpression(start);


        for (Pair<Token, Expression> junction : ListUtils.flipAndTrim(expressions))
        {
            Operator op = Operator.fromToken(junction.getLeft());
            if (op!=null)
                result = new BinaryExpression(result, start, op, junction.getRight());
            else if (junction.getLeft().type==DOT)
            {
                if (junction.getRight() instanceof UnitExpression)
                    result = new FieldAccessExpression(((UnitExpression) junction.getRight()).getIdentifier(), result, start);
                else if (junction.getRight() instanceof FunctionInvokationExpression)
                    ;
            }
        }


        return result;
    }

    private Expression parseForLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        current = consumeNonComment(iterator);
        if (current.type!=BRACE_OPEN)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_OPEN.regex,
                            current.describeContents()
                    )
            );

        current = consumeNonComment(iterator);
        List<Expression> initialization = new ArrayList<>();
        initialization.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
        ForExpression.LoopType type = switch ((Objects.requireNonNull(current = consumeNonComment(iterator))).type)
        {
            case SEMI_COLON, COMMA -> ForExpression.LoopType.STANDARD;
            case COLON -> ForExpression.LoopType.FOREACH;
            default -> {
                errorManager.reportError(
                        ParsingErrorManager.ParsingError.of(
                                ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                                ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                                current.coordinates.row(),
                                current.coordinates.column(),
                                current.toString(),
                                "comma, colon or semicolon",
                                current.describeContents()
                        ));
                yield null;
            }
        };



        current = consumeNonComment(iterator);
        if (current.type!=BRACE_CLOSED)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_CLOSED.regex,
                            current.describeContents()
                    )
            );

        return null;
    }

    /**
     * Parses a `loop` expression. The grammar is assumed to allow either:
     *
     *  1) loop { ... }                    // indefinite loop, or
     *  2) loop(expression) { ... }        // loop that runs `expression` times.
     *
     *  In both cases, either a block `{ ... }` or a single statement is permitted for the body.
     */
    private Expression parseLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager) {
        // `current` is the 'loop' token
        Coordinates start = current.coordinates;

        // We'll peek the next token to check if it is '(' for an iteration count.
        Token next = peekNextNonComment(iterator);
        Expression iterationCount = null;

        // If the next token is a BRACE_OPEN '(', parse the expression inside ( ... ).
        if (next != null && next.type == BRACE_OPEN) {
            // Consume '('
            consumeNonComment(iterator);
            // Parse the iteration count
            Token exprToken = consumeNonComment(iterator);
            iterationCount = parseNextExpression(iterator, exprToken, errorManager, BRACE_CLOSED);

            // Consume the closing ')'
            Token closingParen = consumeNonComment(iterator);
            if (closingParen.type != BRACE_CLOSED) {
                errorManager.reportError(
                        ParsingErrorManager.ParsingError.of(
                                ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                                "Expected ')' after loop iteration count.",
                                closingParen.coordinates.row(),
                                closingParen.coordinates.column(),
                                closingParen.toString(),
                                BRACE_CLOSED.regex,
                                closingParen.describeContents()
                        )
                );
                // Fall back to an empty expression to continue gracefully:
                iterationCount = new EmptyExpression(start);
            }
        }

        // Next, parse the loop body:
        //   - If the next token is '{', parse a block
        //   - Otherwise, parse a single expression (e.g. a single statement)
        Token bodyStart = consumeNonComment(iterator);
        boolean hasBrackets = (bodyStart.type == CURLY_OPEN);
        List<Expression> statements;

        if (hasBrackets) {
            // parse everything inside { ... }
            statements = new ArrayList<>();
            Token bodyToken = consumeNonComment(iterator);
            while (bodyToken.type != CURLY_CLOSED) {
                statements.add(parseNextExpression(iterator, bodyToken, errorManager, SEMI_COLON));
                bodyToken = consumeNonComment(iterator);
            }
        } else {
            // single statement (no curly braces)
            Expression singleStmt = parseNextExpression(iterator, bodyStart, errorManager, SEMI_COLON);
            statements = List.of(singleStmt);
        }

        // Construct the LoopExpression, either with or without an iteration count
        if (iterationCount == null) {
            return new LoopExpression(start, statements, hasBrackets);
        } else {
            return new LoopExpression(iterationCount, statements, start, hasBrackets);
        }
    }

    private Expression parseUnsafe(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;

        current = consumeNonComment(iterator);

        boolean brackets = false;

        if (current.type==CURLY_OPEN)
        {
            current = consumeNonComment(iterator);
            brackets = true;
        }

        List<Expression> statements = new ArrayList<>();

        if (!brackets)
            statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
        else
        {
            while (current.type!=CURLY_CLOSED)
            {
                statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
                current = consumeNonComment(iterator);
            }
        }

        return new UnsafeExpression(statements, start, brackets);
    }

    private Expression parseDoWhileLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;

        current = consumeNonComment(iterator);

        boolean brackets = false;

        if (current.type==CURLY_OPEN)
        {
            current = consumeNonComment(iterator);
            brackets = true;
        }

        List<Expression> statements = new ArrayList<>();

        if (!brackets)
            statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
        else
        {
            while (current.type!=CURLY_CLOSED)
            {
                statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
                current = consumeNonComment(iterator);
            }
        }

        current = consumeNonComment(iterator);
        if (current.type!=WHILE)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            WHILE.regex,
                            current.describeContents()
                    )
            );

        current = consumeNonComment(iterator);
        if (current.type!=BRACE_OPEN)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_OPEN.regex,
                            current.describeContents()
                    )
            );

        current = consumeNonComment(iterator);
        Expression condition = parseNextExpression(iterator, current, errorManager, BRACE_CLOSED);

        current = consumeNonComment(iterator);
        assert current != null;
        if (current.type!=BRACE_CLOSED)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_CLOSED.regex,
                            current.describeContents()
                    )
            );



        return new WhileExpression(condition, statements, start, brackets);
    }

    private Expression parseWhileLoop(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        current = consumeNonComment(iterator);
        if (current.type!=BRACE_OPEN)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_OPEN.regex,
                            current.describeContents()
                    )
            );

        current = consumeNonComment(iterator);
        Expression condition = parseNextExpression(iterator, current, errorManager, BRACE_CLOSED);

        current = consumeNonComment(iterator);
        if (current.type!=BRACE_CLOSED)
            errorManager.reportError(
                    ParsingErrorManager.ParsingError.of(
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                            ParsingErrorManager.ErrorType.EXPECTED_FOUND.getDescription(),
                            current.coordinates.row(),
                            current.coordinates.column(),
                            current.toString(),
                            BRACE_CLOSED.regex,
                            current.describeContents()
                    )
            );

        current = consumeNonComment(iterator);

        boolean brackets = false;

        if (current.type==CURLY_OPEN)
        {
            current = consumeNonComment(iterator);
            brackets = true;
        }

        List<Expression> statements = new ArrayList<>();

        if (!brackets)
            statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
        else
        {
            while (current.type!=CURLY_CLOSED)
            {
                statements.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
                current = consumeNonComment(iterator);
            }
        }

        return new WhileExpression(condition, statements, start, brackets);
    }

    private Expression parseScope(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager)
    {
        Coordinates start = current.coordinates;
        List<Expression> expressions = new ArrayList<>();


        current = consumeNonComment(iterator);
        while (current.type!=CURLY_CLOSED)
        {
            expressions.add(parseNextExpression(iterator, current, errorManager, SEMI_COLON));
            current = consumeNonComment(iterator);
        }

        return new ScopeExpression(start, expressions);
    }

    /**
     * Parses an if-statement (or an else-if branch) with an optional else.
     */
    private Expression parseIf(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager) {
        Coordinates start = current.coordinates;

        // Consume the opening parenthesis (assumed to be a BRACE_OPEN token)
        Token openParen = consumeNonComment(iterator);
        if (openParen.type != BRACE_OPEN) {
            errorManager.reportError(ParsingErrorManager.ParsingError.of(
                    ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                    "Expected '(' after 'if'.",
                    openParen.coordinates.row(),
                    openParen.coordinates.column(),
                    openParen.toString(),
                    BRACE_OPEN.regex,
                    openParen.describeContents()
            ));
        }

        // Parse the if-condition
        Expression condition = parseNextExpression(iterator, consumeNonComment(iterator), errorManager, BRACE_CLOSED);

        // Consume the closing parenthesis (assumed to be a BRACE_CLOSED token)
        Token closeParen = consumeNonComment(iterator);
        if (closeParen.type != BRACE_CLOSED) {
            errorManager.reportError(ParsingErrorManager.ParsingError.of(
                    ParsingErrorManager.ErrorType.EXPECTED_FOUND,
                    "Expected ')' after if-condition.",
                    closeParen.coordinates.row(),
                    closeParen.coordinates.column(),
                    closeParen.toString(),
                    BRACE_CLOSED.regex,
                    closeParen.describeContents()
            ));
        }

        // Parse the if-body: either a block enclosed in { } or a single statement.
        Token bodyToken = consumeNonComment(iterator);
        boolean hasBlock = (bodyToken.type == CURLY_OPEN);
        List<Expression> statements = hasBlock
                ? parseBlock(iterator, errorManager)
                : List.of(parseNextExpression(iterator, bodyToken, errorManager, SEMI_COLON));

        // Check for an else branch.
        IfExpression elseExpr = null;
        Token next = peekNextNonComment(iterator);
        if (next.type == ELSE) {
            consumeNonComment(iterator); // Consume the ELSE token.
            Token possibleIf = peekNextNonComment(iterator);
            if (possibleIf.type == IF) {
                // Handle else-if recursively.
                consumeNonComment(iterator); // Consume the IF token.
                elseExpr = (IfExpression) parseIf(iterator, possibleIf, errorManager);
                elseExpr.setElse(true);  // Mark as "else if".
            } else {
                // Plain else: either a block or a single statement.
                Token elseBody = consumeNonComment(iterator);
                boolean elseHasBlock = (elseBody.type == CURLY_OPEN);
                List<Expression> elseStatements = elseHasBlock
                        ? parseBlock(iterator, errorManager)
                        : List.of(parseNextExpression(iterator, elseBody, errorManager, SEMI_COLON));
                elseExpr = new IfExpression(start, null, elseStatements, null, elseHasBlock, true);
            }
        }

        // Construct and return the IfExpression.
        return new IfExpression(
                start,
                condition instanceof BinaryExpression ? (BinaryExpression) condition : null,
                statements,
                elseExpr,
                hasBlock,
                false
        );
    }


    /**
     * Parses a basic literal (INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, RUNE_LITERAL, or STRING_LITERAL)
     * into a LiteralExpression.
     */
    private Expression parseBasicLiteral(Iterator<Token> iterator, Token current, ParsingErrorManager errorManager) {
        Coordinates start = current.coordinates;
        String rawText = current.internal.orElse("");
        Literal literal;

        switch (current.type) {
            case INT_LITERAL -> {
                IntegerLiteral intLit = new IntegerLiteral();
                intLit.setLocation(start);
                intLit.setSizeInBytes((byte) 4); // default to 4 bytes (32-bit)
                intLit.setUnsigned(false);
                intLit.setInternal(rawText);
                literal = intLit;
            }
            case FLOAT_LITERAL -> {
                FloatLiteral floatLit = new FloatLiteral();
                floatLit.setLocation(start);
                floatLit.setSizeInBytes((byte) 4); // default to 32-bit float
                floatLit.setInternal(rawText);
                literal = floatLit;
            }
            case CHAR_LITERAL -> {
                CharLiteral charLit = new CharLiteral();
                charLit.setLocation(start);
                charLit.setValue(rawText);
                literal = charLit;
            }
            case RUNE_LITERAL -> {
                RuneLiteral runeLit = new RuneLiteral();
                runeLit.setLocation(start);
                // In a real parser you’d convert rawText into a numeric rune value.
                runeLit.setValue(0);
                literal = runeLit;
            }
            case STRING_LITERAL -> {
                StringLiteral strLit = new StringLiteral();
                strLit.setLocation(start);
                strLit.setValue(rawText);
                literal = strLit;
            }
            default -> {
                errorManager.reportError(ParsingErrorManager.ParsingError.of(
                        ParsingErrorManager.ErrorType.SYNTAX_ERROR,
                        ParsingErrorManager.ErrorType.SYNTAX_ERROR.getDescription(),
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

    /* ===== Helper Methods ===== */

    /**
     * Parses a block: consumes tokens from a CURLY_OPEN up to the matching CURLY_CLOSED.
     * Assumes the opening '{' has already been consumed.
     */
    private List<Expression> parseBlock(Iterator<Token> iterator, ParsingErrorManager errorManager) {
        List<Expression> statements = new ArrayList<>();
        Token token = consumeNonComment(iterator);
        while (token.type != CURLY_CLOSED) {
            statements.add(parseNextExpression(iterator, token, errorManager, SEMI_COLON));
            token = consumeNonComment(iterator);
        }
        return statements;
    }


    /**
     * Splits the given token list by SEMI_COLON tokens up to maxSplits times.
     */
    private List<List<Token>> splitBySemicolons(List<Token> tokens, int maxSplits) {
        List<List<Token>> segments = new ArrayList<>();
        List<Token> currentSegment = new ArrayList<>();
        int splitsSoFar = 0;
        for (Token t : tokens) {
            if (t.type == SEMI_COLON && splitsSoFar < maxSplits) {
                segments.add(currentSegment);
                currentSegment = new ArrayList<>();
                splitsSoFar++;
            } else {
                currentSegment.add(t);
            }
        }
        segments.add(currentSegment);
        return segments;
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
