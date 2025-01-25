package org.alex_hashtag.errors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TokenizationErrorManagerTest {

    private TokenizationErrorManager errorManager;
    private final String fileName = "TestFile.toucan";
    private final String source = "package com.example;\nint main() { return 0; }";

    @BeforeEach
    void setUp() {
        errorManager = new TokenizationErrorManager(fileName, source);
    }

    @Test
    void testInitialState_NoErrors() {
        assertFalse(errorManager.hasErrors(), "Error manager should have no errors initially.");
    }

    @Test
    void testReportError() {
        TokenizationErrorManager.TokenizationError error = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.INVALID_TOKEN,
                "Unrecognized token '@'",
                2,
                5,
                "@",
                "Remove the invalid token or replace it with a valid one."
        );

        errorManager.reportError(error);

        assertTrue(errorManager.hasErrors(), "Error manager should report that it has errors.");
    }

    @Test
    void testMultipleErrors() {
        TokenizationErrorManager.TokenizationError error1 = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.INVALID_TOKEN,
                "Unrecognized token '@'",
                2,
                5,
                "@",
                "Remove the invalid token or replace it with a valid one."
        );

        TokenizationErrorManager.TokenizationError error2 = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.UNCLOSED_STRING,
                "String literal not closed.",
                3,
                10,
                "\"Hello",
                "Ensure all string literals are properly closed with a double quote."
        );

        errorManager.reportError(error1);
        errorManager.reportError(error2);

        assertTrue(errorManager.hasErrors(), "Error manager should report that it has errors.");
    }

    @Test
    void testPrintErrors_NoErrors() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            errorManager.printErrors(System.out);
            assertEquals("", outContent.toString(), "No errors should produce no output.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testPrintErrors_WithErrors() {
        TokenizationErrorManager.TokenizationError error = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.INVALID_TOKEN,
                "Unrecognized token '@'",
                2,
                5,
                "@",
                "Remove the invalid token or replace it with a valid one."
        );

        errorManager.reportError(error);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            errorManager.printErrors(System.err);
            String expectedOutput = "\u001B[1m\u001B[31merror [TestFile.toucan]: \u001B[0mUnrecognized token '@'\n" +
                    "  --> line 2:5\n" +
                    "   |\n" +
                    " 2 | int main() { return 0; }\n" +
                    "   |     ^\u001B[1m\u001B[32m\u001B[0m\n" +
                    "  = help: Remove the invalid token or replace it with a valid one.\n\n";

            assertEquals(expectedOutput, errContent.toString(), "Printed error output does not match expected.");
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testPrintErrors_WithMultipleErrors() {
        TokenizationErrorManager.TokenizationError error1 = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.INVALID_TOKEN,
                "Unrecognized token '@'",
                2,
                5,
                "@",
                "Remove the invalid token or replace it with a valid one."
        );

        TokenizationErrorManager.TokenizationError error2 = new TokenizationErrorManager.TokenizationError(
                TokenizationErrorManager.ErrorType.UNCLOSED_STRING,
                "String literal not closed.",
                3,
                10,
                "\"Hello",
                "Ensure all string literals are properly closed with a double quote."
        );

        errorManager.reportError(error1);
        errorManager.reportError(error2);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            errorManager.printErrors(System.err);
            String expectedOutput =
                    "\u001B[1m\u001B[31merror [TestFile.toucan]: \u001B[0mUnrecognized token '@'\n" +
                            "  --> line 2:5\n" +
                            "   |\n" +
                            " 2 | int main() { return 0; }\n" +
                            "   |     ^\u001B[1m\u001B[32m\u001B[0m\n" +
                            "  = help: Remove the invalid token or replace it with a valid one.\n\n" +
                            "\u001B[1m\u001B[31merror [TestFile.toucan]: \u001B[0mString literal not closed.\n" +
                            "  --> line 3:10\n" +
                            "   |\n" +
                            " 3 | // Some code \"Hello\n" +
                            "   |          ^^^\u001B[1m\u001B[32m\u001B[0m\n" +
                            "  = help: Ensure all string literals are properly closed with a double quote.\n\n";

            assertEquals(expectedOutput, errContent.toString(), "Printed errors output does not match expected.");
        } finally {
            System.setErr(originalErr);
        }
    }
}
