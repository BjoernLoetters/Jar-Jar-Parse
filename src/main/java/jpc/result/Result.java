package jpc.result;

import java.util.Optional;
import java.util.function.Function;

public sealed abstract class Result<T> permits Failure, Success {

    public final int offset;

    public Result(final int offset) {
        this.offset = offset;
    }

    public abstract Optional<T> get();

    public abstract boolean isFailure();

    public abstract boolean isSuccess();

    public abstract <U> Result<U> map(final Function<T, U> function);

    public abstract <U> Result<U> flatMap(final Function<T, Result<U>> function);

}
