package org.alex_hashtag.lib.errors;

/**
 * Interface for all compiler errors, ensuring a consistent structure.
 */
public interface CompilerError {
    String getMessage();
    int getLine();
    int getColumn();
    String getToken();
    String getHint();
}
