package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.input.Input;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SequenceParser<T> implements Parser<List<T>> {

    private final List<Parser<T>> parsers;

    public SequenceParser(final List<Parser<T>> parsers) {
        this.parsers = parsers;
    }

    @Override
    public Result<List<T>> apply(final Input input) {
        final List<T> sequence = new ArrayList<>();
        Input current = input.skipWhiteSpace();

        for (final Parser<T> parser: parsers) {
            switch (parser.apply(current)) {
                case Success<T> success:
                    sequence.add(success.value);
                    current = success.rest.skipWhiteSpace();
                    continue;

                case Failure<T> failure:
                    @SuppressWarnings("unchecked")
                    final Failure<List<T>> result = (Failure<List<T>>) failure;
                    return result;
            }
        }

        return new Success<>(Collections.unmodifiableList(sequence), current);
    }

}
