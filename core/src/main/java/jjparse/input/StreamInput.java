package jjparse.input;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * A lazily evaluated {@link Input} for {@link Stream}s of arbitrary elements. {@link Position}s returned by this
 * {@link Input} reflect the order in which the elements are retrieved from the {@link Stream} using {@link Stream#findFirst()}.
 * @param <T> The element type of the {@link Stream}.
 *
 * @author Björn Lötters
 *
 * @see Input
 * @see Position
 * @see Stream
 * @see Stream#findFirst
 */
public final class StreamInput<T> extends Input<T> {

    /** The {@link Stream} internally used as the source. */
    private final Stream<T> stream;

    /** The current offset in the {@link Stream} (i.e., the number of elements already retrieved). */
    private final int offset;

    /** A cache of the current element in this {@link StreamInput}. */
    private T head = null;

    /** A cache for the information whether this {@link StreamInput} is empty or not. */
    private boolean isEmpty = false;

    /** A flag that indicates whether the internal cache is already initialized or not. */
    private boolean initialized = false;

    /**
     * Constructs a new {@link StreamInput}.
     * @param name A human-readable name for this {@link StreamInput}.
     * @param stream The underlying {@link Stream} for this {@link StreamInput}.
     * @param offset The current offset in the underlying {@link Stream} (i.e., the number of elements already retrieved).
     */
    StreamInput(final String name, final Stream<T> stream, final int offset) {
        super(name);
        this.stream = stream;
        this.offset = offset;
    }

    /**
     * Initializes the internal cache if this has not yet been done.
     */
    private void initialize() {
        if (!initialized) {
            initialized = true;
            stream.findFirst().ifPresentOrElse(head -> this.head = head, () -> isEmpty = true);
        }
    }

    @Override
    public boolean isEmpty() {
        initialize();
        return isEmpty;
    }

    @Override
    public T head() throws NoSuchElementException {
        initialize();
        if (isEmpty) {
            throw new NoSuchElementException();
        } else {
            return head;
        }
    }

    @Override
    public StreamInput<T> tail() {
        initialize();
        if (isEmpty) {
            return this;
        } else {
            return new StreamInput<>(name, stream, offset + 1);
        }
    }

    @Override
    public Position position() {
        return new Position(offset) {

            @Override
            public String describe() {
                return isEmpty() ? "end of input" : head().toString();
            }

            @Override
            public String toString() {
                return String.format("%s at position %d", name, offset);
            }

        };
    }

}
