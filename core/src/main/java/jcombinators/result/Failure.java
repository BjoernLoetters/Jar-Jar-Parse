package jcombinators.result;

import jcombinators.description.Description;
import jcombinators.input.Input;

import java.util.Optional;

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
    public T getOrThrow() throws RuntimeException {
        throw new RuntimeException(message);
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
        final String unexpected = input.isEmpty() ? "end of input" : String.format("character '%s'", String.valueOf(Character.toChars(input.getCodePoint())));
        final Optional<String> expected = description.normalize().describe();
        if (expected.isEmpty()) {
            return String.format("syntax error in %s at line %d and character %d: unexpected %s", input.name, input.position.line, input.position.column, unexpected);
        } else {
            return String.format("syntax error in %s at line %d and character %d: unexpected %s, expected %s", input.name, input.position.line, input.position.column, unexpected, expected.get());
        }
    }

}
