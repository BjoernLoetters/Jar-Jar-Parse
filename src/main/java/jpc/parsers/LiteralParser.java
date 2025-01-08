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
        if (input.startsWith(literal, offset)) {
            return new Success<>(literal, offset + literal.length());
        } else if (offset >= input.length()) {
            return new Error<>("unexpected end of input, expected literal '" + literal + "'", offset);
        } else {
            return new Error<>("unexpected character '" + input.charAt(offset) + "', expected literal '" + literal + "'", offset);
        }
    }

}
