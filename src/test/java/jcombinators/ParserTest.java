package jcombinators;

import jcombinators.result.Failure;
import jcombinators.result.Success;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class ParserTest {

    public final <T> void assertSuccess(final Parser<T> parser, final T expected, String input) {
        switch (parser.apply(input, 0)) {
            case Success<T> success:
                assertEquals(expected, success.value);
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
                assertEquals(message, failure.message);
                break;
        }
    }

}
