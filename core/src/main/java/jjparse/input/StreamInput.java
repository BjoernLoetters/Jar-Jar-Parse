package jjparse.input;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class StreamInput<T> extends Input<T> {

    private final Stream<T> stream;

    private final int offset;

    private T head = null;
    private boolean isEmpty = false;
    private boolean initialized = false;

    StreamInput(final String name, final Stream<T> stream, final int offset) {
        super(name);
        this.stream = stream;
        this.offset = offset;
    }

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
