package jcombinators.result;

import jcombinators.input.Input;

import java.util.Optional;
import java.util.function.Function;

public final class Success<T> extends Result<T> {

    public final T value;

    public Success(final T value, final Input rest) {
        super(rest);
        this.value = value;
    }

    @Override
    public Optional<T> get() {
        return Optional.of(value);
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public <U> Result<U> map(final Function<T, U> function) {
        return new Success<>(function.apply(value), rest);
    }

    @Override
    public <U> Result<U> flatMap(final Function<T, Result<U>> function) {
        return function.apply(value);
    }

}
