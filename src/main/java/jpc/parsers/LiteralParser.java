package jpc.parsers;

import jpc.Parser;
import jpc.result.Error;
import jpc.result.Result;
import jpc.result.Success;

public final class LiteralParser implements Parser<String> {

    private final String literal;

    public LiteralParser(final String literal) {
        this.literal = literal;
    }

    @Override
    public final Result<String> apply(final String input, final int offset) {
        final int inputLength = input.length();
        final int prefixLength = literal.length();
        int i = 0;

        while ((offset + i) < inputLength && i < prefixLength) {
            final int a = input.codePointAt(offset + i);
            final int b = literal.codePointAt(i);

            if (a != b) {
                break;
            }

            i += Character.charCount(a);
        }

        if (i >= prefixLength) {
            return new Success<>(literal, offset + literal.length());
        } else if (offset + i >= input.length()) {
            return new Error<>("unexpected end of input, expected literal '" + literal + "'", offset);
        } else {
            final String cp = input.substring(offset + i, offset + i + Character.charCount(input.codePointAt(offset + i)));
            return new Error<>("unexpected character '" + cp + "', expected literal '" + literal + "'", offset);
        }
    }

}
