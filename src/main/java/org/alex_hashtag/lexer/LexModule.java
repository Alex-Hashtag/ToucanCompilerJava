package org.alex_hashtag.lexer;

import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.lib.tokenization.*;

import java.util.Iterator;
import java.util.List;

public class LexModule
{

    Package moduleName;
    List<Import> imports;
    List<Macro> macros;
    List<Prototype> prototypes;
    TokenList tokens;

    public static LexModule create(String code)
    {
        TokenList initialList = TokenList.create(code, toucanRules(), toucanPostProcessor());


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
