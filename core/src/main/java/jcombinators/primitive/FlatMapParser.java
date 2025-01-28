package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.description.Description;
import jcombinators.input.Input;
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
    public Description description() {
        return parser.description();
    }

    @Override
    public Result<U> apply(final Input input) {
        return switch (parser.apply(input.skipWhiteSpace())) {
            case Success<T> success ->
                function.apply(success.value).apply(success.rest.skipWhiteSpace());
            case Failure<T> failure -> {
                @SuppressWarnings("unchecked")
                final Failure<U> result = (Failure<U>) failure;
                yield result;
            }
        };
    }

}
