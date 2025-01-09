package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

import java.util.function.Function;

public final class MapParser<A, B> implements Parser<B> {

    private final Parser<A> parser;

    private final Function<A, B> function;

    public MapParser(final Parser<A> parser, final Function<A, B> function) {
        this.parser = parser;
        this.function = function;
    }

    @Override
    public final Result<B> apply(final String input, final int offset) {
        return switch (parser.apply(input, offset)) {
            case Success<A> success -> new Success<>(function.apply(success.value), success.offset);
            case Failure<A> failure -> {
                @SuppressWarnings("unchecked")
                final Failure<B> result = (Failure<B>) failure;
                yield result;
            }
        };
    }

}
