package org.alex_hashtag.lib.results;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Rust-inspired Option type for Java 23.
 * It represents a value (Some) or its absence (None).
 */
public sealed interface Option<T> permits Option.Some, Option.None {

    /**
     * Represents a value present.
     * The component is named `some` so you can use:
     * <pre>
     *   case Some(var some) -> ...
     * </pre>
     */
    record Some<T>(T some) implements Option<T> {}

    /**
     * Represents an absent value.
     */
    record None<T>() implements Option<T> {}

    default boolean isSome() {
        return switch (this) {
            case Some(var some) -> true;
            case None() -> false;
        };
    }

    default boolean isNone() {
        return !isSome();
    }

    /**
     * Unwraps the Option, returning the contained value if Some,
     * or throwing a RuntimeException if None.
     */
    default T unwrap() {
        return switch (this) {
            case Some(var some) -> some;
            case None() -> throw new RuntimeException("Tried to unwrap a None");
        };
    }

    /**
     * Returns the contained value if Some, or the provided default if None.
     */
    default T unwrapOr(T defaultValue) {
        return switch (this) {
            case Some(var some) -> some;
            case None() -> defaultValue;
        };
    }

    /**
     * Returns the contained value if Some, or computes a default using the supplier if None.
     */
    default T unwrapOrElse(Supplier<T> fallback) {
        return switch (this) {
            case Some(var some) -> some;
            case None() -> fallback.get();
        };
    }

    /**
     * Maps a Some value using the provided mapper,
     * leaving a None unchanged.
     */
    default <U> Option<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Some(var some) -> new Some<>(mapper.apply(some));
            case None() -> new None<>();
        };
    }

    /**
     * Executes the consumer if this is a Some.
     */
    default void ifSome(Consumer<T> consumer) {
        if (this instanceof Some(var some)) {
            consumer.accept(some);
        }
    }

    /** Factory method to create a Some. */
    static <T> Option<T> some(T value) {
        return new Some<>(value);
    }

    /** Factory method to create a None. */
    static <T> Option<T> none() {
        return new None<>();
    }
}
