package jpc;

import jpc.data.Product;
import jpc.parsers.*;
import jpc.result.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface Parser<T> {

    public abstract Result<T> apply(final String input, final int offset);

    public default Parser<List<T>> repeat() {
        return new RepeatParser<>(this);
    }

    public default Parser<List<T>> repeat1() {
        return this.and(this.repeat()).map(product -> Stream.concat(Stream.of(product.first()), product.second().stream()).collect(Collectors.toList()));
    }

    public default <U> Parser<U> map(final Function<T, U> function) {
        return new MapParser<>(this, function);
    }

    public default <U> Parser<U> flatMap(final Function<T, Parser<U>> function) {
        return new FlatMapParser<>(this, function);
    }

    public default <U> Parser<U> keepRight(final Parser<U> parser) {
        return this.flatMap(ignore -> parser);
    }

    public default <U> Parser<T> keepLeft(final Parser<U> parser) {
        return this.flatMap(result -> parser.map(ignore -> result));
    }

    public default <U> Parser<Product<T, U>> and(final Parser<U> parser) {
        return this.flatMap(first -> parser.map(second -> new Product<>(first, second)));
    }

    @SafeVarargs
    public static <T> Parser<T> or(final Parser<? extends T> alternative, final Parser<? extends T> ... alternatives) {
        @SuppressWarnings("unchecked")
        Parser<T> choice = (Parser<T>) alternative;

        for (int i = 0; i < alternatives.length; ++i) {
            @SuppressWarnings("unchecked")
            final Parser<T> parser = (Parser<T>) alternatives[i];
            choice = new ChoiceParser<>(choice, parser);
        }

        return choice;
    }

    @SafeVarargs
    public static <T> Parser<List<T>> sequence(final Parser<? extends T> ... parsers) {
        final List<Parser<T>> sequence = Stream.of(parsers).map(parser -> {
            @SuppressWarnings("unchecked")
            final Parser<T> up = (Parser<T>) parser;
            return up;
        }).collect(Collectors.toList());

        return new SequenceParser<>(sequence);
    }

}
