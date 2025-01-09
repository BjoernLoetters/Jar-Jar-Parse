package jcombinators.result;

import java.util.Optional;

public sealed abstract class Failure<T> extends Result<T> permits Abort, Error {

    public final String message;

    public Failure(final String message, final int offset) {
        super(offset);
        this.message = message;
    }

    @Override
    public final Optional<T> get() {
        return Optional.empty();
    }

    @Override
    public final boolean isFailure() {
        return true;
    }

    @Override
    public final boolean isSuccess() {
        return false;
    }

}
