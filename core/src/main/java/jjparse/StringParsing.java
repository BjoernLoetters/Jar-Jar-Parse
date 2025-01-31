package jjparse;

import jjparse.description.Description;
import jjparse.description.Literal;
import jjparse.description.RegExp;
import jjparse.input.CharacterInput;
import jjparse.input.Input;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringParsing extends Parsing<Character> {

    public StringParsing() {
        setSkipParser(regex("\\s+"));
    }

    /* *** Public Parser API *** */

    public Parser<String> literal(final String literal) {
        return new LiteralParser(literal);
    }

    public Parser<String> regex(final String pattern) {
        return new RegExpParser(pattern);
    }

    public Parser<Character> character(final char character) {
        return literal("" + character).map(c -> c.charAt(0));
    }

    public final Parser<Character> lowercase = regex("[a-z]").map(c -> c.charAt(0));

    public final Parser<Character> uppercase = regex("[A-Z]").map(c -> c.charAt(0));

    public final Parser<String> identifier = regex("[a-zA-Z_][a-zA-Z_0-9]*");

    public final Parser<Integer> digit = regex("[0-9]").map(Integer::parseInt);

    public final Parser<Integer> number = regex("[0-9]+").map(Integer::parseInt);

    public final Parser<Integer> int32 = regex("[+-]?[0-9]+").map(Integer::parseInt);

    public final Parser<Long> int64 = regex("[+-]?[0-9]+").map(Long::parseLong);

    public final Parser<BigInteger> integer = regex("[+-]?[0-9]+").map(BigInteger::new);

    public final Parser<Float> float32 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Float::parseFloat);

    public final Parser<Double> float64 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Double::parseDouble);

    /* *** Primitive Parser Implementations *** */

    private final class LiteralParser extends Parser<String> {

        private final String literal;

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

    private final class RegExpParser extends Parser<String> {

        private final Pattern pattern;

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
                    return new Error<String>(Failure.format(sequence, description()), sequence);
                }
            } else {
                // TODO: Collect the characters in a buffer and match on this buffer
                throw new UnsupportedOperationException();
            }
        }

    }

}
