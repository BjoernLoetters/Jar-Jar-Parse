package jcombinators.input;

import java.util.stream.Stream;

public final class StreamInput<T> extends Input<T> {

    private final Stream<T> stream;

    StreamInput(final String name, final Stream<T> stream) {
        super(name);
        this.stream = stream;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public T head() {
        return null;
    }

    @Override
    public Input<T> tail() {
        return null;
    }

    @Override
    public Input<T>.Position position() {
        return null;
    }

}
