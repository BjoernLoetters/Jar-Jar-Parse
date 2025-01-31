package jjparse;

import jjparse.description.Description;
import jjparse.description.Literal;
import jjparse.description.RegExp;
import jjparse.input.CharacterInput;
import jjparse.input.Input;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension of the basic {@link Parsing} class that provides basic {@link Parser}s for {@link Parsing} with
 * {@link String}s. This class fixes any {@link Input} to an {@link Input} of {@link Character}s.
 *
 * @author Björn Lötters
 *
 * @see Parsing
 */
public abstract class StringParsing extends Parsing<Character> {

    /**
     * Creates an instance of this {@link StringParsing} class and provides access to all {@link Parser}s and
     * {@link Parser} combinators in that way. Additionally, the skip {@link Parser} is initialized in order to skip
     * any white space as defined by the regular expression \s.
     *
     * @see #setSkipParser
     */
    public StringParsing() {
        setSkipParser(regex("\\s+"));
    }

    /* *** Public Parser API *** */

    /**
     * Creates a primitive {@link Parser} that attempts to parse the provided {@link String} literal.
     * @param literal The {@link String} literal which shall be parsed.
     * @return A {@link Parser} which parses the provided {@link String} literal.
     * @see #regex
     */
    public Parser<String> literal(final String literal) {
        return new LiteralParser(literal);
    }

    /**
     * Creates a primitive {@link Parser} that attempts to match the provided regular expression (which must be a valid
     * {@link Pattern}).
     * <br/><br/>
     * <b>Implementation Note</b>: Be careful of applying the returned {@link Parser} to an infinite {@link Input}, as
     * this might not terminate depending on the regular expression.
     * @param pattern The regular expression which shall be used for matching.
     * @return A {@link Parser} that attempts to parse the provided regular expression.
     * @see Pattern
     * @see #literal
     */
    public Parser<String> regex(final String pattern) {
        return new RegExpParser(pattern);
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse the provided {@link Character}.
     * @param character The {@link Character} which shall be parsed.
     * @return A {@link Parser} which parses the provided {@link Character}.
     */
    public Parser<Character> character(final char character) {
        return literal("" + character).map(c -> c.charAt(0));
    }

    /** A {@link Parser} which parses a Latin lowercase letter (a - z). */
    public final Parser<Character> lowercase = regex("[a-z]").map(c -> c.charAt(0));

    /** A {@link Parser} which parses a Latin uppercase letter (A - Z). */
    public final Parser<Character> uppercase = regex("[A-Z]").map(c -> c.charAt(0));

    /** A {@link Parser} which parses the traditional C identifier. */
    public final Parser<String> identifier = regex("[a-zA-Z_][a-zA-Z_0-9]*");

    /** A {@link Parser} which parses a single digit (0 - 9). */
    public final Parser<Integer> digit = regex("[0-9]").map(Integer::parseInt);

    /** A {@link Parser} which parses an unsigned 32-bit integer. */
    public final Parser<Integer> number = regex("[0-9]+").map(Integer::parseInt);

    /** A {@link Parser} which parses a signed 32-bit integer. */
    public final Parser<Integer> int32 = regex("[+-]?[0-9]+").map(Integer::parseInt);

    /** A {@link Parser} which parses a signed 64-bit integer. */
    public final Parser<Long> int64 = regex("[+-]?[0-9]+").map(Long::parseLong);

    /** A {@link Parser} which parses a signed integer. */
    public final Parser<BigInteger> integer = regex("[+-]?[0-9]+").map(BigInteger::new);

    /** A {@link Parser} which parses a 32-bit IEEE 754 floating point number. */
    public final Parser<Float> float32 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Float::parseFloat);

    /** A {@link Parser} which parses a 64-bit IEEE 754 floating point number. */
    public final Parser<Double> float64 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Double::parseDouble);

    /* *** Primitive Parser Implementations *** */

    /**
     * A primitive {@link Parser} for parsing {@link String} literals.
     *
     * @author Björn Lötters
     *
     * @see StringParsing#literal
     * @see RegExpParser
     */
    private final class LiteralParser extends Parser<String> {

        /** The {@link String} literal that shall be parsed. */
        private final String literal;

        /**
         * Creates a new {@link LiteralParser}.
         * @param literal The {@link String} literal that shall be parsed.
         */
        private LiteralParser(final String literal) {
            this.literal = literal;
        }

        @Override
        public Description description() {
            return new Literal(literal);
        }

        @Override
        public Result<String> apply(final Input<Character> input) {
            final Input<Character> skipped = skip(input);

            Input<Character> current = skipped;
            int index = 0;
            while (current.nonEmpty() && index < literal.length()) {
                final int a;
                final int b = literal.codePointAt(index);

                // Read the full code point of this character input, such that error messages only capture full code points
                final Input<Character> previous = current;
                if (Character.isHighSurrogate(current.head())) {
                    final Input<Character> temporary = current.tail();
                    if (temporary.nonEmpty() && Character.isLowSurrogate(temporary.head())) {
                        a = Character.toCodePoint(current.head(), temporary.head());
                        current = temporary;
                    } else {
                        a = current.head();
                    }
                } else {
                    a = current.head();
                }

                if (a != b) {
                    return new Error<String>(Failure.format(previous, description()), skipped);
                }

                index += Character.charCount(a);
                current = current.tail();
            }

            if (index < literal.length()) {
                // We hit the end of the input
                return new Error<String>(Failure.format(current, description()), skipped);
            }

            return new Success<String>(literal, current);
        }

    }

    /**
     * A primitive {@link Parser} for parsing regular expressions. Be careful of applying such a {@link Parser} to an
     * infinite stream of {@link Character}s, as this may not terminate.
     *
     * @author Björn Lötters
     *
     * @see StringParsing#regex
     * @see LiteralParser
     */
    private final class RegExpParser extends Parser<String> {

        /** The compiled {@link Pattern} for this {@link RegExpParser}. */
        private final Pattern pattern;

        /**
         * Creates a new {@link RegExpParser}.
         * @param pattern The regular expression, which must be a valid {@link Pattern}.
         * @throws java.util.regex.PatternSyntaxException If the provided regular expression is not well-formed.
         */
        private RegExpParser(final String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Description description() {
            return new RegExp(pattern);
        }

        @Override
        public Result<String> apply(final Input<Character> input) {
            final Input<Character> skipped = skip(input);

            final CharSequence sequence;
            if (skipped instanceof CharacterInput characterInput) {
                sequence = characterInput;
            } else {
                sequence = new CharacterSequenceWrapper(input);
            }

            final Matcher matcher = pattern.matcher(sequence);
            if (matcher.lookingAt()) {
                final String value = matcher.group();
                return new Success<String>(value, sequence.subSequence(value.length(), sequence.length()));
            } else {
                return new Error<String>(Failure.format(sequence, description()), sequence);
            }
        }

    }

    /**
     * This class is quite hack: It wraps a possibly infinite {@link Input} and implements the {@link CharSequence}
     * interface by lazily buffering the {@link Character}s of the underlying {@link Input} as needed. As long as the
     * end of the underlying {@link Input} is not reached, this class returns {@link Integer#MAX_VALUE} as the
     * {@link CharSequence#length}.
     *
     * @author Björn Lötters
     */
    private static final class CharacterSequenceWrapper implements CharSequence {

        /** The underlying {@link Input}. */
        private Input<Character> current;

        /** The internal buffer of {@link Character}s. */
        private char[] buffer = new char[0];

        /** The actual size of the sub array in the buffer that is currently used. */
        private int bufferSize = 0;

        /**
         * Creates a new {@link CharacterSequenceWrapper}.
         * @param input The {@link Input} which serves as the underlying source.
         */
        private CharacterSequenceWrapper(final Input<Character> input) {
            this.current = input;
        }

        @Override
        public int length() {
            return current.isEmpty() ? bufferSize : Integer.MAX_VALUE;
        }

        @Override
        public char charAt(int index) {
            while (index >= bufferSize && current.nonEmpty()) {
                if (bufferSize >= buffer.length) {
                    final char[] temporary = new char[buffer.length * 2];
                    System.arraycopy(buffer, 0, temporary, 0, buffer.length);
                    buffer = temporary;
                }

                buffer[bufferSize++] = current.head();
                current = current.tail();
            }

            if (index >= bufferSize) {
                throw new IndexOutOfBoundsException(index);
            } else {
                return buffer[index];
            }
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            if (start < 0 || start > end) {
                throw new IndexOutOfBoundsException(start);
            }

            // Ensure that enough characters are stored in the buffer
            try {
                charAt(start);
            } catch (final IndexOutOfBoundsException ignore) { }

            if (end > bufferSize) {
                throw new IndexOutOfBoundsException(end);
            }

            return new String(buffer, start, end - start);
        }

        @Override
        public String toString() {
            return new String(buffer, 0, bufferSize);
        }

    }

}
