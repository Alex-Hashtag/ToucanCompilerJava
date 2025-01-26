package org.alex_hashtag.internal_representation.ast;

import org.alex_hashtag.errors.ParsingErrorManager;
import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.tokenization.Coordinates;
import org.alex_hashtag.tokenization.Token;
import org.alex_hashtag.tokenization.TokenStream;
import org.alex_hashtag.tokenization.TokenType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.alex_hashtag.errors.ParsingErrorManager.*;
import static org.alex_hashtag.tokenization.TokenType.*;

public class AbstractSyntaxTree
{
    List<Macro> macros = new ArrayList<>();
    List<Annotation> annotations;
    List<Type> types;
    List<Function> functions;

    public AbstractSyntaxTree(List<TokenStream> tokenStreams)
    {
        initMacros(tokenStreams);
    }

    /**
     * First pass: parse macros from all TokenStreams
     */
    private void initMacros(List<TokenStream> tokenStreams)
    {
        for (TokenStream stream : tokenStreams)
        {
            ParsingErrorManager errorManager = new ParsingErrorManager(stream.getFilename(), stream.getSource());
            Iterator<Token> iterator = stream.iterator();

            while (iterator.hasNext())
            {
                Token current = iterator.next();

                // 1. Look for the 'macro' keyword
                if (current.type == MACRO)
                {
                    parseSingleMacro(iterator, current, errorManager, stream);
                }
            }
            // Print all collected errors for this file
            if (errorManager.hasErrors())
                errorManager.printErrors(System.out);
        }
    }

    /**
     * Parses a single macro definition after seeing the 'macro' keyword.
     */
    private void parseSingleMacro(Iterator<Token> iterator,
                                  Token macroKeyword,
                                  ParsingErrorManager errorManager,
                                  TokenStream stream)
    {
        Coordinates macroCoords = macroKeyword.coordinates;

        // Skip any intervening comments
        Token current = consumeNonComment(iterator);

        // 2. Next token must be an IDENTIFIER (the macro name)
        if (current == null || current.type != IDENTIFIER)
        {
            if (current != null)
            {
                errorManager.reportError(
                        ParsingError.withHint(
                                ErrorType.EXPECTED_FOUND,
                                ErrorType.EXPECTED_FOUND.getDescription(),
                                current.coordinates.row(),
                                current.coordinates.column(),
                                current.toString(),
                                "Give the macro a name, e.g. 'macro doSomething {...}'",
                                "identifier",
                                current.describeContents()
                        )
                );
            }
            return; // cannot proceed
        }

        String macroName = stream.getPackageName() + "." + current.internal.orElse("macroName");
        current = consumeNonComment(iterator);
        if (current == null || current.type != CURLY_OPEN)
        {
            if (current != null)
            {
                errorManager.reportError(
                        ParsingError.withHint(
                                ErrorType.EXPECTED_FOUND,
                                ErrorType.EXPECTED_FOUND.getDescription(),
                                current.coordinates.row(),
                                current.coordinates.column(),
                                current.toString(),
                                "Add '{' to start the macro body",
                                "'{'",
                                current.describeContents()
                        )
                );
            }
            return;
        }

        // Create the Macro object
        Macro macro = new Macro(macroCoords, macroName);

        // Now parse all arms until we see '}'
        parseMacroArms(iterator, errorManager, macro, stream);

        // Finally, store the completed macro
        this.macros.add(macro);
    }

    /**
     * Parse multiple macro arms until '}' or no more tokens.
     */
    private void parseMacroArms(Iterator<Token> iterator,
                                ParsingErrorManager errorManager,
                                Macro macro,
                                TokenStream stream)
    {
        while (true)
        {
            Token next = consumeNonComment(iterator);
            if (next == null) {
                // No more tokens => end of file, no more arms
                break;
            }

            if (next.type == CURLY_CLOSED) {
                // End of this macro
                break;
            }

            // We expect '(' to start an arm
            if (next.type != BRACE_OPEN)
            {
                errorManager.reportError(
                        ParsingError.withHint(
                                ErrorType.EXPECTED_FOUND,
                                ErrorType.EXPECTED_FOUND.getDescription(),
                                next.coordinates.row(),
                                next.coordinates.column(),
                                next.toString(),
                                "Each macro arm must start with '(' or '}' to end the macro",
                                "'('",
                                next.describeContents()
                        )
                );
                // We can attempt to skip or break
                // Let's break so we don't spam errors for each token
                break;
            }

            // Parse the pattern until ')'
            Macro.Pattern pattern = parseMacroPattern(iterator, errorManager, stream);

            // Next token must be '->'
            Token arrow = consumeNonComment(iterator);
            if (arrow == null || arrow.type != ARROW)
            {
                if (arrow != null)
                {
                    errorManager.reportError(
                            ParsingError.withHint(
                                    ErrorType.EXPECTED_FOUND,
                                    ErrorType.EXPECTED_FOUND.getDescription(),
                                    arrow.coordinates.row(),
                                    arrow.coordinates.column(),
                                    arrow.toString(),
                                    "Missing '->' after macro pattern",
                                    "'->'",
                                    arrow.describeContents()
                            )
                    );
                }
                // skip this arm
                continue;
            }

            // Next token must be '{' for the body
            Token openBody = consumeNonComment(iterator);
            if (openBody == null || openBody.type != CURLY_OPEN)
            {
                if (openBody != null)
                {
                    errorManager.reportError(
                            ParsingError.withHint(
                                    ErrorType.EXPECTED_FOUND,
                                    ErrorType.EXPECTED_FOUND.getDescription(),
                                    openBody.coordinates.row(),
                                    openBody.coordinates.column(),
                                    openBody.toString(),
                                    "Missing '{' to start macro arm’s body",
                                    "'{'",
                                    openBody.describeContents()
                            )
                    );
                }
                // skip
                continue;
            }

            // Gather all tokens in the body until the matching '}'
            List<Token> bodyTokens = parseBlock(iterator, errorManager);

            // Create a TokenStream from the body tokens
            TokenStream bodyStream = new TokenStream(stream.getFilename(), stream.getSource(), bodyTokens);

            // Add the new arm to the macro
            macro.addArm(pattern, bodyStream);
        }
    }

    /**
     * Parse the macro pattern after we've consumed '('.
     * Reads tokens until the matching ')'.
     */
    private Macro.Pattern parseMacroPattern(Iterator<Token> iterator,
                                            ParsingErrorManager errorManager,
                                            TokenStream stream)
    {
        List<Macro.Pattern.PatternElement> elements = new ArrayList<>();
        int parenLevel = 1;

        while (iterator.hasNext() && parenLevel > 0)
        {
            Token current = iterator.next();
            if (current.type == COMMENT) {
                // skip
                continue;
            }
            else if (current.type == BRACE_OPEN) {
                parenLevel++;
            }
            else if (current.type == BRACE_CLOSED) {
                parenLevel--;
                if (parenLevel == 0) break;
            }
            else if (current.type == COMMA) {
                // just a separator
                continue;
            }
            else if (current.type == MACRO_REPEAT_OPEN) {
                // we saw `$(`
                Macro.Pattern subPat = parseSubPatternInsideRepetition(iterator, errorManager);


                Token maybeComma = peekNextNonComment(iterator);
                String separator = null;
                Macro.Pattern.RepetitionKind repKind = Macro.Pattern.RepetitionKind.ZERO_OR_MORE;

                if (maybeComma != null && maybeComma.type == COMMA) {
                    // consume
                    consumeNonComment(iterator);

                    // next might be '+', '*', '?', etc.
                    Token repSym = peekNextNonComment(iterator);
                    if (repSym != null) {
                        if (repSym.type == ADDITION) {
                            repKind = Macro.Pattern.RepetitionKind.ONE_OR_MORE;
                            consumeNonComment(iterator);
                        }
                        else if (repSym.type == MULTIPLICATION) {
                            repKind = Macro.Pattern.RepetitionKind.ZERO_OR_MORE;
                            consumeNonComment(iterator);
                        }
                        // etc. for '?'
                    }
                    separator = ",";
                }
                Macro.Pattern.RepetitionElement repElem =
                        new Macro.Pattern.RepetitionElement(subPat, repKind, separator);
                elements.add(repElem);
            }
            else {
                // Check for e.g. "expression $x"
                if (isMacroTypeKeyword(current)) {
                    Token varToken = consumeNonComment(iterator);
                    if (varToken == null || varToken.type != MACRO_VARIABLE) {
                        if (varToken != null) {
                            errorManager.reportError(
                                    ParsingError.withHint(
                                            ErrorType.EXPECTED_FOUND,
                                            ErrorType.EXPECTED_FOUND.getDescription(),
                                            varToken.coordinates.row(),
                                            varToken.coordinates.column(),
                                            varToken.toString(),
                                            "Expected a macro variable (e.g. $x) after macro type keyword",
                                            "MACRO_VARIABLE",
                                            varToken.describeContents()
                                    )
                            );
                        }
                        continue;
                    }
                    Macro.Pattern.MacroVarType varType =
                            convertToMacroVarType(current.internal.orElse(""));
                    String varName = varToken.internal.orElse("$");
                    elements.add(new Macro.Pattern.VariableElement(varName, varType));
                }
                else if (current.type == MACRO_VARIABLE) {
                    // e.g. `$foo` => default to expression or treat as error
                    String varName = current.internal.orElse("$");
                    elements.add(new Macro.Pattern.VariableElement(
                            varName, Macro.Pattern.MacroVarType.EXPRESSION));
                }
                else {
                    // treat as literal
                    elements.add(new Macro.Pattern.LiteralElement(
                            current.internal.orElse(current.toString())));
                }
            }
        }
        return new Macro.Pattern(elements);
    }

    /**
     * Parse sub-pattern inside `$( ... )`.
     * We stop on the first unmatched ')' or when level goes to 0.
     */
    private Macro.Pattern parseSubPatternInsideRepetition(Iterator<Token> iterator,
                                                          ParsingErrorManager errorManager)
    {
        List<Macro.Pattern.PatternElement> subEls = new ArrayList<>();
        int level = 1; // we are already inside $(

        while (iterator.hasNext() && level > 0)
        {
            Token current = iterator.next();
            if (current.type == COMMENT) {
                continue;
            }
            else if (current.type == BRACE_OPEN) {
                level++;
            }
            else if (current.type == BRACE_CLOSED) {
                level--;
                if (level == 0) break;
            }
            else if (current.type == COMMA) {
                continue;
            }
            else {
                if (isMacroTypeKeyword(current)) {
                    Token varToken = consumeNonComment(iterator);
                    if (varToken == null || varToken.type != MACRO_VARIABLE) {
                        if (varToken != null) {
                            errorManager.reportError(
                                    ParsingError.withHint(
                                            ErrorType.EXPECTED_FOUND,
                                            ErrorType.EXPECTED_FOUND.getDescription(),
                                            varToken.coordinates.row(),
                                            varToken.coordinates.column(),
                                            varToken.toString(),
                                            "Expected a macro variable after macro type keyword inside $(...)",
                                            "MACRO_VARIABLE",
                                            varToken.describeContents()
                                    )
                            );
                        }
                        continue;
                    }
                    Macro.Pattern.MacroVarType vt =
                            convertToMacroVarType(current.internal.orElse(""));
                    String varName = varToken.internal.orElse("$");
                    subEls.add(new Macro.Pattern.VariableElement(varName, vt));
                }
                else if (current.type == MACRO_VARIABLE) {
                    String varName = current.internal.orElse("$");
                    subEls.add(new Macro.Pattern.VariableElement(
                            varName, Macro.Pattern.MacroVarType.EXPRESSION));
                }
                else {
                    // literal
                    subEls.add(new Macro.Pattern.LiteralElement(
                            current.internal.orElse(current.toString())));
                }
            }
        }
        return new Macro.Pattern(subEls);
    }

    /**
     * Parse a `{ ... }` block and return all tokens inside.
     * If nested braces are encountered, handle them so we only stop at the matching brace.
     */
    private List<Token> parseBlock(Iterator<Token> iterator, ParsingErrorManager errorManager)
    {
        List<Token> blockTokens = new ArrayList<>();
        int braceCount = 1; // We already consumed one '{'

        while (iterator.hasNext() && braceCount > 0)
        {
            Token t = iterator.next();
            if (t.type == CURLY_OPEN) {
                braceCount++;
                blockTokens.add(t);
            }
            else if (t.type == CURLY_CLOSED) {
                braceCount--;
                if (braceCount > 0) {
                    blockTokens.add(t);
                }
            }
            else {
                blockTokens.add(t);
            }
        }
        return blockTokens;
    }

    /**
     * Helper to skip over comments and return the next real token, or null.
     */
    private Token consumeNonComment(Iterator<Token> iterator)
    {
        while (iterator.hasNext()) {
            Token t = iterator.next();
            if (t.type != COMMENT) {
                return t;
            }
        }
        return null;
    }

    /**
     * A minimal "peek" approach to see the next non-comment token
     * without consuming it from the iterator. (Implementation details vary.)
     */
    private Token peekNextNonComment(Iterator<Token> iterator)
    {
        if (!iterator.hasNext()) return null;

        // We must actually consume the next token and store it
        Token t = iterator.next();
        while (t.type == COMMENT && iterator.hasNext()) {
            t = iterator.next();
        }

        // We can't truly "un-consume" a normal Iterator,
        // so you'd need to keep a small buffer or use a ListIterator, etc.
        // For demonstration, we wrap the single token in a small buffer.
        // A robust approach: gather tokens in a small queue. We'll do a hack here:

        storedPeek = t; // store it in a static or instance field
        hasPeek = true;
        return t;
    }

    // small fields for the hacky peek
    private Token storedPeek = null;
    private boolean hasPeek = false;

    private Token consumePeekIfAny(Iterator<Token> iterator)
    {
        if (hasPeek) {
            hasPeek = false;
            return storedPeek;
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Converts a macro "type keyword" like "expression" to the relevant enum.
     */
    private boolean isMacroTypeKeyword(Token t)
    {
        if (t.internal.isEmpty()) return false;
        String val = t.internal.get();
        return val.equals("expression") || val.equals("type") || val.equals("identifier");
    }

    private Macro.Pattern.MacroVarType convertToMacroVarType(String s)
    {
        return switch (s) {
            case "expression" -> Macro.Pattern.MacroVarType.EXPRESSION;
            case "type"       -> Macro.Pattern.MacroVarType.TYPE;
            case "identifier" -> Macro.Pattern.MacroVarType.IDENTIFIER;
            default -> Macro.Pattern.MacroVarType.EXPRESSION;
        };
    }
}
