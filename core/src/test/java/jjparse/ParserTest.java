package jjparse;

import jjparse.input.Input;
import org.junit.Test;

import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class ParserTest extends TextParsing {

    public final String getTestName() {
        final StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (int i = stack.length - 1; i >= 0; --i) {
            final StackTraceElement element = stack[i];
            String name = null;
            try {
                Class<?> cls = Class.forName(element.getClassName());
                for (Method method : cls.getDeclaredMethods()) {
                    if (method.getName().equals(element.getMethodName())) {
                        // Check if the method has the specified annotation
                        if (method.isAnnotationPresent(Test.class)) {
                            name = method.getName();
                            break;
                        }
                    }
                }
            } catch (final ClassNotFoundException | SecurityException exception) {
                throw new RuntimeException(exception);
            }

            if (name != null) {
                return name;
            }
        }

        throw new RuntimeException("not a test");
    }

    public final <T> void assertSuccess(final Parser<T> parser, final T expected, final Input<Integer> input) {
        switch (parser.apply(input)) {
            case Success<T> success:
                assertEquals(expected, success.value);
                break;

            case Failure<T> failure:
                fail("expected parse success, but got failure with message '" + failure.format() + "'");
                break;
        }
    }

    public final <T> void assertSuccess(final Parser<T> parser, final T expected, final String string) {
        final Input<Integer> input1 = Input.of("Test '" + getTestName() + "'", string).withFilter(whitespace);
        final Input<Integer> input2 = Input.of("Test '" + getTestName() + "'", string.codePoints().boxed()).withFilter(whitespace);
        assertSuccess(parser, expected, input1);
        assertSuccess(parser, expected, input2);
    }

    public final void assertFailure(final Parser<?> parser, final String message, final Input<Integer> input) {
        switch (parser.apply(input)) {
            case Success<?> success:
                fail("expected parse failure, but got success with value '" + success.value + "'");
                break;

            case Failure<?> failure:
                assertEquals(message, failure.format());
                break;
        }
    }

    public final void assertFailure(final Parser<?> parser, final String message, final String string) {
        final Input<Integer> input = Input.of("Test '" + getTestName() + "'", string).withFilter(whitespace);
        assertFailure(parser, message, input);
    }

}
