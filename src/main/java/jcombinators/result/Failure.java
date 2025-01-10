package jcombinators.result;

import jcombinators.description.Description;
import jcombinators.input.Input;

import java.util.Optional;
import java.util.Set;

public sealed abstract class Failure<T> extends Result<T> permits Abort, Error {

    public final String message;

    public Failure(final String message, final Input rest) {
        super(rest);
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

    public static String format(final Input input, final Description description) {
        final String unexpected = input.isEmpty() ? "end of input" : String.format("character '%s'", String.valueOf(Character.toChars(input.codePointAt(0))));
        final Optional<String> expected = description.normalize().describe();
        if (expected.isEmpty()) {
            return String.format("unexpected %s", unexpected);
        } else {
            return String.format("unexpected %s, expected %s", unexpected, expected.get());
        }
    }

}
