package org.alex_hashtag.tokenization;

public enum TokenType
{
    SEMI_COLON(";"),
    COLON(":"),
    DOT("."),
    COMMA(","),

    // Braces
    BRACE_OPEN("("),
    BRACE_CLOSED(")"),
    BRACKET_OPEN("\\["),
    BRACKET_CLOSED("\\]"),
    CURLY_OPEN("\\{"),
    CURLY_CLOSED("\\}"),
    ARROW_OPEN("<"),
    ARROW_CLOSED(">"),

    // Operators
    ASSIGNMENT("="),
    ADDITION("\\+"),
    SUBTRACTION("-"),
    MULTIPLICATION("\\*"),
    DIVISION("/"),
    MODULO("%"),

    // LOGICAL
    LOGICAL_AND("and"),
    LOGICAL_OR("or"),
    LOGICAL_NOT("\\!"),
    EQUALITY("=="),
    INEQUALITY("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),

    // Bit-Wise
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    BITWISE_AND("&"),
    BITWISE_OR("\\|"),
    BITWISE_XOR("\\^"),
    BITWISE_NOT("~"),

    // Multi-char operators
    BIT_SHIFT_LEFT_EQUALS("<<="),
    BIT_SHIFT_RIGHT_EQUALS(">>="),
    BIT_SHIFT_RIGHT_UNSIGNED_EQUALS(">>>="),
    ADDITION_ASSIGN("\\+="),
    SUBTRACTION_ASSIGN("-="),
    MULTIPLICATION_ASSIGN("\\*="),
    DIVISION_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    BITWISE_AND_ASSIGN("&="),
    BITWISE_OR_ASSIGN("\\|="),
    BITWISE_XOR_ASSIGN("\\^="),
    INCREMENT("\\+\\+"),
    DECREMENT("--"),
    DOUBLE_COLON("::"),

    // Additional
    VAR("var"),
    MUTABLE("mutable"),
    CONST("const"),
    STATIC("static"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    DO("do"),
    FOR("for"),
    LOOP("loop"),
    SWITCH("switch"),
    CONTINUE("continue"),
    BREAK("break"),
    YIELD("yield"),
    RETURN("return"),
    INLINE("inline"),
    ECHO("echo"),
    STRUCT("struct"),
    TYPEDEF("typedef"),
    ENUM("enum"),
    TRAIT("trait"),
    IMPLEMENT("implement"),
    TEMPLATE("template"),
    OPERATIONS("operations"),
    CLASS("class"),
    CONSTRUCTOR("constructor"),
    IMPLICIT("implicit"),
    EXTENDS("extends"),
    IMPLEMENTS("implements"),
    THIS("this"),
    SUPER("super"),
    ABSTRACT("abstract"),
    NULL("null"),
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    SYS("sys"),
    TYPEOF("typeof"),
    SIZEOF("sizeof"),
    UNSAFE("unsafe"),
    PACKAGE("package"),
    IMPORT("import"),

    // Macros
    MACRO("macro"),
    ANNOTATION("annotation"),
    ANNOTATION_USE(""),
    MACRO_USE(""),
    MACRO_VARIABLE("^\\$[A-Za-z_]\\w*"),
    MACRO_EXPR("expression"),
    MACRO_IDENT("identifier"),

    // **Important** for $(
    MACRO_REPEAT_OPEN("\\$\\("),

    ARROW("->"),
    ERROR("error"),
    IDENTIFIER("^[A-Za-z_]\\w*"),

    // Literals
    TRUE("true"),
    FALSE("false"),
    INT_LITERAL("^(?:0[xX][0-9a-fA-F_]+|0[bB][01_]+|0[oO][0-7_]+|[1-9][0-9_]*|0)"),
    FLOAT_LITERAL("^(\\d[\\d_]*\\.\\d[\\d_]*([eE][+-]?\\d[\\d_]*)?|\\d[\\d_]+([eE][+-]?\\d[\\d_]*)?)"),
    CHAR_LITERAL("^'(\\\\.|[^\\\\'])'"),
    RUNE_LITERAL("^'(\\\\u[0-9A-Fa-f]{4}|\\\\U[0-9A-Fa-f]{8}|[^\\\\'])'"),
    STRING_LITERAL("^\"(?:\\\\.|[^\"\\\\])*\""),

    START("[]"),
    END("[]"),
    INVALID("[]"),
    COMMENT("[]");

    public final String regex;

    TokenType(String regex)
    {
        this.regex = regex;
    }

    public boolean isStored()
    {
        return switch (this)
        {
            case IDENTIFIER, INT_LITERAL, FLOAT_LITERAL,
                 CHAR_LITERAL, RUNE_LITERAL, STRING_LITERAL,
                 MACRO_VARIABLE, ANNOTATION_USE, MACRO_USE -> true;
            default -> false;
        };
    }
}
