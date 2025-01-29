package jcombinators;

import jcombinators.description.Description;
import jcombinators.description.Literal;
import jcombinators.description.RegExp;
import jcombinators.input.CharInput;
import jcombinators.input.Input;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringParsing extends Parsing<Character> {

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
        public Result<String> apply(Input<Character> input) {
            input = input.skip();

            Input<Character> current = input;
            int i = 0;

            while (!current.isEmpty() && i < literal.length()) {
                final int a;
                final int b = Character.codePointAt(literal, i);

                final Input<Character> previous = current;
                final char high = current.head();
                if (Character.isHighSurrogate(high)) {
                    final Input<Character> temporary = current.tail();
                    if (!temporary.isEmpty() && Character.isLowSurrogate(temporary.head())) {
                        current = temporary;
                        a = Character.toCodePoint(high, temporary.head());
                    } else {
                        a = high;
                    }
                } else {
                    a = high;
                }

                if (a != b) {
                    return new Error<String>(Failure.format(previous, description()), input);
                }

                current = current.tail();
                i += Character.charCount(b);
            }

            if (i < literal.length()) {
                // End of input
                return new Error<String>(Failure.format(current, description()), input);
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
        public Result<String> apply(Input<Character> input) {
            input = input.skip();

            if (input instanceof CharInput charInput) {
                final Matcher matcher = pattern.matcher(charInput);
                if (matcher.lookingAt()) {
                    final String value = matcher.group();
                    return new Success<String>(value, charInput.subSequence(value.length(), charInput.length()));
                } else {
                    return new Error<String>(Failure.format(charInput, description()), charInput);
                }
            } else {
                // TODO: Collect characters in a buffer and match on this buffer.
                throw new UnsupportedOperationException();
            }
        }

    }

}
