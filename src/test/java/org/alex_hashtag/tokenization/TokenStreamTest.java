package org.alex_hashtag.tokenization;

import org.alex_hashtag.tokenizationOLD.ImportDeclaration;
import org.alex_hashtag.tokenizationOLD.Token;
import org.alex_hashtag.tokenizationOLD.TokenStream;
import org.alex_hashtag.tokenizationOLD.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Iterator;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

class TokenStreamTest {

    private TokenStream tokenStream;

    @BeforeEach
    void setUp() {
        // Sample source code
        String source = """
                package com.example;
                
                import java.util.List;
                import static java.lang.Math.PI;
                
                // This is a single-line comment
                /*
                 * This is a multi-line comment
                 */
                
                class HelloWorld {
                    public void main() {
                        println!("Hello, world!");
                        int a = 10;
                        float32 b = 3.14;
                        char c = 'A';
                        string s = "Sample string";
                        bool flag = true and false;
                    }
                }
                """;

        tokenStream = new TokenStream(Paths.get("HelloWorld.toucan"), source);
    }

    @Test
    void testPackageName() {
        assertEquals("com.example", tokenStream.getPackageName(), "Package name should be parsed correctly.");
    }

    @Test
    void testImports() {
        assertEquals(2, tokenStream.getImports().size(), "There should be two import statements.");

        ImportDeclaration import1 = tokenStream.getImports().get(0);
        assertFalse(import1.isStatic(), "First import should not be static.");
        assertEquals("java.util.List", import1.fullName(), "First import full name should be 'java.util.List'.");
        assertNull(import1.memberOrStar(), "First import should not have a member or star.");

        ImportDeclaration import2 = tokenStream.getImports().get(1);
        assertTrue(import2.isStatic(), "Second import should be static.");
        assertEquals("java.lang.Math", import2.fullName(), "Second import full name should be 'java.lang.Math'.");
        assertEquals("PI", import2.memberOrStar(), "Second import member should be 'PI'.");
    }

    @Test
    void testTokens() {
        Iterator<Token> iterator = tokenStream.iterator();

        // Check for START token
        assertTrue(iterator.hasNext(), "Token stream should have tokens.");
        Token startToken = iterator.next();
        assertEquals(TokenType.START, startToken.type, "First token should be START.");

        // Iterate through tokens and perform checks
        boolean foundClass = false;
        boolean foundMainMethod = false;
        boolean foundPrintln = false;

        while (iterator.hasNext()) {
            Token token = iterator.next();

            if (token.type == TokenType.CLASS) {
                foundClass = true;
            }

            if (token.type == TokenType.IDENTIFIER && "main".equals(token.internal.orElse(""))) {
                foundMainMethod = true;
            }

            if (token.type == TokenType.IDENTIFIER && "println".equals(token.internal.orElse(""))) {
                foundPrintln = true;
            }
        }

        assertTrue(foundClass, "Token stream should contain 'class' keyword.");
        assertTrue(foundMainMethod, "Token stream should contain 'main' method identifier.");
        assertTrue(foundPrintln, "Token stream should contain 'println' identifier.");

        // Check for END token
        assertFalse(iterator.hasNext(), "Token stream should end after all tokens are iterated.");
    }

    @Test
    void testCommentsAreTokenized() {
        String sourceWithComments = """
                package com.example;
                
                // Single-line comment
                /* 
                 * Multi-line comment
                 */
                """;

        TokenStream ts = new TokenStream(Paths.get("Comments.toucan"), sourceWithComments);
        Iterator<Token> iterator = ts.iterator();

        // START token
        assertTrue(iterator.hasNext());
        Token start = iterator.next();
        assertEquals(TokenType.START, start.type);

        // PACKAGE token
        Token packageToken = iterator.next();
        assertEquals(TokenType.PACKAGE, packageToken.type);

        // IDENTIFIER token for package name
        Token packageName = iterator.next();
        assertEquals(TokenType.IDENTIFIER, packageName.type);
        assertEquals("com.example", packageName.internal.orElse(""));

        // SEMI_COLON token
        Token semi = iterator.next();
        assertEquals(TokenType.SEMI_COLON, semi.type);

        // Single-line comment
        Token singleLineComment = iterator.next();
        assertEquals(TokenType.COMMENT, singleLineComment.type);
        assertTrue(singleLineComment.internal.orElse("").contains("Single-line comment"));

        // Multi-line comment
        Token multiLineComment = iterator.next();
        assertEquals(TokenType.COMMENT, multiLineComment.type);
        assertTrue(multiLineComment.internal.orElse("").contains("Multi-line comment"));

        // END token
        Token end = iterator.next();
        assertEquals(TokenType.END, end.type);

        assertFalse(iterator.hasNext(), "No more tokens should be present.");
    }

    @Test
    void testUnclosedStringLiteral() throws Exception {
        String sourceWithUnclosedString = """
                package com.example;
                
                class Test {
                    void method() {
                        string s = "This string is not closed;
                    }
                }
                """;

        // Use System Lambda to catch System.exit
        int status = catchSystemExit(() -> {
            TokenStream ts = new TokenStream(Paths.get("UnclosedString.toucan"), sourceWithUnclosedString);
        });

        assertEquals(1, status, "System.exit should be called with status 1 for unclosed string literal.");
    }

    @Test
    void testInvalidToken() throws Exception {
        String sourceWithInvalidToken = """
                package com.example;
                
                class Test {
                    void method() {
                        int a = 10;
                        float b = 3.14;
                        $invalid = 5;
                    }
                }
                """;

        // Use System Lambda to catch System.exit
        int status = catchSystemExit(() -> {
            TokenStream ts = new TokenStream(Paths.get("InvalidToken.toucan"), sourceWithInvalidToken);
        });

        assertEquals(1, status, "System.exit should be called with status 1 for invalid token.");
    }

    @Test
    void testTokenizationOfLiterals() {
        String sourceWithLiterals = """
                package com.example;
                
                class Literals {
                    void method() {
                        int a = 42;
                        float32 b = 3.14;
                        char c = 'A';
                        string s = "Hello, World!";
                        bool flag = true;
                        bool flag2 = false;
                    }
                }
                """;

        TokenStream ts = new TokenStream(Paths.get("Literals.toucan"), sourceWithLiterals);
        Iterator<Token> iterator = ts.iterator();

        // Iterate through tokens and verify literals
        while (iterator.hasNext()) {
            Token token = iterator.next();

            if (token.type == TokenType.INT_LITERAL) {
                assertEquals("42", token.internal.orElse(""), "Integer literal should be 42.");
            }

            if (token.type == TokenType.FLOAT_LITERAL) {
                assertEquals("3.14", token.internal.orElse(""), "Float literal should be 3.14.");
            }

            if (token.type == TokenType.CHAR_LITERAL) {
                assertEquals("'A'", token.internal.orElse(""), "Char literal should be 'A'.");
            }

            if (token.type == TokenType.STRING_LITERAL) {
                assertEquals("\"Hello, World!\"", token.internal.orElse(""), "String literal should be \"Hello, World!\".");
            }

            if (token.type == TokenType.TRUE) {
                // No internal content expected
            }

            if (token.type == TokenType.FALSE) {
                // No internal content expected
            }
        }

        // No exceptions means pass
    }

    @Test
    void testMacroUsage() {
        String sourceWithMacro = """
                package com.example;
                
                class MacroTest {
                    void method() {
                        repeat_hello!(3);
                        sum!(1, 2, 3, 4, 5);
                        @Getter("fieldName")
                    }
                }
                """;

        TokenStream ts = new TokenStream(Paths.get("MacroTest.toucan"), sourceWithMacro);
        Iterator<Token> iterator = ts.iterator();

        boolean foundRepeatHello = false;
        boolean foundSum = false;
        boolean foundGetter = false;

        while (iterator.hasNext()) {
            Token token = iterator.next();

            if (token.type == TokenType.MACRO_USE && token.internal.orElse("").startsWith("repeat_hello!")) {
                foundRepeatHello = true;
            }

            if (token.type == TokenType.MACRO_USE && token.internal.orElse("").startsWith("sum!")) {
                foundSum = true;
            }

            if (token.type == TokenType.ANNOTATION_USE && token.internal.orElse("").equals("@Getter")) {
                foundGetter = true;
            }
        }

        assertTrue(foundRepeatHello, "Macro 'repeat_hello!' should be tokenized.");
        assertTrue(foundSum, "Macro 'sum!' should be tokenized.");
        assertTrue(foundGetter, "Annotation '@Getter' should be tokenized.");
    }

    @Test
    void testIdentifiersAndKeywords() throws Exception {
        String source = """
                package com.example;
                
                class TestClass {
                    void testMethod() {
                        int var1 = 10;
                        int ifValue = 20;
                        bool flag = true;
                        bool elseFlag = false;
                        int _privateVar = 30;
                        int $invalidVar = 40; // Invalid identifier
                    }
                }
                """;

        // Use System Lambda to catch System.exit
        int status = catchSystemExit(() -> {
            TokenStream ts = new TokenStream(Paths.get("Identifiers.toucan"), source);
        });

        assertEquals(1, status, "System.exit should be called with status 1 for invalid identifier.");
    }

    @Test
    void testGetTokensAsString() {
        String expectedTokens = """
                Type: START, Row: 0, Column: 0
                Type: PACKAGE, Row: 1, Column: 1
                Type: IDENTIFIER, Content: com.example, Row: 1, Column: 9
                Type: SEMI_COLON, Row: 1, Column: 21
                Type: IMPORT, Row: 3, Column: 1
                Type: IDENTIFIER, Content: java.util.List, Row: 3, Column: 8
                Type: SEMI_COLON, Row: 3, Column: 23
                Type: IMPORT, Row: 4, Column: 1
                Type: IDENTIFIER, Content: java.lang.Math, Row: 4, Column: 8
                Type: DOT, Row: 4, Column: 22
                Type: IDENTIFIER, Content: PI, Row: 4, Column: 23
                Type: SEMI_COLON, Row: 4, Column: 25
                Type: COMMENT, Content: // This is a single-line comment, Row: 6, Column: 1
                Type: COMMENT, Content: /* 
                 * This is a multi-line comment
                 */, Row: 7, Column: 1
                Type: CLASS, Row: 10, Column: 1
                Type: IDENTIFIER, Content: HelloWorld, Row: 10, Column: 7
                Type: BRACE_OPEN, Row: 10, Column: 17
                Type: PUBLIC, Row: 11, Column: 5
                Type: VOID, Row: 11, Column: 12
                Type: IDENTIFIER, Content: main, Row: 11, Column: 17
                Type: BRACE_OPEN, Row: 11, Column: 22
                Type: IDENTIFIER, Content: println, Row: 12, Column: 9
                Type: BRACE_OPEN, Row: 12, Column: 17
                Type: STRING_LITERAL, Content: "Hello, world!", Row: 12, Column: 18
                Type: BRACE_CLOSED, Row: 12, Column: 34
                Type: SEMI_COLON, Row: 12, Column: 35
                Type: INT_LITERAL, Content: 10, Row: 13, Column: 15
                Type: SEMI_COLON, Row: 13, Column: 17
                Type: FLOAT_LITERAL, Content: 3.14, Row: 14, Column: 19
                Type: SEMI_COLON, Row: 14, Column: 23
                Type: CHAR_LITERAL, Content: 'A', Row: 15, Column: 17
                Type: SEMI_COLON, Row: 15, Column: 20
                Type: STRING_LITERAL, Content: "Sample string", Row: 16, Column: 19
                Type: SEMI_COLON, Row: 16, Column: 34
                Type: BOOL, Row: 17, Column: 17
                Type: IDENTIFIER, Content: flag, Row: 17, Column: 22
                Type: ASSIGNMENT, Row: 17, Column: 27
                Type: TRUE, Row: 17, Column: 29
                Type: LOGICAL_AND, Row: 17, Column: 34
                Type: FALSE, Row: 17, Column: 37
                Type: SEMI_COLON, Row: 17, Column: 42
                Type: BRACE_CLOSED, Row: 18, Column: 5
                Type: BRACE_CLOSED, Row: 19, Column: 1
                Type: END, Row: 0, Column: 0
                """;

        String actualTokens = tokenStream.getTokensAsString();
        assertEquals(expectedTokens.trim(), actualTokens.trim(), "Tokens as string should match expected output.");
    }
}
