package jcombinators.result;

import java.util.function.Function;

public final class Error<T> extends Failure<T> {

    public Error(final String message, final int offset) {
        super(message, offset);
    }

    @Override
    public final <U> Result<U> map(Function<T, U> function) {
        @SuppressWarnings("unchecked")
        final Error<U> error = (Error<U>) this;
        return error;
    }

    @Override
    public final <U> Result<U> flatMap(Function<T, Result<U>> function) {
        @SuppressWarnings("unchecked")
        final Error<U> error = (Error<U>) this;
        return error;
    }

}
