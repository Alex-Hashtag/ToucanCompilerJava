package org.alex_hashtag.tokenization;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

public class TokenStreamTest {

    /**
     * Helper method to get token types (excluding START and END)
     * from a given TokenStream.
     */
    private List<TokenType> getTokenTypes(TokenStream ts) {
        List<TokenType> types = new LinkedList<>();
        // Skip first and last tokens (START, END)
        List<Token> tokens = ts.tokens;
        for (int i = 1; i < tokens.size() - 1; i++) {
            types.add(tokens.get(i).type);
        }
        return types;
    }

    /**
     * Helper method to get token internals (if present) from a given TokenStream.
     */
    private List<String> getTokenInternals(TokenStream ts) {
        List<String> internals = new LinkedList<>();
        // Skip first and last tokens (START, END)
        List<Token> tokens = ts.tokens;
        for (int i = 1; i < tokens.size() - 1; i++) {
            tokens.get(i).internal.ifPresent(internals::add);
        }
        return internals;
    }

    @Test
    public void testWhitespaceIgnored() {
        // Whitespace should be ignored so that we get only one token (if nothing else)
        // aside from the start and end tokens.
        String input = "    \t   \n  \r\n";
        TokenStream ts = new TokenStream(input);
        // Only START and END tokens exist
        assertEquals(2, ts.tokens.size(), "Only START and END tokens should be present");
    }

    @Test
    public void testSingleLineComment() {
        String comment = "// This is a single-line comment\n";
        TokenStream ts = new TokenStream(comment);
        // There should be a COMMENT token besides START and END
        assertEquals(3, ts.tokens.size(), "Should have START, COMMENT and END tokens");
        Token commentToken = ts.tokens.get(1);
        assertEquals(TokenType.COMMENT, commentToken.type, "Token should be a COMMENT");
        commentToken.internal.ifPresent(internal -> assertEquals(comment.trim(), internal.trim()));
    }

    @Test
    public void testMultiLineComment() {
        String comment = "/* This is a \n multi-line comment */";
        TokenStream ts = new TokenStream(comment);
        assertEquals(3, ts.tokens.size(), "Should have START, COMMENT and END tokens");
        Token commentToken = ts.tokens.get(1);
        assertEquals(TokenType.COMMENT, commentToken.type, "Token should be a COMMENT");
        commentToken.internal.ifPresent(internal -> assertTrue(internal.contains("multi-line comment")));
    }

    @Test
    public void testMultiLineStringLiteral() {
        String input = "\"\"\"This is \na multi-line string\"\"\"";
        TokenStream ts = new TokenStream(input);
        assertEquals(3, ts.tokens.size(), "Should have START, STRING_LITERAL and END tokens");
        Token strToken = ts.tokens.get(1);
        assertEquals(TokenType.STRING_LITERAL, strToken.type, "Token should be a STRING_LITERAL");
        strToken.internal.ifPresent(internal -> {
            assertTrue(internal.contains("multi-line string"), "String literal should contain the content");
            assertTrue(internal.startsWith("\"\"\""), "String literal should start with triple quotes");
        });
    }

    @Test
    public void testRegularStringLiteral() {
        String input = "\"Hello, world!\"";
        TokenStream ts = new TokenStream(input);
        assertEquals(3, ts.tokens.size(), "Should have START, STRING_LITERAL and END tokens");
        Token token = ts.tokens.get(1);
        assertEquals(TokenType.STRING_LITERAL, token.type, "Token should be a STRING_LITERAL");
        token.internal.ifPresent(internal -> assertEquals("\"Hello, world!\"", internal));
    }

    @Test
    public void testCharacterLiteral() {
        String input = "'a'";
        TokenStream ts = new TokenStream(input);
        // Expect a char literal token
        assertEquals(3, ts.tokens.size(), "Should have START, CHAR_LITERAL and END tokens");
        Token token = ts.tokens.get(1);
        assertEquals(TokenType.CHAR_LITERAL, token.type, "Token should be a CHAR_LITERAL");
        token.internal.ifPresent(internal -> assertEquals("'a'", internal));
    }

    @Test
    public void testAnnotationUse() {
        String input = "@Getter";
        TokenStream ts = new TokenStream(input);
        // There should be an annotation token.
        assertEquals(3, ts.tokens.size(), "Should have START, ANNOTATION_USE and END tokens");
        Token token = ts.tokens.get(1);
        assertEquals(TokenType.ANNOTATION_USE, token.type, "Token should be an ANNOTATION_USE token");
        token.internal.ifPresent(internal -> assertEquals("@Getter", internal));
    }

    @Test
    public void testMacroUse() {
        // Test a macro usage like sum!(1, 2, 3)
        String input = "sum!(1, 2, 3)";
        TokenStream ts = new TokenStream(input);
        // We expect a macro use token.
        boolean foundMacro = ts.tokens.stream().anyMatch(
                token -> token.type == TokenType.MACRO_USE && token.internal.orElse("").startsWith("sum!")
        );
        assertTrue(foundMacro, "The MACRO_USE token should be found with content starting with 'sum!'");
    }

    @Test
    public void testMacroVariable() {
        String input = "$foo $bar123";
        TokenStream ts = new TokenStream(input);
        // Expect two macro variables plus the START and END tokens.
        // Use getTokenTypes to verify two tokens of type MACRO_VARIABLE.
        List<TokenType> types = getTokenTypes(ts);
        long macroCount = types.stream().filter(t -> t == TokenType.MACRO_VARIABLE).count();
        assertEquals(2, macroCount, "Should have two MACRO_VARIABLE tokens");

        // Verify internals contain the variable names.
        List<String> internals = getTokenInternals(ts);
        assertTrue(internals.contains("$foo"), "First macro variable should be '$foo'");
        assertTrue(internals.contains("$bar123"), "Second macro variable should be '$bar123'");
    }

    @Test
    public void testNumberLiterals() {
        String input = "123 0 0xFF 3.14 1e10";
        TokenStream ts = new TokenStream(input);

        // Assert expectations
        List<TokenType> types = getTokenTypes(ts);
        long intLiteralCount = types.stream().filter(t -> t == TokenType.INT_LITERAL).count();
        long floatLiteralCount = types.stream().filter(t -> t == TokenType.FLOAT_LITERAL).count();

        assertEquals(3, intLiteralCount, "There should be 3 INT_LITERAL tokens");
        assertEquals(2, floatLiteralCount, "There should be 2 FLOAT_LITERAL tokens");
    }

    @Test
    public void testIdentifierAndKeywords() {
        // Test that keywords are recognized and different from identifiers.
        String input = "if identifier int32 myVar";
        TokenStream ts = new TokenStream(input);
        // Expected tokens, skipping START and END:
        // token1: IF (keyword)
        // token2: MACRO_EXPR? or IDENTIFIER ("identifier") depends on keyword mapping;
        // In the given TokenType, "identifier" is a special macro keyword mapped to MACRO_IDENT.
        // token3: INT32 (keyword)
        // token4: IDENTIFIER ("myVar")
        List<Token> tokens = ts.tokens;
        assertEquals(6, tokens.size(), "Expected 4 tokens + START and END");

        // Check first token: IF
        assertEquals(TokenType.IF, tokens.get(1).type, "First token should be IF (keyword)");
        // Check second token: special macro keyword "identifier"
        assertEquals(TokenType.MACRO_IDENT, tokens.get(2).type, "Second token should be a MACRO_IDENT token");
        // Check third token: INT32 keyword
        assertEquals(TokenType.INT32, tokens.get(3).type, "Third token should be INT32 keyword");
        // Check fourth token: regular identifier "myVar"
        Token token4 = tokens.get(4);
        assertEquals(TokenType.IDENTIFIER, token4.type, "Fourth token should be an IDENTIFIER token");
        token4.internal.ifPresent(internal -> assertEquals("myVar", internal));
    }

    @Test
    public void testOperatorsAndPunctuations() {
        // Test multiple single-character and multi-character operators.
        String input = "; : . , ( ) [ ] { } < > = + - * / % & | ^ ~ !";
        TokenStream ts = new TokenStream(input);
        // Remove START and END tokens
        List<Token> tokens = ts.tokens.subList(1, ts.tokens.size() - 1);
        // Create a mapping of the expected token types in the order of appearance.
        TokenType[] expected = {
                TokenType.SEMI_COLON, TokenType.COLON, TokenType.DOT, TokenType.COMMA,
                TokenType.BRACE_OPEN, TokenType.BRACE_CLOSED,
                TokenType.BRACKET_OPEN, TokenType.BRACKET_CLOSED,
                TokenType.CURLY_OPEN, TokenType.CURLY_CLOSED,
                TokenType.ARROW_OPEN, TokenType.ARROW_CLOSED,
                TokenType.ASSIGNMENT, TokenType.ADDITION, TokenType.SUBTRACTION,
                TokenType.MULTIPLICATION, TokenType.DIVISION, TokenType.MODULO,
                TokenType.BITWISE_AND, TokenType.BITWISE_OR, TokenType.BITWISE_XOR,
                TokenType.BITWISE_NOT, TokenType.LOGICAL_NOT
        };
        assertEquals(expected.length, tokens.size(), "Token count mismatch in operators/punctuation test");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).type, "Unexpected token type at index " + i);
        }
    }

    @Test
    public void testInvalidToken() {
        // Provide an input that does not match any valid token.
        String input = "@?"; // '@' should start an annotation but '?' should not be valid here.
        TokenStream ts = new TokenStream(input);
        // We expect a valid ANNOTATION_USE for "@" followed by a failure on "?".
        List<Token> tokens = ts.tokens;
        // START, annotation token, INVALID, END
        assertEquals(4, tokens.size(), "Should have 2 real tokens plus START and END");
        Token annotationToken = tokens.get(1);
        assertEquals(TokenType.ANNOTATION_USE, annotationToken.type, "First token should be ANNOTATION_USE");
        // Second token should be INVALID (for "?")
        Token invalidToken = tokens.get(2);
        assertEquals(TokenType.INVALID, invalidToken.type, "Token should be INVALID");
        invalidToken.internal.ifPresent(internal -> assertEquals("?", internal));
    }

    @Test
    public void testComplexInput() {
        // A more complex input combining several of the constructs
        String input = """
                // Single line comment
                /* Multi-line
                   comment */
                @Annotation
                class MyClass {
                    int32 value = 123 + 456;
                    string text = "Hello, world!";
                    float num = 3.14;
                    sum!(1, 2, 3);
                    $macroVar;
                }
                """;
        TokenStream ts = new TokenStream(input);
        // We perform several sanity checks
        List<Token> tokens = ts.tokens;
        assertTrue(tokens.size() > 10, "Expected multiple tokens in the complex input");

        // Verify that keywords such as package, class, int32, string exist.
        boolean foundPackage = tokens.stream().anyMatch(token -> token.type == TokenType.PACKAGE);
        boolean foundClass = tokens.stream().anyMatch(token -> token.type == TokenType.CLASS);
        boolean foundInt32 = tokens.stream().anyMatch(token -> token.type == TokenType.INT32);
        boolean foundString = tokens.stream().anyMatch(token -> token.type == TokenType.STRING);
        assertTrue(foundClass, "Keyword 'class' should be recognized");
        assertTrue(foundInt32, "Keyword 'int32' should be recognized");
        assertTrue(foundString, "Keyword 'string' should be recognized");

        // Check that the macro usage token exists.
        boolean foundMacro = tokens.stream().anyMatch(token ->
                token.type == TokenType.MACRO_USE &&
                        token.internal.orElse("").startsWith("sum!")
        );
        assertTrue(foundMacro, "Macro usage 'sum!(1, 2, 3)' should be tokenized");

        // And check the macro variable.
        boolean foundMacroVar = tokens.stream().anyMatch(token ->
                token.type == TokenType.MACRO_VARIABLE &&
                        token.internal.orElse("").equals("$macroVar")
        );
        assertTrue(foundMacroVar, "Macro variable '$macroVar' should be tokenized");
    }
}
