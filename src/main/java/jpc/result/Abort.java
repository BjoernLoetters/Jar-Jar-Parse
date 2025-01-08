package jpc.result;

import java.util.function.Function;

public final class Abort<T> extends Failure<T> {

    public Abort(final String message, final int offset) {
        super(message, offset);
    }

    @Override
    public final <U> Result<U> map(Function<T, U> function) {
        @SuppressWarnings("unchecked")
        final Abort<U> error = (Abort<U>) this;
        return error;
    }

    @Override
    public final <U> Result<U> flatMap(Function<T, Result<U>> function) {
        @SuppressWarnings("unchecked")
        final Abort<U> error = (Abort<U>) this;
        return error;
    }

}
