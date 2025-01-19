package org.alex_hashtag.tokenization;

public enum TokenType
{
    SEMI_COLON(";"),
    COLON(":"),
    DOT("."),
    COMMA(","),

    //Braces
    BRACE_OPEN("("),
    BRACE_CLOSED(")"),
    BRACKET_OPEN("\\["),
    BRACKET_CLOSED("\\]"),
    CURLY_OPEN("\\{"),
    CURLY_CLOSED("\\}"),
    ARROW_OPEN("<"),
    ARROW_CLOSED(">"),

    //Operators
    ASSIGNMENT("="),
    ADDITION("\\+"),
    SUBTRACTION("-"),
    MULTIPLICATION("\\*"),
    DIVISION("/"),
    MODULO("%"),

    //LOGICAL
    LOGICAL_AND("and"),
    LOGICAL_OR("or"),
    LOGICAL_NOT("\\!"),
    EQUALITY("=="),
    INEQUALITY("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),

    //Bit-Wise
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    BITWISE_AND("&"),
    BITWISE_OR("\\|"),
    BITWISE_XOR("\\^"),
    BITWISE_NOT("~"),

    // Multi-character operators
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

    //Types
    INT8("int8"),
    INT16("int16"),
    INT32("int32"),
    INT64("int64"),
    INT128("int128"),
    UINT8("uint8"),
    UINT16("uint16"),
    UINT32("uint32"),
    UINT64("uint64"),
    UINT128("uint128"),
    FLOAT16("float16"),
    FLOAT32("float32"),
    FLOAT64("float64"),
    FLOAT80("float80"),
    FLOAT128("float128"),
    CHAR("char"),
    RUNE("rune"),
    STRING("string"),
    BOOL("bool"),
    VOID("void"),
    TYPE("type"),
    LAMBDA("lambda"),

    VAR("var"),

    //Variable Modifiers
    MUTABLE("mutable"),
    CONST("const"),
    STATIC("static"),

    //Control Flow
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

    //Functions
    RETURN("return"),
    INLINE("inline"),
    ECHO("echo"),

    //Types
    STRUCT("struct"),
    TYPEDEF("typedef"),
    ENUM("enum"),
    TRAIT("trait"),
    IMPLEMENT("implement"),
    TEMPLATE("template"),
    OPERATIONS("operations"),

    //OOP
    CLASS("class"),
    CONSTRUCTOR("constructor"),
    IMPLICIT("implicit"),
    EXTENDS("extends"),
    IMPLEMENTS("implements"),
    THIS("this"),
    SUPER("super"),
    ABSTRACT("abstract"),
    NULL("null"),


    //Access Modifiers
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),

    //Systems Programming
    SYS("sys"),
    TYPEOF("typeof"),
    SIZEOF("sizeof"),
    UNSAFE("unsafe"),

    //Packages
    PACKAGE("package"),
    IMPORT("import"),
    NAMESPACE("namespace"),

    //Macros
    MACRO("macro"),       // existing macro keyword
    ANNOTATION("annotation"),

    // *** NEW TOKENS FOR ANNOTATION & MACROS ***
    ANNOTATION_USE(""),   // logic-driven, not purely regex-based
    MACRO_USE(""),        // logic-driven
    MACRO_VARIABLE("^\\$[A-Za-z_]\\w*"), // e.g. $foo
    MACRO_EXPR("expression"),
    MACRO_IDENT("identifier"),

    //Others
    ARROW("->"),
    ERROR("error"),
    IDENTIFIER("^[A-Za-z_]\\w*"),

    //Literals
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
                 MACRO_VARIABLE, ANNOTATION_USE, MACRO_USE ->
                    true;
            default -> false;
        };
    }
}
