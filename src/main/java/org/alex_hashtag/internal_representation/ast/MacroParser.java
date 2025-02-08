package org.alex_hashtag.internal_representation.macros;

import org.alex_hashtag.errors.ParsingErrorManager;
import org.alex_hashtag.errors.ParsingErrorManager.ErrorType;
import org.alex_hashtag.errors.ParsingErrorManager.ParsingError;
import org.alex_hashtag.tokenization.Coordinates;
import org.alex_hashtag.tokenization.Token;
import org.alex_hashtag.tokenization.TokenStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.alex_hashtag.tokenization.TokenType.*;


public class MacroParser
{

    // -- A small buffer for single-token "peeking" logic --
    private Token storedPeek = null;
    private boolean hasPeek = false;

    /**
     * Public entry point. Parses all macros from the given list of TokenStreams,
     * returning a combined List of macros.
     */
    public List<Macro> parseAllMacros(List<TokenStream> tokenStreams)
    {
        List<Macro> allMacros = new ArrayList<>();
        for (TokenStream stream : tokenStreams)
        {
            ParsingErrorManager errorManager = new ParsingErrorManager(stream.getFilename(), stream.getSource());
            Iterator<Token> iterator = stream.iterator();

            boolean isPublic = false; // Flag to track if the next macro is public

            while (iterator.hasNext())
            {
                Token current = iterator.next();

                if (current.type == PUBLIC)
                {
                    isPublic = true;
                    continue;
                }

                // Look for the 'macro' keyword
                if (current.type == MACRO)
                {
                    parseSingleMacro(iterator, current, errorManager, stream, isPublic, allMacros);
                    isPublic = false; // Reset after processing
                }
                else
                {
                    // If we saw something else, reset the "public" flag
                    isPublic = false;
                }
            }

            // Print all collected errors for this file (if any)
            if (errorManager.hasErrors())
            {
                errorManager.printErrors(System.out);
            }
        }
        return allMacros;
    }

    /**
     * Parses a single macro definition after seeing the 'macro' keyword.
     */
    private void parseSingleMacro(Iterator<Token> iterator,
                                  Token macroKeyword,
                                  ParsingErrorManager errorManager,
                                  TokenStream stream,
                                  boolean isPublic,
                                  List<Macro> resultList)
    {
        Coordinates macroCoords = macroKeyword.coordinates;

        // Skip any intervening comments
        Token current = consumeNonComment(iterator);

        // Next token must be IDENTIFIER (the macro name)
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
                                "Provide a macro name, e.g., 'macro buildGreeting { ... }'",
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
            return;
        }

        // Create the Macro object
        Macro macro = new Macro(macroCoords, macroName, isPublic);

        // Parse all arms until we see '}'
        parseMacroArms(iterator, errorManager, macro, stream);

        // Store the completed macro
        resultList.add(macro);
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
            if (next == null) // End of file, no more arms
                break;
            if (next.type == CURLY_CLOSED) // End of this macro
                break;

            // We expect '(' to start an arm
            if (next.type != BRACE_OPEN)
            { // Assuming BRACE_OPEN is '('
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

                // Break to avoid spamming errors
                break;
            }

            // Parse the pattern until ')'
            Macro.Pattern pattern = parseMacroPattern(iterator, errorManager, stream);

            // Next token must be '->'
            Token arrow = consumeNonComment(iterator);

            if (arrow == null || arrow.type != ARROW)
            {
                if (arrow != null)
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

                // Skip this arm
                continue;
            }

            // Next token must be '{' for the body
            Token openBody = consumeNonComment(iterator);
            if (openBody == null || openBody.type != CURLY_OPEN)
            {
                if (openBody != null)
                    errorManager.reportError(
                            ParsingError.withHint(
                                    ErrorType.EXPECTED_FOUND,
                                    ErrorType.EXPECTED_FOUND.getDescription(),
                                    openBody.coordinates.row(),
                                    openBody.coordinates.column(),
                                    openBody.toString(),
                                    "Missing '{' to start macro arm's body",
                                    "'{'",
                                    openBody.describeContents()
                            )
                    );

                // Skip this arm
                continue;
            }

            // Gather all tokens in the body until the matching '}'
            List<Token> bodyTokens = parseBlock(iterator);

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
            if (current.type == COMMENT) // Skip comments
                continue;
            else if (current.type == BRACE_OPEN)
            { // '('
                parenLevel++;
                elements.add(new Macro.Pattern.LiteralElement("("));
            }
            else if (current.type == BRACE_CLOSED)
            { // ')'
                parenLevel--;
                if (parenLevel == 0)
                    break;
                elements.add(new Macro.Pattern.LiteralElement(")"));
            }
            else if (current.type == COMMA)
            { // Just a separator
                elements.add(new Macro.Pattern.LiteralElement(","));
                continue;
            }
            else if (current.type == MACRO_REPEAT_OPEN)
            { // '$('
                // Detected `$(`
                Macro.Pattern subPat = parseSubPatternInsideRepetition(iterator, errorManager);

                // Check for repetition kind and separator
                String separator = null;
                Macro.Pattern.RepetitionKind repKind = Macro.Pattern.RepetitionKind.ZERO_OR_MORE;

                Token nextToken = peekNextNonComment(iterator);
                if (nextToken != null)
                {
                    if (nextToken.type == QUESTION)
                    { // Optional
                        repKind = Macro.Pattern.RepetitionKind.ZERO_OR_ONE;
                        consumeNonComment(iterator); // Consume '?'
                    }
                    else if (nextToken.type == ADDITION)
                    { // One or more
                        repKind = Macro.Pattern.RepetitionKind.ONE_OR_MORE;
                        consumeNonComment(iterator); // Consume '+'
                    }
                    // Add more repetition kinds if needed
                }

                // For simplicity, assuming comma as separator if applicable
                // You can modify this as per your syntax rules
                separator = ",";

                Macro.Pattern.RepetitionElement repElem =
                        new Macro.Pattern.RepetitionElement(subPat, repKind, separator);
                elements.add(repElem);
            }
            else if (isMacroTypeKeyword(current))
            {
                // Handle typed macro variables like 'expression', 'type', 'identifier'
                Macro.Pattern.MacroVarType varType = convertToMacroVarType(current);
                Token varToken = consumeNonComment(iterator);
                if (varToken == null || varToken.type != MACRO_VARIABLE)
                {
                    if (varToken != null)
                    {
                        errorManager.reportError(
                                ParsingError.withHint(
                                        ErrorType.EXPECTED_FOUND,
                                        ErrorType.EXPECTED_FOUND.getDescription(),
                                        varToken.coordinates.row(),
                                        varToken.coordinates.column(),
                                        varToken.toString(),
                                        "Expected a macro variable (e.g., $name) after type keyword",
                                        "MACRO_VARIABLE",
                                        varToken.describeContents()
                                )
                        );
                    }
                    continue;
                }
                String varName = varToken.internal.orElse("$");
                elements.add(new Macro.Pattern.VariableElement(varName, varType));
            }
            else if (current.type == MACRO_VARIABLE)
            {
                // Untyped macro variable, defaulting to EXPRESSION
                String varName = current.internal.orElse("$");
                elements.add(new Macro.Pattern.VariableElement(
                        varName, Macro.Pattern.MacroVarType.EXPRESSION));
            }
            else
            { // Treat as literal
                elements.add(new Macro.Pattern.LiteralElement(
                        current.internal.orElse(current.toString())));
            }
        }

        // After parsing the pattern, check if next token is '?' for optional pattern
        // (Handled above in repetition kind)

        return new Macro.Pattern(elements);
    }

    /**
     * Parse sub-pattern inside `$( ... )`.
     * We stop on the left unmatched ')' or when level goes to 0.
     */
    private Macro.Pattern parseSubPatternInsideRepetition(Iterator<Token> iterator,
                                                          ParsingErrorManager errorManager)
    {
        List<Macro.Pattern.PatternElement> subElements = new ArrayList<>();
        int level = 1; // Already inside '$('

        while (iterator.hasNext() && level > 0)
        {
            Token current = iterator.next();
            if (current.type == COMMENT) // Skip comments
                continue;
            else if (current.type == BRACE_OPEN)
            { // '('
                level++;
                subElements.add(new Macro.Pattern.LiteralElement("("));
            }
            else if (current.type == BRACE_CLOSED)
            { // ')'
                level--;
                if (level == 0)
                    break;
                subElements.add(new Macro.Pattern.LiteralElement(")"));
            }
            else if (current.type == COMMA)
            {
                subElements.add(new Macro.Pattern.LiteralElement(","));
                continue;
            }
            else if (isMacroTypeKeyword(current))
            {
                // Handle typed macro variables
                Macro.Pattern.MacroVarType varType = convertToMacroVarType(current);
                Token varToken = consumeNonComment(iterator);
                if (varToken == null || varToken.type != MACRO_VARIABLE)
                {
                    if (varToken != null)
                    {
                        errorManager.reportError(
                                ParsingError.withHint(
                                        ErrorType.EXPECTED_FOUND,
                                        ErrorType.EXPECTED_FOUND.getDescription(),
                                        varToken.coordinates.row(),
                                        varToken.coordinates.column(),
                                        varToken.toString(),
                                        "Expected a macro variable (e.g., $name) after type keyword inside $(...)",
                                        "MACRO_VARIABLE",
                                        varToken.describeContents()
                                )
                        );
                    }
                    continue;
                }
                String varName = varToken.internal.orElse("$");
                subElements.add(new Macro.Pattern.VariableElement(varName, varType));
            }
            else if (current.type == MACRO_VARIABLE)
            {
                // Untyped macro variable, defaulting to EXPRESSION
                String varName = current.internal.orElse("$");
                subElements.add(new Macro.Pattern.VariableElement(
                        varName, Macro.Pattern.MacroVarType.EXPRESSION));
            }
            else
            { // Treat as literal
                subElements.add(new Macro.Pattern.LiteralElement(
                        current.internal.orElse(current.toString())));
            }
        }

        return new Macro.Pattern(subElements);
    }

    /**
     * Parse a `{ ... }` block and return all tokens inside.
     * If nested braces are encountered, handle them so we only stop at the matching brace.
     */
    private List<Token> parseBlock(Iterator<Token> iterator)
    {
        List<Token> blockTokens = new ArrayList<>();
        int braceCount = 1; // We already consumed one '{'

        while (iterator.hasNext() && braceCount > 0)
        {
            Token t = iterator.next();
            if (t.type == CURLY_OPEN)
            {
                braceCount++;
                blockTokens.add(t);
            }
            else if (t.type == CURLY_CLOSED)
            {
                braceCount--;
                if (braceCount > 0)
                    blockTokens.add(t);
            }
            else
            {
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

    /**
     * Converts a macro "type keyword" like "expression" to the relevant enum.
     */
    private Macro.Pattern.MacroVarType convertToMacroVarType(Token t)
    {
        String s = t.internal.orElse("");
        return switch (s)
        {
            case "type" -> Macro.Pattern.MacroVarType.TYPE;
            case "identifier" -> Macro.Pattern.MacroVarType.IDENTIFIER;
            default -> Macro.Pattern.MacroVarType.EXPRESSION;
        };
    }

    /**
     * Checks if a token is a macro type keyword.
     */
    private boolean isMacroTypeKeyword(Token t)
    {
        if (t.internal.isEmpty())
            return false;
        String val = t.internal.get();
        return val.equals("expression") || val.equals("type") || val.equals("identifier");
    }
}
