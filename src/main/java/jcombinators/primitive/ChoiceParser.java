package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.description.Choice;
import jcombinators.description.Description;
import jcombinators.input.Input;
import jcombinators.result.*;
import jcombinators.result.Error;

import java.util.List;

public final class ChoiceParser<T> implements Parser<T> {

    private final Parser<T> first;

    private final Parser<T> second;

    public ChoiceParser(final Parser<T> first, final Parser<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final Description description() {
        return new Choice(List.of(first.description(), second.description()));
    }

    @Override
    public final Result<T> apply(final Input input) {
        return switch (first.apply(input)) {
            case Error<T> firstError -> switch (second.apply(input)) {
                case Success<T> success -> success;
                case Abort<T> abort -> abort;
                case Error<T> secondError -> new Error<>(Failure.format(secondError.rest, description()), secondError.rest);
            };
            case Abort<T> abort -> abort;
            case Success<T> success -> success;
        };
    }

}
