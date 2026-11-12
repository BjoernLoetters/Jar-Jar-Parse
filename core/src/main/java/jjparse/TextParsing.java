package jjparse;

import jjparse.diagnostic.Expectation;
import jjparse.input.Input;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension of the basic {@link Parsing} class that provides basic {@link Parser}s for {@link Parsing} with
 * {@link String}s and Unicode texts. This class fixes any {@link Input} to an {@link Input} of {@link Integer}s.
 *
 * @author Björn Lötters
 *
 * @see Parsing
 * @see String
 * @see Character
 */
public abstract class TextParsing extends Parsing<Integer> {

    /* *** Public Parser API *** */

    /**
     * Creates a primitive {@link Parser} that attempts to parse the provided Unicode codepoint.
     * @param codepoint The Unicode codepoint which shall be parsed.
     * @return A {@link Parser} which parses the provided Unicode codepoint.
     */
    public Parser<Integer> codepoint(final int codepoint) {
        return oneOf(codepoint);
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse the provided {@link Character}.
     * @param character The {@link Character} which shall be parsed.
     * @return A {@link Parser} which parses the provided {@link Character}.
     */
    public Parser<Character> character(final char character) {
        return oneOf(character);
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse one of the provided Unicode codepoints.
     * @param codepoints The choice of Unicode codepoints which shall be parsed.
     * @return A {@link Parser} which parses one of the provided Unicode codepoints.
     */
    public Parser<Integer> oneOf(final int ... codepoints) {
        return new CodepointClassParser(codepoints);
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse one of the provided {@link Character}s.
     * @param characters The choice of {@link Character}s which shall be parsed.
     * @return A {@link Parser} which parses one of the provided {@link Character}s.
     */
    public Parser<Character> oneOf(final char ... characters) {
        final int[] codepoints = new int[characters.length];
        for (int i = 0; i < characters.length; ++i) codepoints[i] = characters[i];
        return oneOf(codepoints).map(codepoint -> (char) codepoint.intValue());
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse a Unicode codepoint in the given range.
     * @param from The lowest Unicode codepoint of the range (inclusive).
     * @param to The highest Unicode codepoint of the range (inclusive).
     * @return A {@link Parser} which parses a Unicode codepoint between {@code from} and {@code to}.
     */
    public Parser<Integer> range(final int from, final int to) {
        return new CodepointRangeParser(Math.min(from, to), Math.max(from, to));
    }

    /**
     * Creates a primitive {@link Parser} that attempts to parse a {@link Character} in the given range.
     * @param from The lowest {@link Character} of the range (inclusive).
     * @param to The highest {@link Character} of the range (inclusive).
     * @return A {@link Parser} which parses a {@link Character} between {@code from} and {@code to}.
     */
    public Parser<Character> range(final char from, final char to) {
        return range((int) from, to).map(codepoint -> (char) codepoint.intValue());
    }

    /** A {@link Parser} which parses any Unicode codepoint. */
    public final Parser<Integer> any = new Parser<Integer>() {

        @Override
        public Parsing<Integer>.Result<Integer> apply(final Input<Integer> input) {
        if (input.isEmpty()) {
            return new Error<Integer>(Expectation.from("anything"), input);
        } else {
            return new Success<Integer>(input.head(), input.tail());
        }
        }

    };

    /**
     * Creates a primitive {@link Parser} that parses the provided literal.
     * @param literal The literal that should be parsed.
     * @return A {@link Parser} that parses the provided literal.
     */
    public final Parser<String> literal(final String literal) {
        return primitive(new LiteralParser(literal));
    }

    /**
     * Creates a primitive {@link Parser} that performs a prefix match using the provided regular expression.
     * Since the {@link Input} is not eagerly known, such a prefix match may not work for anchors that depend
     * on the exact size of the {@link Input}.
     * @param pattern The regular expression that should be used for the prefix match.
     * @return A {@link Parser} that performs a prefix match using the provided regular expression.
     * @see Pattern
     * @see Matcher#lookingAt()
     */
    public final Parser<String> regex(final String pattern) {
        return primitive(new RegexParser(pattern));
    }

    /** A {@link Parser} which parses any Unicode codepoint that belongs to the property list {@code White_Space}. */
    public final Parser<Void> whitespace = primitive(oneOf(
        0x0009, 0x000A, 0x000B, 0x000C, 0x000D,
        0x0020,
        0x0085,
        0x00A0,
        0x1680,
        0x2000, 0x2001, 0x2002, 0x2003, 0x2004,
        0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
        0x2028, 0x2029,
        0x202F,
        0x205F,
        0x3000
    ).repeat1().ignore()).expect("whitespace");

    /** A {@link Parser} which parses a Latin lowercase letter (a - z). */
    public final Parser<Character> lowercase = range('a', 'z').expect("a Latin lowercase letter");

    /** A {@link Parser} which parses a Latin uppercase letter (A - Z). */
    public final Parser<Character> uppercase = range('A', 'Z').expect("a Latin uppercase letter");

    /** A {@link Parser} which parses a single digit (0 - 9). */
    public final Parser<Character> digit = range('0', '9').expect("a digit");

    /** A {@link Parser} which parses a sequence of at least one digit. */
    public final Parser<String> digits1 = primitive(digit.repeat1().map(digits -> {
        final char[] characters = new char[digits.size()];
        int index = 0;
        for (final char digit : digits) characters[index++] = digit;
        return new String(characters);
    })).expect("a sequence of digits");

    /** A {@link Parser} which parses a sequence of possibly zero digits. */
    public final Parser<String> digits0 = digits1.optional().map(result -> result.orElse(""));

    /** A {@link Parser} which parses the traditional C identifier. */
    public final Parser<String> identifier =
        primitive(choice(lowercase, uppercase, character('_')).and(
            choice(lowercase, uppercase, character('_'), digit).repeat()
        )).map(result -> {
            final char[] characters = new char[result.second().size() + 1];
            characters[0] = result.first();
            int index = 1;
            for (final char character : result.second()) characters[index++] = character;
            return new String(characters);
        }).expect("a C identifier");

    /** A {@link Parser} which parses a natural number as a 64-bit integer. */
    public final Parser<Long> nat64 = primitive(digit.repeat1()).map(digits -> {
        long result = 0;
        for (final char digit : digits) {
            result *= 10;
            result += (digit - '0');
        }
        return result;
    }).expect("a natural number");

    /** A {@link Parser} which parses a natural number as a 32-bit integer. */
    public final Parser<Integer> nat32 = nat64.map(Long::intValue);

    /** A {@link Parser} which parses a sign and returns `true` for a positive and `false` for a negative sign. */
    public final Parser<Boolean> sign = oneOf('+', '-').map(character -> character.equals('+')).expect("a sign");

    /** A {@link Parser} which parses a 32-bit integer. */
    public final Parser<Integer> int32 = primitive(sign.optional(() -> true).and(nat32))
        .map(result -> result.first() ? result.second() : -result.second())
        .expect("an integer number");

    /** A {@link Parser} which parses a 64-bit integer. */
    public final Parser<Long> int64 = primitive(sign.optional(() -> true).and(nat64))
        .map(result -> result.first() ? result.second() : -result.second())
        .expect("an integer number");

    /** A {@link Parser} which parses a natural number. */
    public final Parser<BigInteger> natural = digits1.map(BigInteger::new).expect("a natural number");

    /** A {@link Parser} which parses an integer. */
    public final Parser<BigInteger> integer = primitive(sign.optional(() -> true).and(natural))
        .map(result -> result.first() ? result.second() : result.second().negate())
        .expect("an integer number");

    /** A {@link Parser} which parses a 32-bit IEEE 754 floating point number. */
    public final Parser<Float> float32 = regex("[+-]?(?:NaN|Infinity|0[xX](?:[0-9a-fA-F]+(?:\\.[0-9a-fA-F]*)?|\\.[0-9a-fA-F]+)[pP][+-]?[0-9]+[fFdD]?|(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?[fFdD]?)")
        .map(Float::parseFloat).expect("a floating point number");

    /** A {@link Parser} which parses a 64-bit IEEE 754 floating point number. */
    public final Parser<Double> float64 = regex("[+-]?(?:NaN|Infinity|0[xX](?:[0-9a-fA-F]+(?:\\.[0-9a-fA-F]*)?|\\.[0-9a-fA-F]+)[pP][+-]?[0-9]+[fFdD]?|(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?[fFdD]?)")
        .map(Double::parseDouble).expect("a floating point number");

    /* *** Primitive Parser Implementations *** */

    private static boolean isPrintable(final int codepoint) {
        return !Character.isWhitespace(codepoint) && !Character.isISOControl(codepoint) && Character.isDefined(codepoint);
    }

    private static String representation(final int codepoint) {
        if (isPrintable(codepoint)) {
            return Character.toString(codepoint);
        } else if (codepoint >= 0 && codepoint <= Character.MAX_VALUE) {
            return String.format("\\u%04X", codepoint);
        } else {
            return String.format("\\u%08X", codepoint);
        }
    }

    private final class CodepointRangeParser extends Parser<Integer> {

        private final int min;
        private final int max;
        private final Expectation expectation;

        private CodepointRangeParser(final int min, final int max) {
            this.min = min;
            this.max = max;
            if (this.min > this.max) {
                this.expectation = Expectation.from("nothing");
            } else if (this.max == this.min) {
                this.expectation = Expectation.from("the token '%s'", Character.toString(this.min));
            } else if (this.max - this.min == 1) {
                this.expectation = Expectation.from("anything that matches '[%s%s]'", representation(min), representation(max));
            } else {
                this.expectation = Expectation.from("anything that matches '[%s-%s]'", representation(min), representation(max));
            }
        }

        @Override
        public Result<Integer> apply(final Input<Integer> input) {
            if (input.nonEmpty()) {
                final int codepoint = input.head();
                if (codepoint >= min && codepoint <= max) {
                    return new Success<Integer>(input.head(), input.tail());
                } else {
                    return new Error<Integer>(expectation, input);
                }
            } else {
                return new Error<Integer>(expectation, input);
            }
        }

    }

    private final class CodepointClassParser extends Parser<Integer> {

        private final long[] allowed;
        private final int min;
        private final int max;
        private final Expectation expectation;

        private CodepointClassParser(final int[] codepoints) {
            if (codepoints.length == 0) {
                this.min = 1;
                this.max = 0;
                this.allowed = new long[0];
                this.expectation = Expectation.from("nothing");
                return;
            }

            if (codepoints.length == 1) {
                this.min = this.max = codepoints[0];
                this.allowed = new long[] { 1L };
                this.expectation = Expectation.from("the token '%s'", Character.toString(this.min));
                return;
            }

            Arrays.sort(codepoints);

            this.min = codepoints[0];
            this.max = codepoints[codepoints.length - 1];
            this.allowed = new long[(max - min + 1 + 63) >>> 6];
            final StringBuilder pattern = new StringBuilder("anything that matches '[");
            int start = 0, end = 0;
            for (int i = 0; i < codepoints.length; ++i) {
                final int codepoint = codepoints[i];
                final int index = codepoint - min;
                final int longIndex = index >>> 6;
                final int bitIndex = index & 63;
                this.allowed[longIndex] = this.allowed[longIndex] | (1L << bitIndex);
                if (i == 0) {
                    start = codepoint;
                    end = codepoint;
                } else if (codepoint - codepoints[i - 1] < 2) {
                    // The distance of the current codepoint to the previous one is 1 (i.e., a successor) or 0 (i.e., a duplicate).
                    end = codepoint;
                } else {
                    if (end - start == 0) {
                        pattern.append(representation(start));
                    } else if (end - start == 1) {
                        pattern.append(representation(start));
                        pattern.append(representation(end));
                    } else {
                        pattern.append(representation(start));
                        pattern.append('-');
                        pattern.append(representation(end));
                    }

                    start = codepoint;
                    end = codepoint;
                }
            }

            // The `codepoints` array cannot be empty at this point, so `end` and `start` are initialized by the previous loop.
            if (end - start == 0) {
                pattern.append(representation(start));
            } else if (end - start == 1) {
                pattern.append(representation(start));
                pattern.append(representation(end));
            } else {
                pattern.append(representation(start));
                pattern.append('-');
                pattern.append(representation(end));
            }

            pattern.append("]'");
            this.expectation = Expectation.from(pattern.toString());
        }

        private boolean isAllowed(final int codepoint) {
            if (codepoint < min || codepoint > max) return false;
            final int index = codepoint - min;
            final int longIndex = index >>> 6;
            final int bitIndex = index & 63;
            return (this.allowed[longIndex] & (1L << bitIndex)) != 0L;
        }

        @Override
        public Result<Integer> apply(final Input<Integer> input) {
            if (input.nonEmpty() && isAllowed(input.head())) {
                return new Success<Integer>(input.head(), input.tail());
            } else {
                return new Error<Integer>(expectation, input);
            }
        }

    }

    private final class LiteralParser extends Parser<String> {

        private final String literal;
        private final int[] codepoints;
        private final Expectation expectation;

        private LiteralParser(final String literal) {
            this.codepoints = literal.codePoints().toArray();
            this.literal = literal;
            this.expectation = Expectation.from("the literal '%s'", literal);
        }

        @Override
        public Parsing<Integer>.Result<String> apply(final Input<Integer> input) {
            Input<Integer> current = input;
            int index = 0;

            while (index < codepoints.length && current.nonEmpty()) {
                if (codepoints[index] != current.head()) {
                    return new Error<String>(expectation, current);
                }

                index += 1;
                current = current.tail();
            }

            if (index < codepoints.length) {
                return new Error<String>(expectation, current);
            }

            return new Success<String>(literal, current);
        }
    }

    private final class RegexParser extends Parser<String> {

        private final Pattern pattern;
        private final Expectation expectation;

        private RegexParser(final String pattern) {
            this.pattern = Pattern.compile(pattern);
            this.expectation = Expectation.from("anything that matches '%s'", pattern);
        }

        @Override
        public Parsing<Integer>.Result<String> apply(final Input<Integer> input) {
            final CharSequenceBuffer buffer = new CharSequenceBuffer(input);

            /*
                Java's regular expressions are made for eager data structures like char sequences.
                In order to make them work with the lazy input data structure, we proceed in two phases:

                1. Phase (speculative matching): We assume that the input is infinite and allow engine to
                consume as much as is needed. In this way, prefix matches are correctly recognized. However,
                in case of end anchors this may result in unexpected behavior, as the length of the input is
                assumed to be `Integer.MAX_VALUE`. Consequently, this implementation only covers prefix matches.

                2. Phase (exact matching): If the engine hits the actual end of the input, an `EndOfInput` exception
                is triggered. In this case, we know the entire input and can run the engine again to decide whether
                the entire input matches or not.
             */
            try {
                // 1. Phase (speculative matching): The matcher may consume as much as is needed. This may trigger an
                // `EndOfInput` exception, if we hit the actual end of the input.
                return match(input, pattern.matcher(buffer));
            } catch (final EndOfInput ignore) {
                // 2. Phase (exact matching): The length of the input is now completely known and a second call should
                // never trigger an `EndOfInput` exception again.
                return match(input, pattern.matcher(buffer));
            }
        }

        private Parsing<Integer>.Result<String> match(Input<Integer> input, final Matcher matcher) throws EndOfInput {
            if (!matcher.lookingAt()) {
                return new Error<String>(expectation, input);
            }

            final String result = matcher.group();
            // Skip the matched codepoints to obtain the rest of the input
            int consumed = result.codePointCount(0, result.length());
            while (consumed-- > 0) input = input.tail();

            return new Success<String>(result, input);
        }

        private final static class EndOfInput extends RuntimeException {

            private static final EndOfInput instance = new EndOfInput();

            private EndOfInput() {
                super(null, null, false, false);
            }

        }

        private final static class CharSequenceBuffer implements CharSequence {

            private Input<Integer> input;

            private char[] buffer = new char[16];

            private int size = 0;

            private CharSequenceBuffer(final Input<Integer> input) {
                this.input = input;
            }

            @Override
            public int length() {
                return input.isEmpty() ? size : Integer.MAX_VALUE;
            }

            @Override
            public char charAt(int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException(index);
                }

                while (index >= size && input.nonEmpty()) {
                    final int codepoint = input.head();
                    final int required = Character.charCount(codepoint);

                    if (size + required > buffer.length) {
                        final char[] temporary = new char[Math.max(buffer.length * 2, size + required)];
                        System.arraycopy(buffer, 0, temporary, 0, size);
                        buffer = temporary;
                    }

                    if (required == 1) {
                        buffer[size++] = (char) codepoint;
                    } else {
                        buffer[size++] = Character.highSurrogate(codepoint);
                        buffer[size++] = Character.lowSurrogate(codepoint);
                    }

                    input = input.tail();
                }

                if (index >= size) {
                    throw EndOfInput.instance;
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
                if (end > 0) {
                    final char ignore = charAt(end - 1);
                }

                return new String(buffer, start, end - start);
            }

            @Override
            public String toString() {
                return new String(buffer, 0, size);
            }

        }

    }

}
