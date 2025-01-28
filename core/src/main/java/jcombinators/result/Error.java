package jcombinators.result;

import jcombinators.description.Description;
import jcombinators.input.Input;

import java.util.Set;
import java.util.function.Function;

public final class Error<T> extends Failure<T> {

    public Error(final String message, final Input rest) {
        super(message, rest);
    }

    @Override
    public <U> Result<U> map(Function<T, U> function) {
        @SuppressWarnings("unchecked")
        final Error<U> error = (Error<U>) this;
        return error;
    }

    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> function) {
        @SuppressWarnings("unchecked")
        final Error<U> error = (Error<U>) this;
        return error;
    }

}
