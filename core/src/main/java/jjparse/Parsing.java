package jjparse;

import jjparse.data.Product;
import jjparse.description.Choice;
import jjparse.description.Description;
import jjparse.description.Empty;
import jjparse.input.Input;

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
 * @see StringParsing
 *
 * @author Björn Lötters
 *
 * @param <I> The input type or, more specifically, the type of a single element in the underlying input stream.
 */
public abstract class Parsing<I> {

    /**
     * Creates an instance of this {@link Parsing} class and provides access to all {@link Parser}s and {@link Parser}
     * combinators in that way.
     */
    public Parsing() { }

    /**
     * Represents the result of a parsing operation.
     * <br/><br/>
     * A {@link Result} can either be a {@link Success} or a {@link Failure}, whereas a failure is further divided into:
     * <ul>
     *     <li>{@link Error}: A recoverable kind of {@link Failure} that may trigger backtracking during parsing.</li>
     *     <li>{@link Abort}: A fatal kind of {@link Failure} that does not trigger backtracking and aborts parsing.</li>
     * </ul>
     * <br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     *
     * @see Success
     * @see Failure
     * @see Input
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the parsed value in case of a {@link Success}.
     */
    public sealed abstract class Result<T> permits Success, Failure {

        /** The rest of the {@link Input}. */
        public final Input<I> rest;

        /**
         * The base constructor for {@link Result}.
         * @param rest The rest of the {@link Input}.
         */
        public Result(final Input<? extends I> rest) {
            @SuppressWarnings("unchecked") // The type is assumed to be immutable and hence covariant
            final Input<I> up = (Input<I>) rest;
            this.rest = up;
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
     * <br/><br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     *
     * @see Result
     * @see Failure
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the parsed value.
     */
    public final class Success<T> extends Result<T> {

        /** The parsed value of this {@link Success}. */
        public final T value;

        /**
         * Constructs a new {@link Success}.
         * @param value The parsed value.
         * @param rest The rest of the {@link Input}.
         */
        public Success(final T value, final Input<? extends I> rest) {
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
     * <br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     *
     * @see Result
     * @see Success
     * @see Error
     * @see Abort
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the parsed value, which is unused in case of a {@link Failure}.
     */
    public sealed abstract class Failure<T> extends Result<T> {

        /** The error message that describes this {@link Failure} in more detail. */
        public final String message;


        /**
         * The base constructor for a {@link Failure}.
         * @param message The error message that describes this {@link Failure} in more detail.
         * @param rest The rest of the {@link Input}.
         */
        public Failure(final String message, final Input<? extends I> rest) {
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
            @SuppressWarnings("unchecked") // A failure does not carry a value of its type parameter
            final Failure<U> failure = (Failure<U>) this;
            return failure;
        }

        @Override
        public <U> Result<U> flatMap(final Function<T, Result<U>> function) {
            @SuppressWarnings("unchecked") // A failure does not carry a value of its type parameter
            final Failure<U> failure = (Failure<U>) this;
            return failure;
        }

        /**
         * Format an error message for the provided {@link Input} and {@link Description}.
         * @param input The related {@link Input} of the error message.
         * @param description The {@link Description} of the {@link Parsing.Parser} that failed.
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
     * <br/><br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     *
     * @see Result
     * @see Failure
     * @see Abort
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the parsed value, which is unused in case of an {@link Error}.
     */
    public final class Error<T> extends Failure<T> {

        public Error(final String message, final Input<? extends I> rest) {
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
     * <br/><br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     *
     * @see Result
     * @see Failure
     * @see Error
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the parsed value, which is unused in case of an {@link Abort}.
     */
    public final class Abort<T> extends Failure<T> {

        public Abort(final String message, final Input<? extends I> rest) {
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
     * Very similar to recursive descent parsing, a {@link Parser} is just a {@link Function} that takes an {@link Input}
     * and produces a {@link Result}. In order to implement a {@link Parser} it therefore suffices to implement the
     * {@link Parser#apply} method.
     * <br/><br/>
     * <b>Note</b>: This class is intended to be immutable and hence covariant in its type parameter.
     * <br/><br/>
     * <b>Implementation Note</b>: Unfortunately, Java does not support instance interfaces which is why this class is not a
     * {@link FunctionalInterface} and we cannot use the lambda syntax to implement a {@link Parser}.
     *
     * @author Björn Lötters
     *
     * @param <T> The covariant type of the value that is the result of running this {@link Parser}.
     */
    public abstract class Parser<T> implements Function<Input<I>, Result<T>> {

        /**
         * Applies this {@link Parser} to the provided {@link Input}, returning a {@link Result}.
         * @param input The {@link Input} which this {@link Parser} shall parse.
         * @return The {@link Result} of the parsing operation.
         *
         * @see Result
         */
        @Override
        public abstract Result<T> apply(final Input<I> input);

        /**
         * This method shall return a {@link Description} for this {@link Parser} which provides details about the shape
         * of the {@link Input} this {@link Parser} expects. By default, this method returns an {@link Empty} {@link Description}
         * and must hence be overwritten if required.
         *
         * @return A {@link Description} of this {@link Parser}.
         *
         * @see Description
         */
        public Description description() {
            return new Empty();
        }

        /**
         * Maps this {@link Parser} by applying the provided {@link Function} to the {@link Result}s it produces.
         * @param function The {@link Function} which shall be applied to the {@link Result}s of this parser.
         * @return A {@link Parser} that parses the exact same language as this one, but with a transformed {@link Result}.
         * @param <U> The type of the transformed {@link Parser}.
         * @see #flatMap
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
         * @see #map
         * @see #and
         */
        public final <U> Parser<U> flatMap(final Function<T, Parser<U>> function) {
            return new FlatMapParser<T, U>(this, function);
        }

        /**
         * Concatenates this and the provided {@link Parser} and returns a {@link Product} of the corresponding values
         * on {@link Success}.
         * @param right The second {@link Parser} which shall be appended to this {@link Parser}.
         * @return A {@link Parser} which first parses this and then the provided one, returning the {@link Product} of
         *         their returned values on {@link Success}.
         * @param <U> The type of the second {@link Parser}'s {@link Result}.
         * @see #keepLeft
         * @see #keepRight
         * @see #flatMap
         * @see #or
         * @see #sequence
         */
        public final <U> Parser<Product<T, U>> and(final Parser<U> right) {
            return flatMap(first -> right.map(second -> new Product<>(first, second)));
        }

        /**
         * Concatenates this and the provided {@link Parser} but returns only the value parsed by this {@link Parser}.
         * @param right The second {@link Parser} which shall be appended to this {@link Parser}.
         * @return A {@link Parser} which first parses this and then the provided one, returning the value of this
         *         {@link Parser} only.
         * @param <U> The type of the second {@link Parser}'s {@link Result}.
         * @see #keepRight
         * @see #and
         */
        public final <U> Parser<T> keepLeft(final Parser<U> right) {
            return flatMap(result -> right.map(ignore -> result));
        }

        /**
         * Concatenates this and the provided {@link Parser} but returns only the value parsed by the provided one.
         * @param right The second {@link Parser} which shall be appended to this {@link Parser}.
         * @return A {@link Parser} which first parses this and then the provided one, returning the value of the
         *         provided {@link Parser} only.
         * @param <U> The type of the second {@link Parser}'s {@link Result}.
         * @see #keepLeft
         * @see #and
         */
        public final <U> Parser<U> keepRight(final Parser<U> right) {
            return flatMap(ignore -> right);
        }

        /**
         * Creates a {@link Parser} which first attempts to parse this {@link Parser} and, if this {@link Parser} fails
         * with an {@link Error}, attempts to parse the exact same {@link Input} with the provided {@link Parser}.
         * @param parser The alternative {@link Parser}.
         * @return A {@link Parser} which first attempts to parse the {@link Input} with this and only then with the
         *         provided {@link Parser}.
         *
         * @see #choice
         * @see #and
         */
        public final Parser<T> or(final Parser<? extends T> parser) {
            return choice(this, parser);
        }

        /**
         * Creates a {@link Parser} which attempts to parse this {@link Parser} and only fails if this {@link Parser}
         * aborts with an {@link Abort}.
         * @return A {@link Parser} which optionally parses this {@link Parser}.
         *
         * @see Optional
         * @see #repeat
         */
        public final Parser<Optional<T>> optional() {
            return map(Optional::of).or(success(Optional::empty));
        }

        /**
         * Creates a {@link Parser} which applies this {@link Parser} arbitrarily many, including zero, times.
         * @return A {@link Parser} which repeats this {@link Parser} arbitrarily many, including zero, times.
         * @see #optional
         * @see #repeat1
         * @see #separate
         * @see #separate1
         */
        public final Parser<List<T>> repeat() {
            return new RepeatParser<>(this);
        }

        /**
         * Creates a {@link Parser} which applies this {@link Parser} arbitrarily many times but at least one time.
         * @return A {@link Parser} which repeats this {@link Parser} arbitrarily many times but at least one time.
         * @see #optional
         * @see #repeat
         * @see #separate
         * @see #separate1
         */
        public final Parser<List<T>> repeat1() {
            return this.and(repeat()).map(product -> product.map(Stream::of, List::stream).fold(Stream::concat).toList());
        }

        /**
         * Creates a {@link Parser} which parses this {@link Parser} (at least one time) interleaved with the provided
         * separator {@link Parser}, only returning the values returned by this {@link Parser}. This is especially
         * useful for {@link Input}s like comma separated values.
         * @param separator The separator {@link Parser}.
         * @return A {@link Parser} which parses this {@link Parser} interleaved with the provided separator
         *         {@link Parser}.
         * @see #repeat
         * @see #repeat1
         * @see #separate
         * @see #chainLeft
         * @see #chainLeft1
         * @see #chainRight
         * @see #chainRight1
         */
        public final Parser<List<T>> separate1(final Parser<?> separator) {
            return this.and(separator.and(this).repeat()).map(product -> product.map(Stream::of, list -> list.stream().map(Product::second)).fold(Stream::concat).toList());
        }

        /**
         * Creates a {@link Parser} which parses this {@link Parser} (possibly zero times) interleaved with the provided
         * separator {@link Parser}, only returning the values returned by this {@link Parser}. This is especially
         * useful for {@link Input}s like comma separated values.
         * @param separator The separator {@link Parser}.
         * @return A {@link Parser} which parses this {@link Parser} interleaved with the provided separator
         *         {@link Parser}.
         * @see #repeat
         * @see #repeat1
         * @see #separate1
         * @see #chainLeft
         * @see #chainLeft1
         * @see #chainRight
         * @see #chainRight1
         */
        public final Parser<List<T>> separate(final Parser<?> separator) {
            return separate1(separator).optional().map(result -> result.orElseGet(List::of));
        }

        /**
         * Creates a {@link Parser} which parses this {@link Parser} in between the two provided {@link Parser}s, only
         * returning the value of this {@link Parser}.
         * @param left The left of the two enclosing {@link Parser}s.
         * @param right The right of the two enclosing {@link Parser}s.
         * @return A {@link Parser} which parses this {@link Parser} in between the two provided {@link Parser}s.
         * @see #and
         */
        public final Parser<T> between(final Parser<?> left, final Parser<?> right) {
            return left.keepRight(this).keepLeft(right);
        }

        /**
         * Creates a {@link Parser} which behaves like this {@link Parser} except that the created {@link Parser} aborts
         * with an {@link Abort} when this {@link Parser} fails with an {@link Error}. The effect of this is that
         * backtracking does not extend over the created {@link Parser}. This is especially useful for the {@link Parser}
         * right after a keyword {@link Parser}, to prevent backtracking over the keyword.
         * @return A {@link Parser} which behaves like this {@link Parser}, but with backtracking disabled.
         * @see #not
         */
        public final Parser<T> commit() {
            return new CommitParser<>(this);
        }

        /**
         * Creates a {@link Parser} which attempts to not parse this {@link Parser}. That is to say, every
         * {@link Failure} of this {@link Parser} is transformed into a {@link Success} and vice versa. The created
         * {@link Parser} never consumes any {@link Input}.
         * @return A {@link Parser} which ensures that this {@link Parser} is not applicable.
         * @see #commit
         */
        public final Parser<Void> not() {
            return new NegationParser(this);
        }

        /**
         * Creates a {@link Parser} which prints additional debug output to the standard output stream.
         * @param name A human-readable name for this {@link Parser}.
         * @return A {@link Parser} which behaves like this {@link Parser} but prints additional debug output.
         */
        public final Parser<T> log(final String name) {
            return new LogParser<>(name, this);
        }

    }

    /** The {@link Parser} used for skipping {@link Input} before applying another {@link Parser}. */
    private Parser<Void> skip = success(() -> null);

    /** A boolean flag that controls whether we are currently in skip mode and hence should not apply the skip {@link Parser} again. */
    private boolean skipping = false;

    /* *** Public Parser API *** */

    /**
     * Attempts to parse the whole {@link Input} with the provided {@link Parser}. This method fails, if {@link Input}
     * that cannot be skipped remains after applying the provided {@link Parser}.
     * @param parser The {@link Parser} that should be applied to the {@link Input}.
     * @param input The {@link Input} that should be parsed using the provided {@link Parser}.
     * @return The {@link Result} after applying the provided {@link Parser} to the {@link Input}.
     * @param <T> The type of the {@link Result}.
     * @see #skip(Input)
     * @see Parser
     * @see Input
     * @see Result
     */
    public final <T> Result<T> parse(final Parser<? extends T> parser, final Input<? extends I> input) {
        @SuppressWarnings("unchecked") // The type is assumed to be immutable and hence covariant
        final Result<T> result = (Result<T>) parser.apply(skip(input));

        if (result.isSuccess()) {
            final Input<I> rest = skip(result.rest);
            if (rest.isEmpty()) {
                return new Success<>(result.getOrFail(), rest);
            } else {
                return new Error<>(Failure.format(rest, new Empty()), rest);
            }
        } else {
            return result;
        }
    }

    /**
     * Skips the prefix of the provided {@link Input} using the current skip {@link Parser}. This method does not fail,
     * even if the skip {@link Parser} was not successful.
     * @param input The {@link Input} whose prefix should be skipped.
     * @return The {@link Input} that remains after skipping.
     * @see #setSkipParser(Parser)
     */
    public final Input<I> skip(final Input<? extends I> input) {
        @SuppressWarnings("unchecked") // The type is assumed to be immutable and hence covariant
        final Input<I> up = (Input<I>) input;
        if (skipping) {
            return up;
        } else {
            skipping = true;
            try {
                return skip.apply(up).rest;
            } finally {
                skipping = false;
            }
        }
    }

    /**
     * Sets the {@link Parser} that is used for skipping {@link Input} before another {@link Parser} is applied. By
     * default, no skip {@link Parser} is set and no {@link Input} is skipped.
     * @param parser The new {@link Parser} that should be used for skipping. This {@link Parser} may fail, in which
     *               case no {@link Input} is skipped.
     * @param <T> The result type of the provided {@link Parser}, which is voided internally.
     * @throws NullPointerException If the provided {@link Parser} is {@code null}.
     * @see #skip(Input)
     */
    public final <T> void setSkipParser(final Parser<? extends T> parser) throws NullPointerException {
        if (parser == null) {
            throw new NullPointerException();
        } else {
            skip = parser.map(ignore -> null);
        }
    }

    /**
     * Lifts the provided {@link Function} into a {@link Parser}. This allows us to use Java's lambda notation, despite
     * that {@link Parser} must be an abstract class.
     * @param function The {@link Function} that parses an {@link Input} and returns a {@link Result}.
     * @return A {@link Parser} which behaves just like the provided {@link Function}.
     * @param <T> The type of the {@link Result}.
     * @see Parser
     * @see Result
     * @see Input
     * @see Function
     */
    public final <T> Parser<T> lift(final Function<? super Input<I>, ? extends Result<T>> function) {
        return new Parser<T>() {

            @Override
            public Result<T> apply(final Input<I> input) {
                return function.apply(skip(input));
            }

        };
    }

    /**
     * Constructs a {@link Parser} that always fails with an {@link Error}.
     * @param message The message of the {@link Error}.
     * @return A {@link Parser} that always fails with an {@link Error}.
     * @param <T> The mandatory type of the {@link Result}.
     */
    public final <T> Parser<T> error(final String message) {
        return lift(input -> new Error<>(message, input));
    }

    /**
     * Constructs a {@link Parser} that always fails with an {@link Abort}.
     * @param message The message of the {@link Abort}.
     * @return A {@link Parser} that always fails with an {@link Abort}.
     * @param <T> The mandatory type of the {@link Result}.
     */
    public final <T> Parser<T> abort(final String message) {
        return lift(input -> new Abort<>(message, input));
    }

    /**
     * Constructs a {@link Parser} that always succeeds with a {@link Success}.
     * @param supplier A {@link Supplier} for the value that should be returned by the {@link Parser}.
     * @return A {@link Parser} that always succeeds with a {@link Success}.
     * @param <T> The type of the value that should be returned by {@link Parser}.
     */
    public final <T> Parser<T> success(final Supplier<? extends T> supplier) {
        return lift(input -> new Success<>(supplier.get(), input));
    }

    /**
     * Constructs a {@link Parser} that attempts to parse the provided {@link Parser}s in the order they are given until
     * one of them succeeds or aborts. The returned {@link Parser} fails if all {@link Parser}s fail.
     * @param parsers The {@link Parser}s that should be used for the new {@link Parser}.
     * @return A {@link Parser} which attempts to parse the provided {@link Parser}s in the order they are given.
     * @param <T> The type of the {@link Result}.
     */
    @SafeVarargs
    public final <T> Parser<T> choice(final Parser<? extends T>... parsers) {
        Parser<T> choice = error("empty choice");

        for (final Parser<? extends T> parser : parsers) {
            @SuppressWarnings("unchecked") // The type is assumed to be immutable and hence covariant
            final Parser<T> up = (Parser<T>) parser;
            choice = new ChoiceParser<>(choice, up);
        }

        return choice;
    }

    /**
     * Constructs a new {@link Parser} that applies the provided {@link Parser}s in sequence. It fails when one of the
     * {@link Parser}s fails and succeeds only when all {@link Parser}s succeed. In this case, the {@link Result}s of
     * all {@link Parser}s are collected in a single, immutable {@link List}.
     * @param parsers The {@link Parser}s that should be used for the new {@link Parser}.
     * @return A {@link Parser} which attempts to parse the provided {@link Parser}s in sequence.
     * @param <T> The type of a single {@link Result}.
     */
    @SafeVarargs
    public final <T> Parser<List<T>> sequence(final Parser<? extends T>... parsers) {
        Parser<List<T>> sequence = success(() -> new ArrayList<>());

        for (final Parser<? extends T> parser : parsers) {
            sequence = sequence.flatMap(result -> parser.map(element -> {
                result.add(element);
                return result;
            }));
        }

        return sequence.map(Collections::unmodifiableList);
    }

    /**
     * Just like {@link Parser#separate1}, this method separates the element {@link Parser} using the separator
     * {@link Parser}. However, instead of returning a {@link List} of the parsed elements, the elements are folded from
     * left to right into a single element using the {@link BiFunction}s returned by the separator {@link Parser}. This
     * is especially useful for parsing left-associative chains of operators such as {@code 1 + 2 + 3}.
     * @param element The element {@link Parser}.
     * @param separator The separator {@link Parser} which must return a {@link BiFunction} that combines any two elements.
     * @return A {@link Parser} which behaves similar to {@link Parser#separate1} but combines all elements into
     *         a single one.
     * @param <T> The type of the elements.
     * @see Parser#separate1
     * @see #chainLeft
     * @see #chainRight 
     * @see #chainRight1
     * @see BiFunction
     */
    public final <T> Parser<T> chainLeft1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(product -> {
            T result = product.first();

            for (final Product<BiFunction<T, T, T>, T> next : product.second()) {
                result = next.first().apply(result, next.second());
            }

            return result;
        });
    }

    /**
     * Just like {@link Parser#separate}, this method separates the element {@link Parser} using the separator
     * {@link Parser}. However, instead of returning a {@link List} of the parsed elements, the elements are folded from
     * left to right into a single element using the {@link BiFunction}s returned by the separator {@link Parser}. This
     * is especially useful for parsing left-associative chains of operators such as {@code 1 + 2 + 3}.
     * @param element The element {@link Parser}.
     * @param separator The separator {@link Parser} which must return a {@link BiFunction} that combines any two elements.
     * @param otherwise The element that should be returned when there is no single element in the chain.
     * @return A {@link Parser} which behaves similar to {@link Parser#separate} but combines all elements into
     *         a single one.
     * @param <T> The type of the elements.
     * @see Parser#separate
     * @see #chainLeft1
     * @see #chainRight
     * @see #chainRight1
     * @see BiFunction
     */
    public final <T> Parser<T> chainLeft(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainLeft1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    /**
     * Just like {@link Parser#separate1}, this method separates the element {@link Parser} using the separator
     * {@link Parser}. However, instead of returning a {@link List} of the parsed elements, the elements are folded from
     * right to left into a single element using the {@link BiFunction}s returned by the separator {@link Parser}. This
     * is especially useful for parsing right-associative chains of operators such as {@code a = b = c}.
     * @param element The element {@link Parser}.
     * @param separator The separator {@link Parser} which must return a {@link BiFunction} that combines any two elements.
     * @return A {@link Parser} which behaves similar to {@link Parser#separate1} but combines all elements into
     *         a single one.
     * @param <T> The type of the elements.
     * @see Parser#separate1
     * @see #chainLeft
     * @see #chainLeft1
     * @see #chainRight
     * @see BiFunction
     */
    public final <T> Parser<T> chainRight1(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator) {
        return element.and(separator.and(element).repeat()).map(product -> {
            final Iterator<? extends Product<BiFunction<T, T, T>, T>> iterator = product.second().reversed().iterator();

            if (iterator.hasNext()) {
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
            } else {
                return product.first();
            }
        });
    }

    /**
     * Just like {@link Parser#separate}, this method separates the element {@link Parser} using the separator
     * {@link Parser}. However, instead of returning a {@link List} of the parsed elements, the elements are folded from
     * right to left into a single element using the {@link BiFunction}s returned by the separator {@link Parser}. This
     * is especially useful for parsing right-associative chains of operators such as {@code a = b = c}.
     * @param element The element {@link Parser}.
     * @param separator The separator {@link Parser} which must return a {@link BiFunction} that combines any two elements.
     * @param otherwise The element that should be returned when there is no single element in the chain.
     * @return A {@link Parser} which behaves similar to {@link Parser#separate} but combines all elements into
     *         a single one.
     * @param <T> The type of the elements.
     * @see Parser#separate
     * @see #chainLeft
     * @see #chainLeft1
     * @see #chainRight1
     * @see BiFunction
     */
    public final <T> Parser<T> chainRight(final Parser<T> element, final Parser<BiFunction<T, T, T>> separator, final T otherwise) {
        return chainRight1(element, separator).optional().map(result -> result.orElse(otherwise));
    }

    /**
     * Constructs a new {@link Parser} that behaves just like the given one, applying the current {@link Input.Position}
     * to the {@link Function} returned by the provided {@link Parser}.
     * @param parser A {@link Parser} returning a {@link Function} which accepts the current {@link Input.Position}.
     * @return A new {@link Parser} that provides the given one with a {@link Input.Position}.
     * @param <T> The type of the {@link Result}.
     */
    public final <T> Parser<T> position(final Parser<Function<Input<I>.Position, T>> parser) {
        return lift(input -> parser.apply(input).map(function -> function.apply(input.position())));
    }

    /**
     * Constructs a new {@link Parser} which is initialized lazy. That is to say, when this {@link Parser} is applied
     * for the first time, the provided {@link Supplier} is called and the returned {@link Parser} is used for parsing.
     * Moreover, this {@link Parser} is memorized for future applications. This method is especially useful since Java
     * does not provide us with lazy declarations, which are required for mutually recursive parsers (as they occur very
     * often in practice).
     * @param supplier The {@link Supplier} that provides the actual {@link Parser}.
     * @return A {@link Parser} that lazily evaluates the provided {@link Supplier} and behaves just like the {@link Parser}
     *         provided by the {@link Supplier}.
     * @param <T> The type of the {@link Result}.
     */
    public final <T> Parser<T> lazy(final Supplier<Parser<? extends T>> supplier) {
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
            return parser.apply(skip(input)).map(function);
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
            return switch (parser.apply(skip(input)).map(function)) {
                case Success<Parser<U>> success ->
                    switch (success.value.apply(skip(success.rest))) {
                        case Success<U> result -> result;
                        case Error<U> error -> new Error<>(error.message, input);
                        case Abort<U> abort -> new Abort<>(abort.message, input);
                    };

                case Failure<Parser<U>> failure -> {
                    @SuppressWarnings("unchecked")  // A failure does not carry a value of its type parameter
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
            final Input<I> skipped = skip(input);
            return switch (first.apply(skipped)) {
                case Success<T> success -> success;
                case Abort<T> abort -> abort;
                case Error<T> ignore -> switch (second.apply(skipped)) {
                    case Success<T> success -> success;
                    case Abort<T> abort -> abort;
                    case Error<T> secondError -> new Error<>(Failure.format(secondError.rest, description()), skipped);
                };
            };
        }

    }

    private final class LazyParser<T> extends Parser<T> {

        private final Supplier<Parser<? extends T>> supplier;

        private Parser<T> parser = null;

        public LazyParser(final Supplier<Parser<? extends T>> supplier) {
            this.supplier = supplier;
        }

        private Parser<T> get() {
            if (parser == null) {
                @SuppressWarnings("unchecked") // The type is assumed to be immutable and hence covariant
                final Parser<T> up = (Parser<T>) supplier.get();
                parser = up;
            }

            return parser;
        }

        @Override
        public Description description() {
            return get().description();
        }

        @Override
        public Result<T> apply(final Input<I> input) {
            return get().apply(skip(input));
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
            Result<T> result = parser.apply(skip(input));

            while (result.isSuccess()) {
                elements.add(result.getOrFail());
                result = parser.apply(skip(result.rest));
            }

            return new Success<>(Collections.unmodifiableList(elements), result.rest);
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
            final Input<I> skipped = skip(input);
            return switch(parser.apply(skipped)) {
                case Success<?> ignore -> new Error<>(Failure.format(skipped, description()), skipped);
                case Failure<?> ignore -> new Success<>(null, skipped);
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
            final Input<I> skipped = skip(input);
            System.out.printf("trying '%s' in %s (%s)\n", name, skipped.position(), skipped.position().describe());
            final Result<T> result = parser.apply(skipped);
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
