package jcombinators.data;

import java.util.function.BiFunction;
import java.util.function.Function;
import jcombinators.Parsing.Parser;

/**
 * A simple representation of a {@link Product} (or pair) as a record. This class is primarily used by the combinator
 * {@link Parser#and} to return two values without losing the type information and to offer convenient operations
 * for the mapping and reduction of these two values.
 *
 * @param first The first or left value of this {@link Product}.
 * @param second The second or right value of this {@link Product}.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 *
 * @see Parser#and
 */
public record Product<A, B>(A first, B second) {

    /**
     * Maps this {@link Product} to a new one by mapping its values using the provided functions.
     * @param first The function that is used to map the first value of this {@link Product}.
     * @param second The function that is used to map the second value of this {@link Product}.
     * @return A new {@link Product} whose values correspond to the mapped values of this {@link Product}.
     * @param <C> The target type of the first value.
     * @param <D> The target type of the second value.
     */
    public <C, D> Product<C, D> map(final Function<A, C> first, final Function<B, D> second) {
        return of(first.apply(this.first), second.apply(this.second));
    }

    /**
     * Reduces this {@link Product} to a value by applying the provided function to this {@link Product}'s values.
     * @param function The function that is used to fold the first and second value to a new value.
     * @return The result of applying the provided function to this {@link Product}'s values.
     * @param <C> The type of the resulting value.
     */
    public <C> C fold(final BiFunction<A, B, C> function) {
        return function.apply(first, second);
    }

    /**
     * A shorthand for constructing a new {@link Product}.
     * @param first The first or left value of this {@link Product}.
     * @param second The second or right value of this {@link Product}.
     * @return A new {@link Product} which consists of the provided values.
     * @param <A> The type of the first value.
     * @param <B> The type of the second value.
     */
    public static <A, B> Product<A, B> of(final A first, final B second) {
        return new Product<>(first, second);
    }

}
