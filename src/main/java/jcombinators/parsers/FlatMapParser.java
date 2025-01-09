package jcombinators.parsers;

import jcombinators.Parser;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

import java.util.function.Function;

public final class FlatMapParser<T, U> implements Parser<U> {

    private final Parser<T> parser;

    private final Function<T, Parser<U>> function;

    public FlatMapParser(final Parser<T> parser, final Function<T, Parser<U>> function) {
        this.parser = parser;
        this.function = function;
    }

    @Override
    public final Result<U> apply(final String input, final int offset) {
        return switch (parser.apply(input, offset)) {
            case Success<T> success -> function.apply(success.value).apply(input, success.offset);
            case Failure<T> failure -> {
                @SuppressWarnings("unchecked")
                final Failure<U> result = (Failure<U>) failure;
                yield result;
            }
        };
    }

}
