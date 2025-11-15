package jjparse;

import jjparse.description.*;
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
        return oneOf(character);
    }

    public Parser<Character> oneOf(final char ... characters) {
        if (characters.length == 0) {
            return new Parser<Character>() {
                @Override
                public Result<Character> apply(final Input<Character> input) {
                    final Input<Character> skipped = skip(input);
                    return new Error<Character>(Failure.format(skipped, description()), skipped);
                }
            };
        } else {
            return new CharacterClassParser(characters);
        }
    }

    public Parser<Character> range(final char from, final char to) {
        return new CharacterRangeParser((char) Math.min(from, to), (char) Math.max(from, to));
    }

    /** A {@link Parser} which parses a Latin lowercase letter (a - z). */
    public final Parser<Character> lowercase = range('a', 'z');

    /** A {@link Parser} which parses a Latin uppercase letter (A - Z). */
    public final Parser<Character> uppercase = range('A', 'Z');

    /** A {@link Parser} which parses a single digit (0 - 9). */
    public final Parser<Character> digit = range('0', '9');

    /** A {@link Parser} which parses a sequence of at least one digit. */
    public final Parser<String> digits1 = digit.repeat1().map(digits -> {
        final char[] characters = new char[digits.size()];
        int index = 0;
        for (final char digit : digits) characters[index++] = digit;
        return new String(characters);
    });

    /** A {@link Parser} which parses a sequence of possibly zero digits. */
    public final Parser<String> digits0 = digits1.optional().map(result -> result.orElse(""));

    /** A {@link Parser} which parses the traditional C identifier. */
    public final Parser<String> identifier =
        choice(lowercase, uppercase, character('_')).and(
            choice(lowercase, uppercase, character('_'), digit).repeat()
        ).map(result -> {
            final char[] characters = new char[result.second().size() + 1];
            characters[0] = result.first();
            int index = 1;
            for (final char character : result.second()) characters[index++] = character;
            return new String(characters);
        });

    /** A {@link Parser} which parses a natural number as a 64-bit integer. */
    public final Parser<Long> nat64 = digit.repeat1().map(digits -> {
        long result = 0;
        for (final char digit : digits) {
            result *= 10;
            result += (digit - '0');
        }
        return result;
    });

    /** A {@link Parser} which parses a natural number as a 32-bit integer. */
    public final Parser<Integer> nat32 = nat64.map(Long::intValue);

    /** A {@link Parser} which parses a 32-bit integer. */
    public final Parser<Integer> int32 = oneOf('+', '-').optional().and(nat32)
        .map(result -> result.first().orElse('+') == '+' ? result.second() : -result.second());

    /** A {@link Parser} which parses a 64-bit integer. */
    public final Parser<Long> int64 = oneOf('+', '-').optional().and(nat64)
        .map(result -> result.first().orElse('+') == '+' ? result.second() : -result.second());

    /** A {@link Parser} which parses a natural number. */
    public final Parser<BigInteger> natural = digits1.map(BigInteger::new);

    /** A {@link Parser} which parses an integer. */
    public final Parser<BigInteger> integer = oneOf('+', '-').optional().and(natural)
        .map(result -> result.first().orElse('+') == '+' ? result.second() : result.second().negate());

    /** A {@link Parser} which parses a 32-bit IEEE 754 floating point number. */
    public final Parser<Float> float32 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Float::parseFloat);

    /** A {@link Parser} which parses a 64-bit IEEE 754 floating point number. */
    public final Parser<Double> float64 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Double::parseDouble);

    /* *** Primitive Parser Implementations *** */

    private final class CharacterRangeParser extends Parser<Character> {

        private final char min;
        private final char max;
        private final Description description;

        private CharacterRangeParser(final char min, final char max) {
            this.min = min;
            this.max = max;
            this.description = new CharacterRange(min, max);
        }

        @Override
        public Description description() {
            return description;
        }

        @Override
        public Result<Character> apply(final Input<Character> input) {
            final Input<Character> skipped = skip(input);

            if (skipped.nonEmpty()) {
                final char character = input.head();
                if (character < min || character > max) {
                    return new Error<Character>(Failure.format(skipped, description()), skipped);
                } else {
                    return new Success<Character>(skipped.head(), skipped.tail());
                }
            } else {
                return new Error<Character>(Failure.format(skipped, description()), skipped);
            }
        }

    }

    private final class CharacterClassParser extends Parser<Character> {

        private final long[] allowed;
        private final char min;
        private final char max;
        private final Description description;

        private CharacterClassParser(final char[] characters) {
            if (characters.length == 0) {
                throw new IllegalArgumentException("character class may not be empty");
            }

            this.description = new CharacterClass(characters);

            char min = Character.MAX_VALUE;
            char max = Character.MIN_VALUE;

            for (final char character : characters) {
                if (character < min) min = character;
                if (character > max) max = character;
            }

            this.min = min;
            this.max = max;
            this.allowed = new long[(max - min + 1 + 63) >>> 6];
            for (final char character : characters) {
                final int index = character - min;
                final int longIndex = index >>> 6;
                final int bitIndex = index & 63;
                this.allowed[longIndex] = this.allowed[longIndex] | (1L << bitIndex);
            }
        }

        @Override
        public Description description() {
            return description;
        }

        private boolean isAllowed(final char character) {
            if (character < min || character > max) return false;
            final int index = character - min;
            final int longIndex = index >>> 6;
            final int bitIndex = index & 63;
            return (this.allowed[longIndex] & (1L << bitIndex)) != 0L;
        }

        @Override
        public Result<Character> apply(final Input<Character> input) {
            final Input<Character> skipped = skip(input);

            if (skipped.nonEmpty() && isAllowed(skipped.head())) {
                return new Success<Character>(skipped.head(), skipped.tail());
            } else {
                return new Error<Character>(Failure.format(skipped, description()), skipped);
            }
        }

    }

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

            if (skipped instanceof CharacterInput sequence) {
                final Matcher matcher = pattern.matcher(sequence);
                if (matcher.lookingAt()) {
                    final String value = matcher.group();
                    return new Success<String>(value, sequence.subSequence(value.length(), sequence.length()));
                } else {
                    return new Error<String>(Failure.format(skipped, description()), skipped);
                }
            } else {
                final CharacterSequenceWrapper sequence = new CharacterSequenceWrapper(skipped);
                final Matcher matcher = pattern.matcher(sequence);
                if (matcher.lookingAt()) {
                    final String value = matcher.group();
                    Input<Character> rest = skipped;
                    for (int i = 0; i < value.length(); ++i) {
                        rest = rest.tail();
                    }
                    return new Success<String>(value, rest);
                } else {
                    return new Error<String>(Failure.format(skipped, description()), skipped);
                }
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
