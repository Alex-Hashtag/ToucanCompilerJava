package org.alex_hashtag.lib.results;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Rust-inspired Result type for Java 23.
 * It represents either a successful value (Ok) or an error (Err),
 * where the error type can be any type.
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Err {

    /**
     * Represents a successful result.
     * The record component is named `ok` so you can pattern-match:
     * <pre>
     *   case Ok(var ok) -> ...
     * </pre>
     */
    record Ok<T, E>(T ok) implements Result<T, E> {}

    /**
     * Represents an error result.
     */
    record Err<T, E>(E error) implements Result<T, E> {}

    default boolean isOk() {
        return switch (this) {
            case Ok(var ok) -> true;
            case Err(var error) -> false;
        };
    }

    default boolean isErr() {
        return switch (this) {
            case Ok(var ok) -> false;
            case Err(var error) -> true;
        };
    }

    /**
     * Unwraps this Result, returning the contained value if Ok,
     * or throwing a RuntimeException if Err.
     */
    default T unwrap() {
        return switch (this) {
            case Ok(var ok) -> ok;
            case Err(var error) -> throw new RuntimeException("Tried to unwrap an Err: " + error);
        };
    }

    /**
     * Returns the contained value if Ok, or the provided default if Err.
     */
    default T unwrapOr(T defaultValue) {
        return switch (this) {
            case Ok(var ok) -> ok;
            case Err(var error) -> defaultValue;
        };
    }

    /**
     * Returns the contained value if Ok, or computes a default using the supplier if Err.
     */
    default T unwrapOrElse(Supplier<T> fallback) {
        return switch (this) {
            case Ok(var ok) -> ok;
            case Err(var error) -> fallback.get();
        };
    }

    /**
     * Maps an Ok value using the provided mapper, leaving an Err unchanged.
     */
    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
            case Ok(var ok) -> new Ok<>(mapper.apply(ok));
            case Err(var error) -> new Err<>(error);
        };
    }

    /**
     * Maps an Err value using the provided mapper, leaving an Ok unchanged.
     */
    default <F> Result<T, F> mapErr(Function<E, F> mapper) {
        return switch (this) {
            case Ok(var ok) -> new Ok<>(ok);
            case Err(var error) -> new Err<>(mapper.apply(error));
        };
    }

    /**
     * Executes the provided consumer if this is an Ok.
     */
    default void ifOk(Consumer<T> consumer) {
        if (this instanceof Ok(var ok)) {
            consumer.accept(ok);
        }
    }

    /**
     * Executes the provided consumer if this is an Err.
     */
    default void ifErr(Consumer<E> consumer) {
        if (this instanceof Err(var error)) {
            consumer.accept(error);
        }
    }

    /** Factory method to create an Ok result. */
    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    /** Factory method to create an Err result. */
    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }
}
