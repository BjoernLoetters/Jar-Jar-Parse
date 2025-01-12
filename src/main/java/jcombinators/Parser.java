package jcombinators;

import jcombinators.data.Tuple;
import jcombinators.description.Description;
import jcombinators.description.Negation;
import jcombinators.description.Unknown;
import jcombinators.input.Input;
import jcombinators.primitive.*;
import jcombinators.result.*;
import jcombinators.result.Error;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface Parser<T> extends Function<Input, Result<T>> {

    public abstract Result<T> apply(final Input input);

    public default Description description() {
        return new Unknown();
    }

    public default Parser<List<T>> repeat() {
        return new RepeatParser<>(this);
    }

    public default Parser<List<T>> repeat1() {
        return this.and(this.repeat()).map(tuple -> Stream.concat(Stream.of(tuple.first()), tuple.second().stream()).collect(Collectors.toList()));
    }

    public default <U> Parser<U> map(final Function<T, U> function) {
        return new MapParser<>(this, function);
    }

    public default <U> Parser<U> flatMap(final Function<T, Parser<U>> function) {
        return new FlatMapParser<>(this, function);
    }

    public default <U> Parser<U> andr(final Parser<U> parser) {
        return this.flatMap(ignore -> parser);
    }

    public default <U> Parser<T> andl(final Parser<U> parser) {
        return this.flatMap(result -> parser.map(ignore -> result));
    }

    public default <U> Parser<Tuple<T, U>> and(final Parser<U> parser) {
        return this.flatMap(first -> parser.map(second -> new Tuple<>(first, second)));
    }

    public default Parser<T> commit() {
        return (input -> switch (apply(input)) {
            case Success<T> success -> success;
            case Error<T> error -> new Abort<>(error.message, error.rest);
            case Abort<T> abort -> abort;
        });
    }

    public default Parser<Void> not() {
        return (input -> switch (apply(input)) {
            case Success<T> success -> new Error<>(Failure.format(input, new Negation(this.description())), input);
            case Failure<T> ignore -> new Success<>(null, input);
        });
    }

    public default Parser<Optional<T>> optional() {
        return (input -> switch(apply(input)) {
            case Success<T> success -> new Success<>(Optional.of(success.value), success.rest);
            case Error<T> error -> new Success<>(Optional.empty(), error.rest);
            case Abort<T> abort -> {
                @SuppressWarnings("unchecked")
                final Abort<Optional<T>> result = (Abort<Optional<T>>) abort;
                yield result;
            }
        });
    }

    public default <U> Parser<List<T>> separate(final Parser<U> separator) {
        return separate1(separator).optional().map(optional -> optional.orElse(List.of()));
    }

    public default <U> Parser<List<T>> separate1(final Parser<U> separator) {
        return this.and(separator.and(this).repeat())
                .map(tuple -> Stream.concat(Stream.of(tuple.first()), tuple.second().stream().map(Tuple::second)).collect(Collectors.toList()));
    }

    public default Parser<T> or(final Parser<T> alternative) {
        return or(this, alternative);
    }

    public static <T> Parser<T> chainl(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(tuple -> {
            T result = tuple.first();

            for (Tuple<BiFunction<T, T, T>, T> next : tuple.second()) {
                result = next.first().apply(result, next.second());
            }

            return result;
        });
    }

    public static <T> Parser<T> chainr(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
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

    public static <T> Parser<T> success(final T value) {
        return (input -> new Success<>(value, input));
    }

    public static <T> Parser<T> fail(final String message) {
        return (input -> new Error<>(message, input));
    }

    public static <T> Parser<T> abort(final String message) {
        return (input -> new Abort<>(message, input));
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
