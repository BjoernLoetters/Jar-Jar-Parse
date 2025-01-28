package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.input.Input;
import jcombinators.result.Result;

import java.util.function.Supplier;

public final class LazyParser<T> implements Parser<T> {

    private final Supplier<Parser<T>> supplier;

    private Parser<T> parser = null;

    public LazyParser(final Supplier<Parser<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Result<T> apply(final Input input) {
        if (parser == null) {
            parser = supplier.get();
        }

        return parser.apply(input);
    }

}
