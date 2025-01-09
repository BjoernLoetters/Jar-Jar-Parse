package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RepeatParser<T> implements Parser<List<T>> {

    private final Parser<T> parser;

    public RepeatParser(final Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public final Result<List<T>> apply(final String input, final int offset) {
        final List<T> values = new ArrayList<>();
        int current = offset;
        boolean stop = false;

        do {
            switch (parser.apply(input, current)) {
                case Success<T> success:
                    values.add(success.value);
                    current = success.offset;
                    break;
                case Failure<T> ignore:
                    stop = true;
                    break;
            }
        } while (!stop);

        return new Success<>(Collections.unmodifiableList(values), current);
    }

}
