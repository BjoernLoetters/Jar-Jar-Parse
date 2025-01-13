package jcombinators.data;

import java.util.function.BiFunction;
import java.util.function.Function;

public record Tuple<A, B>(A first, B second) {

    public <C, D> Tuple<C, D> map(final Function<A, C> firstFunction, final Function<B, D> secondFunction) {
        return of(firstFunction.apply(first), secondFunction.apply(second));
    }

    public <C> C fold(final BiFunction<A, B, C> function) {
        return function.apply(first, second);
    }

    public static <A, B> Tuple<A, B> of(final A first, final B second) {
        return new Tuple<>(first, second);
    }

}
