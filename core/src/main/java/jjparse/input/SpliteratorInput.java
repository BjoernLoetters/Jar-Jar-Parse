package jjparse.input;

import java.util.NoSuchElementException;
import java.util.Spliterator;

/**
 * A lazily evaluated {@link Input} for {@link Spliterator}s of arbitrary elements. {@link Position}s returned by this
 * {@link Input} reflect the order in which the elements are retrieved from the {@link Spliterator} using {@link Spliterator#tryAdvance}.
 * @param <T> The element type of the {@link Spliterator}.
 *
 * @author Björn Lötters
 *
 * @see Input
 * @see Position
 * @see Spliterator
 * @see Spliterator#tryAdvance
 */
public final class SpliteratorInput<T> extends Input<T> {

    /** The {@link Spliterator} internally used as the source. */
    private final Spliterator<T> spliterator;

    /** The current offset in the {@link Spliterator} (i.e., the number of elements already retrieved). */
    private final int offset;

    /** A cache of the current element in this {@link SpliteratorInput}. */
    private T head = null;

    /** A cache for the information whether this {@link SpliteratorInput} is empty or not. */
    private boolean isEmpty = false;

    /** A flag that indicates whether the internal cache is already initialized or not. */
    private boolean initialized = false;

    /**
     * Constructs a new {@link SpliteratorInput}.
     * @param name A human-readable name for this {@link SpliteratorInput}.
     * @param spliterator The underlying {@link Spliterator} for this {@link SpliteratorInput}.
     * @param offset The current offset in the underlying {@link Spliterator} (i.e., the number of elements already retrieved).
     */
    SpliteratorInput(final String name, final Spliterator<T> spliterator, final int offset) {
        super(name);
        this.spliterator = spliterator;
        this.offset = offset;
    }

    /**
     * Initializes the internal cache if this has not yet been done.
     */
    private void initialize() {
        if (!initialized) {
            initialized = true;
            this.isEmpty = !spliterator.tryAdvance(head -> this.head = head);
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
    public SpliteratorInput<T> tail() {
        initialize();
        if (isEmpty) {
            return this;
        } else {
            return new SpliteratorInput<>(name, spliterator, offset + 1);
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
