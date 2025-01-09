package jcombinators.parsers;

import jcombinators.Parser;
import jcombinators.result.*;
import jcombinators.result.Error;

public final class ChoiceParser<T> implements Parser<T> {

    private final Parser<T> first;

    private final Parser<T> second;

    public ChoiceParser(final Parser<T> first, final Parser<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final Result<T> apply(final String input, final int offset) {
        return switch (first.apply(input, offset)) {
            case Error<T> ignore -> second.apply(input, offset);
            case Abort<T> abort -> abort;
            case Success<T> success -> success;
        };
    }

}
