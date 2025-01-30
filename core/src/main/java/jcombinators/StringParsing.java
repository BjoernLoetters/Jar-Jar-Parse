package jcombinators;

import jcombinators.description.Description;
import jcombinators.description.Literal;
import jcombinators.description.RegExp;
import jcombinators.input.CharacterInput;
import jcombinators.input.Input;

import java.math.BigInteger;
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
            Input<Character> current = input;
            int index = 0;

            while (current.nonEmpty() && index < literal.length()) {
                if (current.head() != literal.charAt(index)) {
                    return new Error<String>(Failure.format(current, description()), input);
                }

                ++index;
                current = current.tail();
            }

            if (index < literal.length()) {
                // We hit the end of the input
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
            if (input instanceof CharacterInput sequence) {
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
