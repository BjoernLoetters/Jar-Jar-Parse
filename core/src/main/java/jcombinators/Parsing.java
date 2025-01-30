package jcombinators;

import jcombinators.data.Product;
import jcombinators.description.Choice;
import jcombinators.description.Description;
import jcombinators.description.Empty;
import jcombinators.input.Input;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An abstract class that provides all basic parsing capabilities.
 * <br/><br/>
 * In order to implement a parser, one must extend this class and specify the corresponding input type. The subclass
 * then has access to the {@link Parsing.Parser} class and the {@link Parsing.Result} class, which are specialized for
 * the respective input type.
 *
 * @see Parsing.Result
 * @see Parsing.Parser
 *
 * @author Björn Lötters
 *
 * @param <I> The input type or, more specifically, the type of a single element in the underlying input stream.
 */
public abstract class Parsing<I> {

    /**
     * Represents the result of a parsing operation.
     * <br/><br/>
     * A {@link Result} can either be a {@link Success} or a {@link Failure}, whereas a failure is further divided into:
     * <ul>
     *     <li>{@link Error}: A recoverable kind of {@link Failure} that may trigger backtracking during parsing.</li>
     *     <li>{@link Abort}: A fatal kind of {@link Failure} that does not trigger backtracking and aborts parsing.</li>
     * </ul>
     *
     * @see Success
     * @see Failure
     * @see Input
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the parsed value in case of a {@link Success}.
     */
    public sealed abstract class Result<T> permits Success, Failure {

        /** The rest of the {@link Input}. */
        public final Input<I> rest;

        /**
         * The base constructor for {@link Result}.
         * @param rest The rest of the {@link Input}.
         */
        public Result(final Input<I> rest) {
            this.rest = rest;
        }

        /**
         * Returns the value of this {@link Result}, if it is present.
         * @return The optional value of this {@link Result}.
         */
        public abstract Optional<T> get();

        /**
         * Returns the value of this {@link Result} or fails with an exception, if it is not present.
         * @return The value of this {@link Result}.
         * @throws NoSuchElementException If this {@link Result} is not a {@link Success}.
         */
        public abstract T getOrFail() throws NoSuchElementException;

        /**
         * Checks whether this {@link Result} is a {@link Failure}.
         * @return {@code true} if this {@link Result} is a {@link Failure}.
         */
        public abstract boolean isFailure();

        /**
         * Checks whether this {@link Result} is a {@link Success}.
         * @return {@code true} if this {@link Result} is a {@link Success}.
         */
        public abstract boolean isSuccess();

        /**
         * Maps the value of this {@link Result} in case it is a {@link Success} using the provided {@link Function}.
         * @param function The {@link Function} used to map the value.
         * @return A {@link Result} which contains the mapped value.
         * @param <U> The type of the mapped value.
         */
        public abstract <U> Result<U> map(final Function<T, U> function);

        /**
         * Maps the value of this {@link Result} in case it is a {@link Success} to another {@link Result} using the
         * provided {@link Function} and flattens the nested {@link Result}.
         * @param function The {@link Function} used to map the value.
         * @return The {@link Result} of the mapped value.
         * @param <U> The type of the returned {@link Result}.
         */
        public abstract <U> Result<U> flatMap(final Function<T, Result<U>> function);

    }

    /**
     * Represents a successful result of a parsing operation.
     *
     * @see Result
     * @see Failure
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the parsed value.
     */
    public final class Success<T> extends Result<T> {

        /** The parsed value of this {@link Success}. */
        public final T value;

        /**
         * Constructs a new {@link Success}.
         * @param value The parsed value.
         * @param rest The rest of the {@link Input}.
         */
        public Success(final T value, final Input<I> rest) {
            super(rest);
            this.value = value;
        }

        @Override
        public Optional<T> get() {
            return Optional.of(value);
        }

        @Override
        public T getOrFail() throws NoSuchElementException {
            return value;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public <U> Result<U> map(final Function<T, U> function) {
            return new Success<>(function.apply(value), rest);
        }

        @Override
        public <U> Result<U> flatMap(final Function<T, Result<U>> function) {
            return function.apply(value);
        }

    }

    /**
     * Represents a {@link Result} which indicates a parse failure.
     * <br><br>
     * Instead of the parsed value, a {@link Failure} only carries an error message that describes the failure in detail.
     * Moreover, a {@link Failure} comes in two variants:
     * <ul>
     *     <li>{@link Error}: A recoverable kind of {@link Failure} that may trigger backtracking during parsing.</li>
     *     <li>{@link Abort}: A fatal kind of {@link Failure} that does not trigger backtracking and aborts parsing.</li>
     * </ul>
     *
     * @see Result
     * @see Success
     * @see Error
     * @see Abort
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the parsed value, which is unused in case of a {@link Failure}.
     */
    public sealed abstract class Failure<T> extends Result<T> {

        /** The error message that describes this {@link Failure} in more detail. */
        public final String message;


        /**
         * The base constructor for a {@link Failure}.
         * @param message The error message that describes this {@link Failure} in more detail.
         * @param rest The rest of the {@link Input}.
         */
        public Failure(final String message, final Input<I> rest) {
            super(rest);
            this.message = message;
        }

        @Override
        public Optional<T> get() {
            return Optional.empty();
        }

        @Override
        public T getOrFail() throws NoSuchElementException {
            throw new NoSuchElementException(message);
        }

        @Override
        public final boolean isFailure() {
            return true;
        }

        @Override
        public final boolean isSuccess() {
            return false;
        }

        /**
         * Checks whether this {@link Failure} is fatal in the sense that no backtracking shall be applied during
         * parsing.
         * @return {@code true} if this {@link Failure} is an {@link Abort} and {@code false} if it is an {@link Error}.
         */
        public abstract boolean isFatal();


        @Override
        public <U> Result<U> map(final Function<T, U> function) {
            // Since failures never carry a value of their type parameter, this cast always succeeds.
            @SuppressWarnings("unchecked")
            final Failure<U> failure = (Failure<U>) this;
            return failure;
        }

        @Override
        public <U> Result<U> flatMap(final Function<T, Result<U>> function) {
            // Since failures never carry a value of their type parameter, this cast always succeeds.
            @SuppressWarnings("unchecked")
            final Failure<U> failure = (Failure<U>) this;
            return failure;
        }

        /**
         * Format an error message for the provided {@link Input} and {@link Description}.
         * @param input The related {@link Input} of the error message.
         * @param description The {@link Description} of the {@link Parser} that failed.
         * @return A formatted error message that provides details about the location and kind of the error.
         */
        public static String format(final Input<?> input, final Description description) {
            final Optional<String> expected = description.normalize().describe();
            if (expected.isEmpty()) {
                return String.format("syntax error in %s: unexpected %s", input.position(), input.position().describe());
            } else {
                return String.format("syntax error in %s: unexpected %s, expected %s", input.position(), input.position().describe(), expected.get());
            }
        }

    }

    /**
     * Represents a recoverable {@link Failure}.
     * <br/><br/>
     * An {@link Error} may lead to backtracking during parsing. In particular, when a {@link ChoiceParser} fails with
     * an {@link Error} on its first alternative, it tries to apply the second one.
     *
     * @see Result
     * @see Failure
     * @see Abort
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the parsed value, which is unused in case of an {@link Error}.
     */
    public final class Error<T> extends Failure<T> {

        public Error(final String message, final Input<I> rest) {
            super(message, rest);
        }

        @Override
        public boolean isFatal() {
            return false;
        }

    }

    /**
     * Represents a fatal {@link Failure}.
     * <br/><br/>
     * An {@link Abort} prevents backtracking during parsing. In particular, when a {@link ChoiceParser} fails with
     * an {@link Abort} on its first alternative, it does not attempt to apply the second one.
     *
     * @see Result
     * @see Failure
     * @see Error
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the parsed value, which is unused in case of an {@link Abort}.
     */
    public final class Abort<T> extends Failure<T> {

        public Abort(final String message, final Input<I> rest) {
            super(message, rest);
        }

        @Override
        public boolean isFatal() {
            return true;
        }

    }

    /**
     * The abstract base class of a {@link Parser}.
     * <br/><br/>
     * Very similar to recursive descent parsing, a {@link Parser} is just a {@link Function} that takes an
     * {@link Input} and produces a {@link Result}. That is to say, in order to implement a {@link Parser} it
     * suffices to create an anonymous class that extends this class, implementing the {@link Parser#apply}
     * method.
     *
     * @implNote Unfortunately, Java does not support instance interfaces which is why this class is not a
     * {@link FunctionalInterface} and we cannot use the lambda syntax to implement a {@link Parser}.
     *
     * @author Björn Lötters
     *
     * @param <T> The type of the value that is the result of running this parser.
     */
    public abstract class Parser<T> implements Function<Input<I>, Result<T>> {

        /**
         * Applies this {@link Parser} to the provided {@link Input}, returning a {@link Result}.
         * @param input The {@link Input} which this {@link Parser} shall parse.
         * @return The {@link Result} of the parsing operation.
         */
        @Override
        public abstract Result<T> apply(final Input<I> input);

        public Description description() {
            return new Empty();
        }

        /**
         * Maps this {@link Parser} by applying the provided {@link Function} to the {@link Result}s it produces.
         * @param function The {@link Function} which shall be applied to the {@link Result}s of this parser.
         * @return A {@link Parser} that parses the exact same language as this one, but with a transformed {@link Result}.
         * @param <U> The type of the transformed {@link Parser}.
         */
        public final <U> Parser<U> map(final Function<T, U> function) {
            return new MapParser<>(this, function);
        }

        /**
         * Maps this {@link Parser} by applying the provided {@link Function} to the {@link Result}s it produces. On
         * success, the {@link Parser} returned by this {@link Function} is immediately applied to the rest of the input.
         * In this way, this method also behaves like a concatenation of this {@link Parser} and the returned one.
         * @param function The {@link Function} which shall be applied to the {@link Result}s of this parser.
         * @return A {@link Parser} that first applies this and then the one returned by the provided
         * {@link Function}.
         * @param <U> The type of the transformed {@link Parser}.
         */
        public final <U> Parser<U> flatMap(final Function<T, Parser<U>> function) {
            return new FlatMapParser<T, U>(this, function);
        }

        public final <U> Parser<Product<T, U>> and(final Parser<U> right) {
            return flatMap(first -> right.map(second -> new Product<>(first, second)));
        }

        public final <U> Parser<T> keepLeft(final Parser<U> right) {
            return flatMap(result -> right.map(ignore -> result));
        }

        public final <U> Parser<U> keepRight(final Parser<U> right) {
            return flatMap(ignore -> right);
        }

        public final Parser<T> or(final Parser<T> parser) {
            return choice(this, parser);
        }

        public final Parser<Optional<T>> optional() {
            return map(Optional::of).or(success(Optional::empty));
        }

        public final Parser<List<T>> repeat() {
            return new RepeatParser<>(this);
        }

        public final Parser<List<T>> repeat1() {
            return this.and(repeat()).map(product -> product.map(Stream::of, List::stream).fold(Stream::concat).toList());
        }

        public final Parser<List<T>> separate1(final Parser<?> separator) {
            return this.and(separator.and(this).repeat()).map(product -> product.map(Stream::of, list -> list.stream().map(Product::second)).fold(Stream::concat).toList());
        }

        public final Parser<List<T>> separate(final Parser<?> separator) {
            return separate1(separator).optional().map(result -> result.orElseGet(List::of));
        }

        public final Parser<T> between(final Parser<?> left, final Parser<?> right) {
            return left.keepRight(this).keepLeft(right);
        }

        public final Parser<T> commit() {
            return new CommitParser<>(this);
        }

        public final Parser<Void> not() {
            return new NegationParser(this);
        }

        public final Parser<T> log(final String name) {
            return new LogParser(name, this);
        }

    }

    /* *** Public Parser API *** */

    public final <T> Result<T> parse(final Parser<T> parser, final Input<I> input) {
        final Result<T> result = parser.apply(input);

        if (result.isSuccess()) {
            if (result.rest.isEmpty()) {
                return new Success<>(result.getOrFail(), result.rest);
            } else {
                return new Error<>(Failure.format(result.rest, new Empty()), result.rest);
            }
        } else {
            return result;
        }
    }

    public final <T> Parser<T> error(final String message) {
        return new Parser<T>() {

            @Override
            public Result<T> apply(final Input<I> input) {
                return new Error<>(message, input);
            }

        };
    }

    public final <T> Parser<T> abort(final String message) {
        return new Parser<T>() {

            @Override
            public Result<T> apply(final Input<I> input) {
                return new Abort<>(message, input);
            }

        };
    }

    public final <T> Parser<T> success(final Supplier<T> supplier) {
        return new Parser<T>() {

            @Override
            public Result<T> apply(final Input<I> input) {
                return new Success<>(supplier.get(), input);
            }

        };
    }

    @SafeVarargs
    public final <T> Parser<T> choice(final Parser<? extends T>... alternatives) {
        Parser<T> choice = error("empty choice");

        for (final Parser<? extends T> parser : alternatives) {
            @SuppressWarnings("unchecked")
            final Parser<T> up = (Parser<T>) parser;
            choice = new ChoiceParser<>(choice, up);
        }

        return choice;
    }

    @SafeVarargs
    public final <T> Parser<List<T>> sequence(final Parser<? extends T>... elements) {
        Parser<List<T>> sequence = success(() -> new ArrayList<>());

        for (final Parser<? extends T> parser : elements) {
            sequence = sequence.flatMap(result -> parser.map(element -> {
                result.add(element);
                return result;
            }));
        }

        return sequence.map(Collections::unmodifiableList);
    }

    public <T> Parser<T> chainLeft1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(product -> {
            T result = product.first();

            for (Product<BiFunction<T, T, T>, T> next : product.second()) {
                result = next.first().apply(result, next.second());
            }

            return result;
        });
    }

    public <T> Parser<T> chainLeft(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainLeft1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    public <T> Parser<T> chainRight1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(product -> {
            if (product.second().isEmpty()) {
                return product.first();
            } else {
                final List<Product<BiFunction<T, T, T>, T>> reversed = product.second().reversed();
                final Iterator<Product<BiFunction<T, T, T>, T>> iterator = reversed.iterator();

                final Product<BiFunction<T, T, T>, T> first = iterator.next();
                T result = first.second();
                BiFunction<T, T, T> combiner = first.first();

                while (iterator.hasNext()) {
                    final Product<BiFunction<T, T, T>, T> next = iterator.next();
                    result = combiner.apply(next.second(), result);
                    combiner = next.first();
                }

                result = combiner.apply(product.first(), result);
                return result;
            }
        });
    }

    public <T> Parser<T> chainRight(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainRight1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    public final <T> Parser<T> position(final Parser<Function<Input<I>.Position, T>> parser) {
        return new PositionParser<>(parser);
    }

    public final <T> Parser<T> lazy(final Supplier<Parser<T>> supplier) {
        return new LazyParser<T>(supplier);
    }

    /* *** Primitive Parser Implementations *** */

    private final class MapParser<T, U> extends Parser<U> {

        private final Parser<T> parser;

        private final Function<T, U> function;

        private MapParser(final Parser<T> parser, final Function<T, U> function) {
            this.parser = parser;
            this.function = function;
        }

        @Override
        public Description description() {
            return parser.description();
        }

        @Override
        public Result<U> apply(final Input<I> input) {
            return parser.apply(input).map(function);
        }

    }

    private final class FlatMapParser<T, U> extends Parser<U> {

        private final Parser<T> parser;

        private final Function<T, Parser<U>> function;

        private FlatMapParser(final Parser<T> parser, final Function<T, Parser<U>> function) {
            this.parser = parser;
            this.function = function;
        }

        @Override
        public Description description() {
            return parser.description();
        }

        @Override
        public Result<U> apply(final Input<I> input) {
            return switch (parser.apply(input).map(function)) {
                case Success<Parser<U>> success ->
                    switch (success.value.apply(success.rest)) {
                        case Success<U> result -> result;
                        case Error<U> error -> new Error<>(error.message, input);
                        case Abort<U> abort -> new Abort<>(abort.message, input);
                    };

                case Failure<Parser<U>> failure -> {
                    @SuppressWarnings("unchecked")
                    final Failure<U> result = (Failure<U>) failure;
                    yield result;
                }
            };
        }

    }

    private final class ChoiceParser<T> extends Parser<T> {

        private final Parser<T> first;

        private final Parser<T> second;

        private ChoiceParser(final Parser<T> first, final Parser<T> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Description description() {
            return new Choice(List.of(first.description(), second.description()));
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            return switch (first.apply(input)) {
                case Success<T> success -> success;
                case Abort<T> abort -> abort;
                case Error<T> firstError -> switch (second.apply(firstError.rest)) {
                    case Success<T> success -> success;
                    case Abort<T> abort -> abort;
                    case Error<T> secondError -> new Error<>(Failure.format(secondError.rest, description()), secondError.rest);
                };
            };
        }

    }

    private final class LazyParser<T> extends Parser<T> {

        private final Supplier<Parser<T>> supplier;

        private Parser<T> parser = null;

        public LazyParser(final Supplier<Parser<T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public Description description() {
            if (parser == null) {
                parser = supplier.get();
            }

            return parser.description();
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            if (parser == null) {
                parser = supplier.get();
            }

            return parser.apply(input);
        }

    }

    private final class RepeatParser<T> extends Parser<List<T>> {

        private final Parser<T> parser;

        private RepeatParser(final Parser<T> parser) {
            this.parser = parser;
        }

        @Override
        public Description description() {
            return parser.description();
        }

        @Override
        public Result<List<T>> apply(final Input<I> input) {
            final List<T> elements = new ArrayList<>();
            Result<T> result = parser.apply(input);

            while (result.isSuccess()) {
                elements.add(result.getOrFail());
                result = parser.apply(result.rest);
            }

            return new Success<>(Collections.unmodifiableList(elements), result.rest);
        }

    }

    private final class PositionParser<T> extends Parser<T> {

        private final Parser<Function<Input<I>.Position, T>> parser;

        private PositionParser(final Parser<Function<Input<I>.Position, T>> parser) {
            this.parser = parser;
        }

        @Override
        public Description description() {
            return parser.description();
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            final Input<I>.Position position = input.position();
            return parser.apply(input).map(function -> function.apply(position));
        }

    }

    private final class CommitParser<T> extends Parser<T> {

        private final Parser<T> parser;

        private CommitParser(final Parser<T> parser) {
            this.parser = parser;
        }

        @Override
        public Description description() {
            return parser.description();
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            return switch (parser.apply(input)) {
                case Success<T> success -> success;
                case Error<T> error -> new Abort<>(error.message, error.rest);
                case Abort<T> abort -> abort;
            };
        }

    }

    private final class NegationParser extends Parser<Void> {

        private final Parser<?> parser;

        private NegationParser(final Parser<?> parser) {
            this.parser = parser;
        }

        @Override
        public Description description() {
            return parser.description().negate();
        }

        @Override
        public Result<Void> apply(final Input<I> input) {
            return switch(parser.apply(input)) {
                case Success<?> success -> new Error<>(Failure.format(input, description()), success.rest);
                case Failure<?> ignore -> new Success<>(null, input);
            };
        }

    }

    private final class LogParser<T> extends Parser<T> {

        private final String name;

        private final Parser<T> parser;

        public LogParser(final String name, final Parser<T> parser) {
            this.name = name;
            this.parser = parser;
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            System.out.printf("trying '%s' in %s (%s)\n", name, input.position(), input.position().describe());
            final Result<T> result = parser.apply(input);
            return switch (result) {
                case Success<T> success -> {
                    System.out.printf("succeeded to parse '%s': %s\n", name, success.value);
                    yield success;
                }
                case Failure<T> failure -> {
                    System.out.printf("failed to parse '%s': %s\n", name, failure.message);
                    yield failure;
                }
            };
        }

    }

}
