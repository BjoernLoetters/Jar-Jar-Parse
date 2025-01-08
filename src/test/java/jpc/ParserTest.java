package jpc;

import jpc.result.Failure;
import jpc.result.Success;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class ParserTest {

    public final <T> void assertSuccess(final Parser<T> parser, final T expected, String input) {
        switch (parser.apply(input, 0)) {
            case Success<T> success:
                assertEquals(success.value, expected);
                break;

            case Failure<T> failure:
                fail("expected parse success, but got failure with message '" + failure.message + "'");
                break;
        }
    }

    public final void assertFailure(final Parser<?> parser, final String message, String input) {
        switch (parser.apply(input, 0)) {
            case Success<?> success:
                fail("expected parse failure, but got success with value '" + success.value + "'");
                break;

            case Failure<?> failure:
                assertEquals(failure.message, message);
                break;
        }
    }

}
