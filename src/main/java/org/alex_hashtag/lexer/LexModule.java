package org.alex_hashtag.lexer;

import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.lib.errors.ErrorManager;
import org.alex_hashtag.lib.results.Option;
import org.alex_hashtag.lib.tokenization.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LexModule
{

    Package moduleName;
    List<Import> imports;
    List<Macro> macros;
    List<Prototype> prototypes;
    TokenList tokens;

    public static LexModule create(String code, String fileName)
    {
        TokenList initialList = TokenList.create(code, toucanRules(), toucanPostProcessor());
        TokenList.LookAheadIterator iterator = (TokenList.LookAheadIterator) initialList.iterator();
        ErrorManager<LexerError> errorManager = new ErrorManager<>(fileName, code);

        Option<Package> moduleName = parsePackage(iterator, errorManager);


    }

    /// Parses the type of the import statement.
    /// It can either be a variable, a function, a macro, an annotation, or one of various types
    /// Here are examples:
    /// import class com.example.MyClass;
    /// import enum com.example.MyEnum;
    /// import trait com.example.MyTrait;
    /// import int32 com.example.MyVar;
    /// import int32(int32, float64) com.example.myFunction();
    /// import macro com.example.myMacro!;
    /// import annotation com.example.@MyAnnotation;
    ///
    /// The type of the import statement is determined by the keyword that follows the 'import' keyword.
    /// The type can be one of the following:
    /// - class
    /// - enum
    /// - trait
    /// - A type like int, String, etc. is considered a variable type.
    /// - Something that follows type(...) is a function, for example, int(int, int), String(int, int, int), etc.
    /// - macro
    /// - annotation
    /// @return The type of the import statement as a String
    private static Option<String> parseImportType(TokenList.LookAheadIterator iterator, ErrorManager<LexerError> errorManager) {
        // Skip over any comment tokens.
        Token current;
        do current = iterator.next();
        while (current instanceof Token.Comment);


        // Check if the token is a reserved import type keyword.
        switch (current)
        {
            case Token.Keyword keyword ->
            {
                String typeValue = keyword.getValue();
                if (typeValue.equals("class") || typeValue.equals("enum") || typeValue.equals("trait")
                        || typeValue.equals("macro") || typeValue.equals("annotation"))
                {
                    iterator.remove(); // Consume the token
                    return Option.some(typeValue);
                }
                else
                {
                    errorManager.reportError(new LexerError(
                            "Invalid import type keyword: " + typeValue,
                            current.getPosition().line(),
                            current.getPosition().column(),
                            current,
                            "Import type must be one of: class, enum, trait, macro, annotation, or a valid variable type."
                    ));
                    return Option.none();
                }
            }


            // If the token is an identifier, treat it as a variable type (or function type if followed by '(').
            case Token.Identifier identifier ->
            {
                String baseType = identifier.getValue();
                iterator.remove(); // Consume the type token


                // Peek ahead to see if it is a function type (i.e. followed by '(')
                Token next;
                do next = iterator.next();
                while (next instanceof Token.Comment);

                if (iterator.hasNext() && (next instanceof Token.Delimiter delimiter && delimiter.getValue().equals("(")))
                {
                    // It is a function type; consume the '(' delimiter.
                    iterator.next(); // Consume '('
                    iterator.remove();

                    StringBuilder paramsBuilder = new StringBuilder();
                    paramsBuilder.append("(");
                    boolean firstParam = true;
                    boolean closingFound = false;
                    while (iterator.hasNext())
                    {
                        // Skip comments between parameters.
                        do next = iterator.next();
                        while (next instanceof Token.Comment);
                        Token token = iterator.next();
                        // If we hit a closing parenthesis, we are done.
                        if (token instanceof Token.Delimiter delim && delim.getValue().equals(")"))
                        {
                            paramsBuilder.append(")");
                            closingFound = true;
                            break;
                        }
                        // Add a comma separator if needed.
                        if (!firstParam)
                        {
                            // If the token is an explicit comma, consume it and append the separator.
                            if (token instanceof Token.Delimiter comma && comma.getValue().equals(","))
                            {
                                paramsBuilder.append(", ");
                                continue;
                            }
                            else // If no comma was encountered, add one before the next parameter.
                                paramsBuilder.append(", ");
                        }
                        firstParam = false;

                        // Parameter tokens should be valid type identifiers (or keywords).
                        if (token instanceof Token.Identifier pid)
                        {
                            paramsBuilder.append(pid.getValue());
                        }
                        else if (token instanceof Token.Keyword pk)
                        {
                            paramsBuilder.append(pk.getValue());
                        }
                        else
                        {
                            errorManager.reportError(new LexerError(
                                    "Unexpected token in function type parameters: " + token.toString(),
                                    token.getPosition().line(),
                                    token.getPosition().column(),
                                    token,
                                    "Function type parameters must be valid type identifiers."
                            ));
                            return Option.none();
                        }
                    }
                    if (!closingFound)
                    {
                        errorManager.reportError(new LexerError(
                                "Expected ')' to close function type parameters.",
                                current.getPosition().line(),
                                current.getPosition().column(),
                                current,
                                "Ensure that the function type parameter list is properly closed with ')'."
                        ));
                        return Option.none();
                    }
                    String functionType = baseType + paramsBuilder.toString();
                    return Option.some(functionType);
                }
                else
                {
                    // No '(' found; return the variable type as is.
                    return Option.some(baseType);
                }
            }
            case null, default ->
            {
                // If the token type is not recognized, report an error.
                errorManager.reportError(new LexerError(
                        "Invalid token encountered while parsing import type: " + current.toString(),
                        current.getPosition().line(),
                        current.getPosition().column(),
                        current,
                        "Import type must be a valid type specifier."
                ));
                return Option.none();
            }
        }
    }


    private static Option<Package> parsePackage(TokenList.LookAheadIterator iterator, ErrorManager<LexerError> errorManager)
    {
        Token current;
        do current = iterator.next();
        while (current instanceof Token.Comment);

        if (!(current instanceof Token.Keyword keyword) || !keyword.getValue().equals("package"))
        {
            errorManager.reportError(new LexerError(
                    "Expected 'package' keyword, found " + current.toString(),
                    current.getPosition().line(),
                    current.getPosition().column(),
                    current,
                    "Every file must declare the package it is a part of at the top."
            ));
            return Option.none();
        }

        Option<List<String>> packageName = parseNextPackageName(iterator, errorManager);
        if (packageName.isSome())
        {
            return Option.some(new Package(packageName.unwrap()));
        }
        return Option.none();
    }

    private static Option<List<String>> parseNextPackageName(TokenList.LookAheadIterator iterator, ErrorManager<LexerError> errorManager)
    {
        iterator.remove(); // Consume 'package' keyword

        List<String> packageParts = new ArrayList<>();
        Token current;


        do current = iterator.next();
        while (current instanceof Token.Comment);

        while (current instanceof Token.Literal literal && literal.type().equals("default"))
        {
            packageParts.add(literal.getValue());
            iterator.remove(); // Consume token
            current = iterator.next();

            switch (current)
            {
                case Token.Delimiter delimiter when delimiter.getValue().equals(".") ->
                {
                    iterator.remove(); // Consume '.'
                    current = iterator.next();
                }
                case Token.Delimiter delimiter when delimiter.getValue().equals(";") ->
                {
                    iterator.remove(); // Consume ';'
                    return Option.some(packageParts); // Consume ';'
                }
                case null, default ->
                {
                    errorManager.reportError(new LexerError(
                            "Invalid package name syntax. Expected '.' or ';', found " + current.toString(),
                            current.getPosition().line(),
                            current.getPosition().column(),
                            current,
                            "Package names must be a sequence of identifiers separated by dots and ending with a semicolon."
                    ));
                    return Option.none();
                }
            }
        }

        errorManager.reportError(new LexerError(
                "Expected package name after 'package' keyword.",
                current.getPosition().line(),
                current.getPosition().column(),
                current,
                "A valid package declaration must follow the format: package com.example;"
        ));
        return Option.none();
    }


    private static TokenRules toucanRules()
    {
        return TokenRules.builder()

                // === Delimiters === //
                .delimeter(";").delimeter(":").delimeter(",")
                .delimeter("(").delimeter(")").delimeter("[").delimeter("]")
                .delimeter("{").delimeter("}").delimeter("<").delimeter(">")

                // === Operators === //
                // Arithmetic & Assignment
                .operator("=").operator("+").operator("-").operator("*").operator("/")
                .operator("%").operator("?").operator(".")

                // Logical Operators
                .operator("and").operator("or").operator("!")
                .operator("==").operator("!=").operator(">").operator(">=")
                .operator("<").operator("<=")

                // Bitwise Operators
                .operator("<<").operator(">>").operator(">>>")
                .operator("&").operator("|").operator("^").operator("~")

                // Compound Assignment Operators
                .operator("<<=").operator(">>=").operator(">>>=")
                .operator("+=").operator("-=").operator("*=").operator("/=")
                .operator("%=").operator("&=").operator("|=").operator("^=")

                // Increment/Decrement & Misc
                .operator("++").operator("--").operator("::")
                .operator("->").operator("$") // Special operators

                // === Keywords (Removed: var, private, sys) === //
                .keyword("mutable").keyword("const").keyword("static")
                .keyword("if").keyword("else").keyword("while").keyword("do").keyword("for")
                .keyword("loop").keyword("switch").keyword("continue").keyword("break")
                .keyword("yield").keyword("return").keyword("inline").keyword("echo")
                .keyword("struct").keyword("typedef").keyword("enum").keyword("trait")
                .keyword("implement").keyword("template").keyword("class")
                .keyword("constructor").keyword("implicit").keyword("extends").keyword("implements")
                .keyword("abstract").keyword("public").keyword("protected")
                .keyword("typeof").keyword("sizeof").keyword("unsafe")
                .keyword("package").keyword("import").keyword("macro").keyword("annotation")

                // === Identifiers === //
                .identifier("default", "^[A-Za-z_]\\w*")  // Standard identifier
                .identifier("macro", "^[A-Za-z_]\\w*!$") // Ends with "!"
                .identifier("annotation", "^@[A-Za-z_]\\w*") // Starts with "@"

                // === Literals === //
                .literal("null", "null") // Null literal
                .literal("boolean", "true|false") // Boolean literals
                .literal("integer", "^(?:0[xX][0-9a-fA-F_]+|0[bB][01_]+|0[oO][0-7_]+|[1-9][0-9_]*|0)") // Integer literals
                .literal("float", "^(\\d[\\d_]*\\.\\d[\\d_]*([eE][+-]?\\d[\\d_]*)?|\\d[\\d_]+([eE][+-]?\\d[\\d_]*)?)") // Float literals
                .literal("char", "^'(\\\\.|[^\\\\'])'") // Character literal
                .literal("rune", "^'(\\\\u[0-9A-Fa-f]{4}|\\\\U[0-9A-Fa-f]{8}|[^\\\\'])'") // Unicode rune literal
                .literal("string", "^\"(?:\\\\.|[^\"\\\\])*\"") // Single-line string literal
                .literal("string", "^\"\"\"(?:.|\\n)*?\"\"\"") // Multi-line string literal

                // === Comments (Fixed multi-line regex) === //
                .comment("//.*")  // Single-line comments
                .comment("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/")  // Multi-line comments

                // === Tokenization Behavior === //
                .whitespaceMode(WhitespaceMode.IGNORE)  // Ignore whitespace (C-style)
                .enableLongestMatchFirst()  // Ensures longest matches take precedence (e.g., `>=` before `>`)
                .makeCaseSensitive()  // Distinguish between lowercase and uppercase identifiers

                .build();
    }

    private static TokenPostProcessor toucanPostProcessor()
    {
        return TokenPostProcessor.builder()
                .literal("string", TokenTransformations::unquoteAndTrimIndentation)
                .literal("string", TokenTransformations::processEscapeSequences)
                .comment("comment", TokenTransformations::stripCommentMarkers)
                .literal("integer", TokenTransformations::normalizeInteger)
                .literal("float", TokenTransformations::normalizeFloat)
                .identifier("annotation", TokenTransformations::stripAnnotation)
                .identifier("macro", TokenTransformations::stripMacroExclamation)
                .build();
    }


}
