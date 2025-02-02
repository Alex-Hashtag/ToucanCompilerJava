package org.alex_hashtag.internal_representation.utils;

import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility class for working with lists.
 */
public final class ListUtils {

    // Prevent instantiation.
    private ListUtils() {}

    /**
     * A generic immutable triple.
     *
     * @param <A> The type of the left element.
     * @param <B> The type of the middle element.
     * @param <C> The type of the right element.
     */
    public record Triple<A, B, C>(A left, B middle, C right) {
        @Override
        public String toString() {
            return "Triple[" + left + ", " + middle + ", " + right + "]";
        }
    }

    /**
     * Transforms a list of {@link Pair} objects into an {@link Iterable} of {@link Triple} objects.
     * <p>
     * Each {@code Triple<T, U, T>} is built from consecutive pairs in the list as follows:
     * <ul>
     *   <li>The left element is the left element of the current pair.
     *   <li>The middle element is the middle element (of type U) from the current pair.
     *   <li>The right element is the left element of the next pair.
     * </ul>
     * <p>
     * This excludes the last operator from the last pair.
     *
     * @param <T>   the type for the left and right elements.
     * @param <U>   the type for the middle element.
     * @param pairs the list of pairs.
     * @return an {@link Iterable} of triples.
     */
    public static <T, U> Iterable<Triple<T, U, T>> adjacentTriples(final List<Pair<T, U>> pairs) {
        return () -> new Iterator<Triple<T, U, T>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < pairs.size() - 1;
            }

            @Override
            public Triple<T, U, T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Pair<T, U> currentPair = pairs.get(index);
                Pair<T, U> nextPair = pairs.get(index + 1);

                Triple<T, U, T> triple = new Triple<>(
                        currentPair.getLeft(),
                        currentPair.getRight(),
                        nextPair.getLeft());
                index++;
                return triple;
            }
        };
    }


    /**
     * Transforms a list of {@link Pair} objects into a new {@code List} of {@link Pair} objects,
     * where the new pairs are built by "removing" the first element of the flattened sequence (the
     * first element of the first pair) and the last element (the second element of the last pair).
     *
     * In other words, for an input list of pairs:
     * <pre>
     *     p0 = (a, b), p1 = (c, d), p2 = (e, f)
     * </pre>
     * the flattened list is:
     * <pre>
     *     [a, b, c, d, e, f]
     * </pre>
     * After removing the first (a) and last (f) elements, the remaining list is:
     * <pre>
     *     [b, c, d, e]
     * </pre>
     * which gets regrouped into new pairs:
     * <pre>
     *     (b, c) and (d, e)
     * </pre>
     * Notice that the new pairs have type {@code Pair<U, T>} because
     * <ul>
     *     <li><b>b</b> is the original second element of the first pair (of type U), and
     *     <li><b>c</b> is the original first element of the second pair (of type T).
     * </ul>
     *
     * @param <T>   the type of the first element in the input pairs
     * @param <U>   the type of the second element in the input pairs
     * @param pairs the list of pairs to process
     * @return a list of pairs of type {@code Pair<U, T>}
     */
    public static <T, U> List<Pair<U, T>> flipAndTrim(List<Pair<T, U>> pairs) {
        List<Pair<U, T>> result = new ArrayList<>();
        // For each adjacent pair in the original list,
        // use the current pair's second element (U) and the next pair's first element (T)
        for (int i = 0; i < pairs.size() - 1; i++) {
            U first = pairs.get(i).getRight();
            T second = pairs.get(i + 1).getLeft();
            result.add(Pair.create(first, second));
        }
        return result;
    }

}
