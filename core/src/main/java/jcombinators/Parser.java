package jcombinators;

import jcombinators.data.Tuple;
import jcombinators.description.Description;
import jcombinators.description.Negation;
import jcombinators.description.Empty;
import jcombinators.input.Input;
import jcombinators.position.Position;
import jcombinators.primitive.*;
import jcombinators.result.*;
import jcombinators.result.Error;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface Parser<T> extends Function<Input, Result<T>> {

    Result<T> apply(final Input input);

    default Description description() {
        return new Empty();
    }

    default Parser<List<T>> repeat() {
        return new RepeatParser<>(this);
    }

    default Parser<List<T>> repeat1() {
        return this.and(this.repeat()).map(tuple -> tuple.map(Stream::of, List::stream).fold(Stream::concat).collect(Collectors.toList()));
    }

    default <U> Parser<U> map(final Function<T, U> function) {
        return new MapParser<>(this, function);
    }

    default <U> Parser<U> flatMap(final Function<T, Parser<U>> function) {
        return new FlatMapParser<>(this, function);
    }

    default <U> Parser<U> keepRight(final Parser<U> parser) {
        return this.flatMap(ignore -> parser);
    }

    default <U> Parser<T> keepLeft(final Parser<U> parser) {
        return this.flatMap(result -> parser.map(ignore -> result));
    }

    default <U> Parser<Tuple<T, U>> and(final Parser<U> parser) {
        return this.flatMap(first -> parser.map(second -> Tuple.of(first, second)));
    }

    default Parser<T> commit() {
        return (input -> switch (apply(input.skipWhiteSpace())) {
            case Success<T> success -> success;
            case Error<T> error -> new Abort<>(error.message, error.rest);
            case Abort<T> abort -> abort;
        });
    }

    default Parser<Void> not() {
        return (input -> {
            input = input.skipWhiteSpace();
            return switch (apply(input)) {
                case Success<T> ignore -> new Error<>(Failure.format(input, new Negation(this.description())), input);
                case Failure<T> ignore -> new Success<>(null, input);
            };
        });
    }

    default Parser<Optional<T>> optional() {
        return (input -> switch(apply(input.skipWhiteSpace())) {
            case Success<T> success -> new Success<>(Optional.of(success.value), success.rest);
            case Error<T> error -> new Success<>(Optional.empty(), error.rest);
            case Abort<T> abort -> {
                @SuppressWarnings("unchecked")
                final Abort<Optional<T>> result = (Abort<Optional<T>>) abort;
                yield result;
            }
        });
    }

    default <U> Parser<List<T>> separate(final Parser<U> separator) {
        return separate1(separator).optional().map(optional -> optional.orElse(List.of()));
    }

    default <U> Parser<List<T>> separate1(final Parser<U> separator) {
        return this.and(separator.and(this).repeat())
                .map(tuple -> tuple.map(Stream::of, list -> list.stream().map(Tuple::second)).fold(Stream::concat).toList());
    }

    default Parser<T> or(final Parser<T> alternative) {
        return or(this, alternative);
    }

    default Parser<T> between(final Parser<?> left, final Parser<?> right) {
        return left.keepRight(this).keepLeft(right);
    }

    static <T> Parser<T> chainLeft1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(tuple -> {
            T result = tuple.first();

            for (Tuple<BiFunction<T, T, T>, T> next : tuple.second()) {
                result = next.first().apply(result, next.second());
            }

            return result;
        });
    }

    static <T> Parser<T> chainLeft(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainLeft1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    static <T> Parser<T> chainRight1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(tuple -> {
            if (tuple.second().isEmpty()) {
                return tuple.first();
            } else {
                final List<Tuple<BiFunction<T, T, T>, T>> reversed = tuple.second().reversed();
                final Iterator<Tuple<BiFunction<T, T, T>, T>> iterator = reversed.iterator();

                final Tuple<BiFunction<T, T, T>, T> first = iterator.next();
                T result = first.second();
                BiFunction<T, T, T> combiner = first.first();

                while (iterator.hasNext()) {
                    final Tuple<BiFunction<T, T, T>, T> next = iterator.next();
                    result = combiner.apply(next.second(), result);
                    combiner = next.first();
                }

                result = combiner.apply(tuple.first(), result);
                return result;
            }
        });
    }

    static <T> Parser<T> chainRight(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainLeft1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    static <T> Parser<T> success(final T value) {
        return (input -> new Success<>(value, input.skipWhiteSpace()));
    }

    static <T> Parser<T> fail(final String message) {
        return (input -> new Error<>(message, input.skipWhiteSpace()));
    }

    static <T> Parser<T> abort(final String message) {
        return (input -> new Abort<>(message, input.skipWhiteSpace()));
    }

    @SafeVarargs
    static <T> Parser<T> or(final Parser<? extends T> alternative, final Parser<? extends T>... alternatives) {
        @SuppressWarnings("unchecked")
        Parser<T> choice = (Parser<T>) alternative;

        for (final Parser<? extends T> value : alternatives) {
            @SuppressWarnings("unchecked")
            final Parser<T> parser = (Parser<T>) value;
            choice = new ChoiceParser<>(choice, parser);
        }

        return choice;
    }

    @SafeVarargs
    static <T> Parser<List<T>> sequence(final Parser<? extends T>... parsers) {
        final List<Parser<T>> sequence = Stream.of(parsers).map(parser -> {
            @SuppressWarnings("unchecked")
            final Parser<T> up = (Parser<T>) parser;
            return up;
        }).collect(Collectors.toList());

        return new SequenceParser<>(sequence);
    }

    static <T> Parser<T> position(final Parser<Function<Position, T>> parser) {
        return input -> {
            input = input.skipWhiteSpace();
            final Position position = input.position;
            return parser.apply(input).map(function -> function.apply(position));
        };
    }

    static <T> Parser<T> lazy(final Supplier<Parser<T>> supplier) {
        return new LazyParser<>(supplier);
    }

}
