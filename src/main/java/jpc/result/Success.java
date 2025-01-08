package jpc.result;

import java.util.Optional;
import java.util.function.Function;

public final class Success<T> extends Result<T> {

    public final T value;

    public Success(final T value, final int offset) {
        super(offset);
        this.value = value;
    }

    @Override
    public final Optional<T> get() {
        return Optional.of(value);
    }

    @Override
    public final boolean isFailure() {
        return false;
    }

    @Override
    public final boolean isSuccess() {
        return true;
    }

    @Override
    public final <U> Result<U> map(final Function<T, U> function) {
        return new Success<>(function.apply(value), offset);
    }

    @Override
    public final <U> Result<U> flatMap(final Function<T, Result<U>> function) {
        return function.apply(value);
    }

}
