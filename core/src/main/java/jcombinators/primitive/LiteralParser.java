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
    public Description description() {
        return new Literal(literal);
    }

    @Override
    public Result<String> apply(Input input) {
        input = input.skipWhiteSpace();
        final int length = literal.length();

        int i = 0;
        while (!input.isEmpty() && i < length) {
            final int a = input.getCodePoint();
            final int b = literal.codePointAt(i);

            if (a != b) {
                break;
            }

            input = input.next();
            i += Character.charCount(a);
        }

        if (i == length) {
            return new Success<>(literal, input);
        } else {
            return new Error<>(Failure.format(input, description()), input);
        }
    }

}
