package jjparse.input;

import jjparse.Parsing;

import java.util.NoSuchElementException;

/**
 * A decorator that wraps an {@link Input} with a {@link Parsing.Parser} which filters out irrelevant input tokens.
 * Such a {@link FilteredInput} contains the same input tokens as the underlying {@link Input} except for those skipped
 * by the provided {@link Parsing.Parser}.
 * {@link Position}s returned by this {@link Input} correspond to the ones of the underlying {@link Input}.
 * Instances of this class can be obtained using the {@link Input#withFilter(Parsing.Parser)} method.
 *
 * @see Input
 * @see Input#withFilter(Parsing.Parser)
 * @see Parsing.Parser
 *
 * @author Björn Lötters
 */
public class FilteredInput<I> extends Input<I> {

    /** The raw {@link Input} that underlies this one. */
    public final Input<I> input;

    /** A {@link Parsing.Parser} that filters out irrelevant input tokens. */
    public final Parsing<I>.Parser<Void> filter;

    /** A flag that indicates whether this {@link FilteredInput} has already been initialized by a call to {@link #initialize()}. */
    private boolean initialized = false;

    /** A cache for the filtered {@link Input} that is initialized in {@link #initialize()}. */
    private Input<I> filtered = null;

    /**
     * Constructs a new {@link FilteredInput} on basis of the provided {@link Input} and filter {@link Parsing.Parser}.
     * @param input The raw {@link Input} that should be filtered.
     * @param filter A {@link Parsing.Parser} that filters out irrelevant input tokens.
     */
    FilteredInput(final Input<I> input, final Parsing<I>.Parser<Void> filter) {
        super(input.name);
        this.input = input;
        this.filter = filter;
    }

    /** Initializes the internal cache if this has not yet been done. */
    private void initialize() {
        if (!initialized) {
            initialized = true;

            final Parsing<I>.Result<?> result = filter.apply(input);
            if (result.isSuccess()) {
                this.filtered = result.rest;
            } else {
                this.filtered = input;
            }
        }
    }

    @Override
    public Input<I> raw() {
        initialize();
        return filtered.raw();
    }

    @Override
    public boolean isEmpty() {
        initialize();
        return this.filtered.isEmpty();
    }

    @Override
    public I head() throws NoSuchElementException {
        initialize();
        return this.filtered.head();
    }

    @Override
    public Input<I> tail() {
        initialize();
        return new FilteredInput<>(this.filtered.tail(), filter);
    }

    @Override
    public Input<I>.Position position() {
        initialize();
        return this.filtered.position();
    }

}
