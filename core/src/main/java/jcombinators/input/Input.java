package jcombinators.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jcombinators.Parsing.Parser;

/**
 * An abstract representation of an {@link Input} for parsing. In most cases, this will be a {@link CharInputOld}, which is
 * an {@link Input} of {@link Character} and can be considered a sequence of characters (just like a {@link String}).
 * @param <T> The element type for this {@link Input}. In most cases this type equals {@link Character}.
 *
 * @see Parser
 *
 * @author Björn Lötters
 */
public abstract class Input<T> {

    /** A human-readable name for this {@link Input} which can be used for error reporting purposes. */
    public final String name;

    /**
     * Constructs a new {@link Input} on basis of the provided name.
     * @param name A human-readable name for this {@link Input}, which is primarily used for error reporting purposes.
     */
    public Input(final String name) {
        this.name = name;
    }

    /**
     * Checks whether this {@link Input} is empty.
     * @return {@code true} if this {@link Input} is empty.
     * @see #nonEmpty
     */
    public abstract boolean isEmpty();

    /**
     * Checks whether this {@link Input} is not empty.
     * @return {@code true} if this {@link Input} is not empty.
     * @see #isEmpty
     */
    public final boolean nonEmpty() {
        return !isEmpty();
    }

    /**
     * Returns the first element of this {@link Input}. If this {@link Input} is empty, an exception is thrown.
     * @return The first element of this {@link Input}.
     * @throws NoSuchElementException If this {@link Input} is empty.
     * @see #isEmpty()
     * @see #tail()
     */
    public abstract T head() throws NoSuchElementException;

    /**
     * Returns the rest of this {@link Input}, excluding its first element. If this {@link Input} is empty, an
     * empty {@link Input} is returned.
     * @return The rest of this {@link Input} or an empty {@link Input}, if this {@link Input} is empty.
     * @see #isEmpty()
     * @see #head()
     */
    public abstract Input<T> tail();

    /**
     * A {@link Position} that points to the first element in this {@link Input}.
     * @return The {@link Position} of the first element in this {@link Input}.
     */
    public abstract Position position();

    /**
     * Filters this {@link Input} by skipping all elements for which the given {@link Predicate} does not hold. The
     * {@link Position}s returned by the filtered {@link Input} are not affected by this operation. That is, all
     * elements retain their original {@link Position}s from this {@link Input}.
     * @param predicate The {@link Predicate} that shall be used as filter. Whenever this {@link Predicate} returns
     *                  {@code false} for an element, it is skipped and not included in the final {@link Input}.
     * @return An {@link Input} containing all elements of this {@link Input} except for those elements for which the
     *         provided {@link Predicate} does not hold.
     * @see #position()
     */
    public final Input<T> filter(final Predicate<T> predicate) {
        return new FilterInput<>(this, predicate);
    }

    /**
     * Represents an offset within this {@link Input}. Since the unit of this offset depends on the element type of
     * this {@link Input}, this class depends on the specific {@link Input} instance. Also note, that this offset may
     * not reflect the {@link Position} as it is perceived by the user. This is, for example, the case for {@link Input}s
     * of {@link Character}s, where up to two {@link Character}s may form a single unicode code point. It is up to the
     * implementation of the respective {@link Input} to interpret this offset accordingly.
     *
     * @see Input
     *
     * @author Björn Lötters
     */
    public abstract class Position {

        /** The {@link Input} to which this {@link Position} refers. */
        public final Input<T> input;

        /** The offset (i.e., the number of skipped elements) to which this {@link Position} points in this {@link Input}. */
        public final int offset;

        /**
         * Creates a new {@link Position} on basis of the provided offset.
         * @param offset The offset of this {@link Position}. An offset is the number of elements that must be skipped
         *               in the underlying {@link Input} in order to reach this {@link Position}.
         */
        public Position(final int offset) {
            this.input = Input.this;
            this.offset = offset;
        }

        /**
         * Describes the element to which this {@link Position} refers to.
         * @return A {@link String} representation of the element to which this {@link Position} refers to.
         */
        public abstract String describe();

        @Override
        public abstract String toString();

        @Override
        public boolean equals(final Object object) {
            if (object instanceof Input<?>.Position position) {
                return Objects.equals(input, position.input) && offset == position.offset;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(input, offset);
        }

    }

    /* *** Static Methods for Input Construction *** */

    /**
     * Constructs an empty {@link Input} of an arbitrary element type where {@code "<empty>"} is used as the name.
     * @return An empty {@link Input} of an arbitrary element type.
     * @param <T> The element type.
     */
    public static <T> Input<T> empty() {
        return of("<empty>", Stream.empty());
    }

    /**
     * Constructs a new {@link Input} on basis of the provided {@link Iterator}.
     * @param name A human-readable name for the {@link Input}.
     * @param iterator The underlying {@link Iterator} that provides the elements for the {@link Input}.
     * @return An {@link Input} over the provided {@link Iterator}.
     * @param <T> The element type of the {@link Input} and the {@link Iterator}.
     */
    public static <T> Input<T> of(final String name, final Iterator<T> iterator) {
        return of(name, () -> iterator);
    }

    /**
     * Constructs a new {@link Input} on basis of the provided {@link Iterable}.
     * @param name A human-readable name for the {@link Input}.
     * @param iterable The underlying {@link Iterable} that provides the elements for the {@link Input}.
     * @return An {@link Input} over the provided {@link Iterable}.
     * @param <T> The element type of the {@link Input} and the {@link Iterable}.
     */
    public static <T> Input<T> of(final String name, final Iterable<T> iterable) {
        return of(name, StreamSupport.stream(iterable.spliterator(), false));
    }

    /**
     * Constructs a new {@link Input} on basis of the provided {@link Stream}.
     * @param name A human-readable name for the {@link Input}.
     * @param stream The underlying {@link Stream} that provides the elements for the {@link Input}.
     * @return An {@link Input} over the provided {@link Stream}.
     * @param <T> The element type of the {@link Input} and the {@link Stream}.
     */
    public static <T> Input<T> of(final String name, final Stream<T> stream) {
        return new StreamInput<>(name, stream);
    }

    /**
     * Constructs a new {@link Input} of {@link Character}s on basis of the provided {@link InputStream} and {@link Charset}.
     * Take note that the provided {@link InputStream} is completely read before the {@link Input} is created. For a lazy
     * kind of input, consider one of the other variants of this method.
     * @param name A human-readable name for the {@link Input}.
     * @param stream The underlying source for this {@link Input}.
     * @param charset The {@link Charset} that shall be used to decode the {@link InputStream}.
     * @return An {@link Input} of {@link Character}s that is based on the underlying {@link InputStream}.
     */
    public static Input<Character> of(final String name, final InputStream stream, final Charset charset) throws IOException {
        final byte[] bytes = stream.readAllBytes();
        return of(name, bytes, charset);
    }

    /**
     * Constructs a new {@link Input} of {@link Character}s on basis of the provided file {@link Path} and {@link Charset}.
     * @param path The {@link Path} to the file that should be read as the source of this input. At the same time, this path is
     *             used as the name for the {@link Input}.
     * @param charset The {@link Charset} that shall be used to decode the file denoted by the provided {@link Path}.
     * @return An {@link Input} of {@link Character}s that is based on the underlying file contents.
     */
    public static Input<Character> of(final Path path, final Charset charset) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        return of(String.format("file '%s'", path), bytes, charset);
    }

    /**
     * Constructs a new {@link Input} of {@link Character}s on basis of the provided {@link Byte} array and {@link Charset}.
     * @param name A human-readable name for the {@link Input}.
     * @param bytes The underlying source for this {@link Input}.
     * @param charset The {@link Charset} that shall be used to decode the {@link Byte} array.
     * @return An {@link Input} of {@link Character}s that is based on the underlying {@link Byte} array.
     */
    public static Input<Character> of(final String name, final byte[] bytes, final Charset charset) {
        final String contents = new String(bytes, charset);
        return of(name, contents);
    }

    /**
     * Constructs a new {@link Input} of {@link Character}s on basis of the provided {@link CharSequence}.
     * @param name A human-readable name for the {@link Input}.
     * @param sequence The underlying source for this {@link Input}.
     * @return An {@link Input} of {@link Character}s that is based on the underlying {@link CharSequence}.
     */
    public static Input<Character> of(final String name, final CharSequence sequence) {
        final int[] lines = IntStream.range(1, sequence.length() + 1)
            .filter(index -> sequence.charAt(index - 1) == '\n')
            .toArray();

        return new CharacterInput(name, sequence, 0, sequence.length(), lines);
    }

}
