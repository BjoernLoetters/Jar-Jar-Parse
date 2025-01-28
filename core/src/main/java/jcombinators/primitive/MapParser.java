package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.description.Description;
import jcombinators.input.Input;
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
    public Description description() {
        return parser.description();
    }

    @Override
    public Result<B> apply(final Input input) {
        return switch (parser.apply(input.skipWhiteSpace())) {
            case Success<A> success -> new Success<>(function.apply(success.value), success.rest);
            case Failure<A> failure -> {
                @SuppressWarnings("unchecked")
                final Failure<B> result = (Failure<B>) failure;
                yield result;
            }
        };
    }

}
