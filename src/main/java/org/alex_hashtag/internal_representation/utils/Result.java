package org.alex_hashtag.internal_representation.utils;

/**
 * A tagged union type representing either a success ({@code Ok<T, E>}) or an error ({@code Err<T, E>}).
 * This is a generic alternative to exceptions for handling errors in a functional style.
 *
 * @param <T> The type of the success value.
 * @param <E> The type of the error value.
 */
public sealed interface Result<T, E> permits Ok, Err {

    /**
     * Checks if the result is successful ({@code Ok}).
     *
     * @return {@code true} if this instance is {@code Ok}, otherwise {@code false}.
     */
    default boolean isOk() {
        return this instanceof Ok<T, E>;
    }

    /**
     * Checks if the result represents an error ({@code Err}).
     *
     * @return {@code true} if this instance is {@code Err}, otherwise {@code false}.
     */
    default boolean isErr() {
        return this instanceof Err<T, E>;
    }

    /**
     * Extracts the success value if this instance is {@code Ok}, otherwise throws an exception.
     *
     * @return The success value.
     * @throws IllegalStateException if this instance is {@code Err}.
     */
    default T unwrap() throws IllegalStateException {
        return switch (this) {
            case Ok<T, E>(T value) -> value;
            case Err<T, E>(E error) -> throw new IllegalStateException("Attempted to unwrap an Err: " + error);
        };
    }

    /**
     * Extracts the error value if this instance is {@code Err}, otherwise throws an exception.
     *
     * @return The error value.
     * @throws IllegalStateException if this instance is {@code Ok}.
     */
    default E unwrapErr() throws IllegalStateException {
        return switch (this) {
            case Err<T, E>(E error) -> error;
            case Ok<T, E>(T value) -> throw new IllegalStateException("Attempted to unwrap an Ok: " + value);
        };
    }

    /**
     * Transforms the success value if this instance is {@code Ok}, otherwise returns the error.
     *
     * @param <U> The new type of the success value.
     * @param mapper A function that transforms the success value.
     * @return A new {@code Result} with the mapped success value, or the same {@code Err}.
     */
    default <U> Result<U, E> map(java.util.function.Function<T, U> mapper) {
        return switch (this) {
            case Ok<T, E>(T value) -> new Ok<>(mapper.apply(value));
            case Err<T, E>(E error) -> new Err<>(error);
        };
    }

    /**
     * Transforms the success value into another {@code Result}, allowing for chaining operations.
     *
     * @param <U> The new type of the success value.
     * @param mapper A function that maps the success value to another {@code Result}.
     * @return The result of applying the function, or the same {@code Err}.
     */
    default <U> Result<U, E> flatMap(java.util.function.Function<T, Result<U, E>> mapper) {
        return switch (this) {
            case Ok<T, E>(T value) -> mapper.apply(value);
            case Err<T, E>(E error) -> new Err<>(error);
        };
    }
}
