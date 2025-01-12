package jcombinators;

import jcombinators.input.Input;
import jcombinators.result.Failure;
import jcombinators.result.Success;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class ParserTest {

    public final Optional<String> getTestName() {
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
                return Optional.of(name);
            }
        }

        return Optional.empty();
    }

    public final <T> void assertSuccess(final Parser<T> parser, final T expected, final String string) {
        final Input input = Input.of("Test '" + getTestName() + "'", string);
        switch (parser.apply(input)) {
            case Success<T> success:
                assertEquals(expected, success.value);
                break;

            case Failure<T> failure:
                fail("expected parse success, but got failure with message '" + failure.message + "'");
                break;
        }
    }

    public final void assertFailure(final Parser<?> parser, final String message, final String string) {
        final Input input = Input.of("Test '" + getTestName() + "'", string);
        switch (parser.apply(input)) {
            case Success<?> success:
                fail("expected parse failure, but got success with value '" + success.value + "'");
                break;

            case Failure<?> failure:
                assertEquals(message, failure.message);
                break;
        }
    }

}
