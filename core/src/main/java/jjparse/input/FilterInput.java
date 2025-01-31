package jjparse.input;

import java.util.function.Predicate;

public final class FilterInput<T> extends Input<T> {

    private Input<T> input;

    private final Predicate<T> predicate;

    private boolean skipped = false;

    FilterInput(final Input<T> input, final Predicate<T> predicate) {
        super(input.name);
        this.input = input;
        this.predicate = predicate;
    }

    private Input<T> skip() {
        if (!skipped) {
            skipped = true;

            while (input.nonEmpty() && !predicate.test(input.head())) {
                input = input.tail();
            }
        }

        return input;
    }

    @Override
    public boolean isEmpty() {
        return skip().isEmpty();
    }

    @Override
    public T head() {
        return skip().head();
    }

    @Override
    public Input<T> tail() {
        return skip().tail();
    }

    @Override
    public Input<T>.Position position() {
        return skip().position();
    }

}
