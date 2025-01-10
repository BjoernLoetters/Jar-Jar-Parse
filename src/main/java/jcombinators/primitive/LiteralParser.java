package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.description.Description;
import jcombinators.description.Literal;
import jcombinators.input.Input;
import jcombinators.result.Error;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

public final class LiteralParser implements Parser<String> {

    private final String literal;

    public LiteralParser(final String literal) {
        this.literal = literal;
    }

    @Override
    public final Description description() {
        return new Literal(literal);
    }

    @Override
    public final Result<String> apply(final Input input) {
        final int inputLength = input.length();
        final int prefixLength = literal.length();
        int i = 0;

        while (i < inputLength && i < prefixLength) {
            final int a = input.codePointAt(i);
            final int b = literal.codePointAt(i);

            if (a != b) {
                break;
            }

            i += Character.charCount(a);
        }

        if (i == prefixLength) {
            return new Success<>(literal, input.subSequence(i));
        } else {
            return new Error<>(Failure.format(input.subSequence(i), description()), input);
        }
    }

}
