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


    /**
     * First pass: Go through the token streams (of different files) and first get all the macros
     * Second pass: Populate annotations, types and functions (Macros will be processed accordingly)
     * Execute annotations on different types and functions
     * Populate the TypeRegistry
     * Check type constraints
     */
    public AbstractSyntaxTree(List<TokenStream> tokenStreams)
    {
        initMacros(tokenStreams);
    }

    private void initMacros(List<TokenStream> tokenStreams)
    {
        for (TokenStream stream : tokenStreams)
        {
            ParsingErrorManager errorManager = new ParsingErrorManager(stream.getFilename(), stream.getSource());
            Iterator<Token> iterator = stream.iterator();
            Token current;

            while (iterator.hasNext())
            {
                current = iterator.next();

                // 1. Look for the 'macro' keyword
                if (current.type.equals(MACRO))
                {
                    // Coordinates of the 'macro' keyword
                    Coordinates coordinates = current.coordinates;

                    // Skip any intervening comments
                    do {
                        if (!iterator.hasNext()) break;
                        current = iterator.next();
                    } while (current.type.equals(COMMENT));

                    // 2. Next token must be an IDENTIFIER (the macro name)
                    if (current.type != IDENTIFIER)
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
                        // We can't really proceed meaningfully, so continue
                        continue;
                    }

                    String macroName = stream.getPackageName() + "." + current.internal.get();

                    // Skip any comments again
                    do {
                        if (!iterator.hasNext()) break;
                        current = iterator.next();
                    } while (current.type.equals(COMMENT));

                    // 3. Now expect '{' to begin the macro body
                    if (current.type != CURLY_OPEN)
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
                        continue;
                    }

                    // Create the Macro object
                    Macro macro = new Macro(coordinates, macroName);

                    //
                    // 4. Parse each “arm” until we encounter the final '}' of the macro.
                    //
                    while (true)
                    {
                        // Move to the next meaningful token
                        do {
                            if (!iterator.hasNext()) break;
                            current = iterator.next();
                        } while (current.type.equals(COMMENT));

                        // If we see the closing '}', we've finished this macro
                        if (current.type.equals(CURLY_CLOSED)) {
                            break;
                        }

                        // Otherwise, we expect an arm of the form:
                        //   ( pattern ) -> { code } ;
                        //
                        // Step a) parse the pattern (which starts with '(')
                        if (current.type != BRACE_OPEN)
                        {
                            errorManager.reportError(
                                    ParsingError.withHint(
                                            ErrorType.EXPECTED_FOUND,
                                            ErrorType.EXPECTED_FOUND.getDescription(),
                                            current.coordinates.row(),
                                            current.coordinates.column(),
                                            current.toString(),
                                            "Each macro arm must start with '('",
                                            "'('",
                                            current.describeContents()
                                    )
                            );
                            // Try to recover or skip this arm
                            continue;
                        }

                        // Parse the macro pattern up to matching ')'
                        Macro.Pattern pattern = parseMacroPattern(iterator, errorManager, stream);

                        // Step b) after the pattern, expect "->"
                        Token arrowToken = consumeNonComment(iterator);
                        if (arrowToken == null || arrowToken.type != TokenType.ARROW)
                        {
                            if (arrowToken != null) {
                                errorManager.reportError(
                                        ParsingError.withHint(
                                                ErrorType.EXPECTED_FOUND,
                                                ErrorType.EXPECTED_FOUND.getDescription(),
                                                arrowToken.coordinates.row(),
                                                arrowToken.coordinates.column(),
                                                arrowToken.toString(),
                                                "Missing '->' after macro pattern",
                                                "'->'",
                                                arrowToken.describeContents()
                                        )
                                );
                            }
                            // Attempt to continue
                            continue;
                        }

                        // Step c) after '->', expect '{' for the body
                        Token openBrace = consumeNonComment(iterator);
                        if (openBrace == null || openBrace.type != TokenType.CURLY_OPEN)
                        {
                            if (openBrace != null) {
                                errorManager.reportError(
                                        ParsingError.withHint(
                                                ErrorType.EXPECTED_FOUND,
                                                ErrorType.EXPECTED_FOUND.getDescription(),
                                                openBrace.coordinates.row(),
                                                openBrace.coordinates.column(),
                                                openBrace.toString(),
                                                "Missing '{' to start macro arm’s body",
                                                "'{'",
                                                openBrace.describeContents()
                                        )
                                );
                            }
                            // Attempt to continue
                            continue;
                        }

                        // Step d) Gather all tokens in the body until the matching '}'
                        List<Token> bodyTokens = parseBlock(iterator, errorManager);

                        // Create a TokenStream from the body tokens
                        TokenStream bodyStream = new TokenStream(
                                stream.getFilename(),
                                stream.getSource(),
                                bodyTokens
                        );

                        // Add the new arm to the macro
                        macro.addArm(pattern, bodyStream);

                        // Step e) After the body, we usually expect a semicolon or maybe we hit the final '}'
                        Token maybeSemicolon = consumeNonComment(iterator);
                        if (maybeSemicolon == null) {
                            // End of file or no more tokens
                            break;
                        }
                        if (maybeSemicolon.type.equals(TokenType.CURLY_CLOSED)) {
                            // That means no semicolon was there, but we closed the macro
                            break;
                        }
                        if (!maybeSemicolon.type.equals(TokenType.SEMI_COLON)) {
                            // Not a semicolon, might be an error or the next arm
                            // You could push it back or report an error. For simplicity, we’ll just do an error:
                            errorManager.reportError(
                                    ParsingError.withHint(
                                            ErrorType.EXPECTED_FOUND,
                                            ErrorType.EXPECTED_FOUND.getDescription(),
                                            maybeSemicolon.coordinates.row(),
                                            maybeSemicolon.coordinates.column(),
                                            maybeSemicolon.toString(),
                                            "Expected a semicolon ';' after the macro arm’s body",
                                            "';'",
                                            maybeSemicolon.describeContents()
                                    )
                            );
                            // Attempt to continue
                        }
                    }

                    // 5. Finally, store the completed macro
                    this.macros.add(macro);
                }
            }

            // Print all collected errors for this file
            if (errorManager.hasErrors()) {
                errorManager.printErrors(System.out);
            }
        }
    }

    /**
     * Consume the next token that is not a COMMENT, or return null if end.
     */
    private Token consumeNonComment(Iterator<Token> iterator) {
        while (iterator.hasNext()) {
            Token t = iterator.next();
            if (!t.type.equals(TokenType.COMMENT)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Parse the macro pattern starting after we've consumed the '(' token.
     * We read tokens until the matching ')' (accounting for nesting if needed).
     */
    private Macro.Pattern parseMacroPattern(Iterator<Token> iterator,
                                            ParsingErrorManager errorManager,
                                            TokenStream stream)
    {
        List<Macro.Pattern.PatternElement> elements = new ArrayList<>();
        int parenLevel = 1;  // We have already seen one '('

        while (iterator.hasNext() && parenLevel > 0)
        {
            Token current = iterator.next();
            if (current.type.equals(TokenType.COMMENT)) {
                // Skip comments
                continue;
            }
            else if (current.type.equals(TokenType.BRACE_OPEN)) {
                parenLevel++;
            }
            else if (current.type.equals(TokenType.BRACE_CLOSED)) {
                parenLevel--;
                if (parenLevel == 0) {
                    // End of pattern
                    break;
                }
            }
            else if (current.type.equals(TokenType.COMMA)) {
                // just a separator, skip
                continue;
            }
            else {
                // Check for something like `expression $x`, `type $T`, `identifier $name`,
                // or a literal pattern token, e.g. `add`, `subtract`.
                // This is a simplified approach that tries to see if the token is one
                // of the known macro "types" (expression/type/identifier) or a plain literal.

                if (isMacroTypeKeyword(current)) {
                    // e.g. "expression", "type", or "identifier"
                    // Next token should be a MACRO_VARIABLE like $x
                    Token varToken = consumeNonComment(iterator);
                    if (varToken == null || varToken.type != TokenType.MACRO_VARIABLE) {
                        // Report an error or handle it
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
                        else {
                            // no token at all
                        }
                        continue;
                    }
                    // Turn e.g. "expression" => MacroVarType.EXPRESSION
                    Macro.Pattern.MacroVarType varType = convertToMacroVarType(current.internal.orElse(""));
                    String varName = varToken.internal.orElse("$");
                    Macro.Pattern.VariableElement varElem = new Macro.Pattern.VariableElement(varName, varType);
                    elements.add(varElem);
                }
                else if (current.type == TokenType.MACRO_VARIABLE) {
                    // If the pattern is just `$foo` with no preceding keyword
                    // you could treat it as expression by default, or error
                    // For now, let's store it as a raw macro variable or error out
                    String varName = current.internal.orElse("$");
                    // Default to expression? Or error?
                    Macro.Pattern.VariableElement varElem = new Macro.Pattern.VariableElement(varName,
                            Macro.Pattern.MacroVarType.EXPRESSION);
                    elements.add(varElem);
                }
                else {
                    // Otherwise, treat it as a literal pattern element
                    elements.add(new Macro.Pattern.LiteralElement(current.internal.orElse(current.toString())));
                }
            }
        }

        return new Macro.Pattern(elements);
    }

    private boolean isMacroTypeKeyword(Token t) {
        if (t.internal.isEmpty()) return false;
        String val = t.internal.get();
        return val.equals("expression") || val.equals("type") || val.equals("identifier");
    }

    private Macro.Pattern.MacroVarType convertToMacroVarType(String s) {
        return switch (s) {
            case "expression" -> Macro.Pattern.MacroVarType.EXPRESSION;
            case "type"       -> Macro.Pattern.MacroVarType.TYPE;
            case "identifier" -> Macro.Pattern.MacroVarType.IDENTIFIER;
            default -> Macro.Pattern.MacroVarType.EXPRESSION;
        };
    }

    /**
     * Parse a `{ ... }` block and return all tokens inside.
     * If nested braces are encountered, handle them so we only stop
     * at the matching brace.
     */
    private List<Token> parseBlock(Iterator<Token> iterator, ParsingErrorManager errorManager)
    {
        List<Token> blockTokens = new ArrayList<>();
        int braceCount = 1; // We already consumed one '{'

        while (iterator.hasNext() && braceCount > 0)
        {
            Token t = iterator.next();
            if (t.type.equals(TokenType.CURLY_OPEN)) {
                braceCount++;
                blockTokens.add(t);
            }
            else if (t.type.equals(TokenType.CURLY_CLOSED)) {
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
}
