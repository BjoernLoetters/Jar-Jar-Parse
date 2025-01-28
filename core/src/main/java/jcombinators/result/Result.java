package jcombinators.result;

import jcombinators.input.Input;

import java.util.Optional;
import java.util.function.Function;

public sealed abstract class Result<T> permits Failure, Success {

    public final Input rest;

    public Result(final Input rest) {
        this.rest = rest;
    }

    public abstract Optional<T> get();

    public abstract T getOrThrow() throws RuntimeException;

    public abstract boolean isFailure();

    public abstract boolean isSuccess();

    public abstract <U> Result<U> map(final Function<T, U> function);

    public abstract <U> Result<U> flatMap(final Function<T, Result<U>> function);

}
