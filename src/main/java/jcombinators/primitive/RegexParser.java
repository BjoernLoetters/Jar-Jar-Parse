package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.description.Description;
import jcombinators.description.Literal;
import jcombinators.description.Regex;
import jcombinators.input.Input;
import jcombinators.result.Error;
import jcombinators.result.Failure;
import jcombinators.result.Result;
import jcombinators.result.Success;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexParser implements Parser<String> {

    private final Pattern pattern;

    public RegexParser(final Pattern pattern) {
        this.pattern = pattern;
    }

    public RegexParser(final String pattern) {
        this(Pattern.compile(pattern));
    }

    @Override
    public final Description description() {
        return new Regex(pattern);
    }

    @Override
    public final Result<String> apply(final Input input) {
        final Matcher matcher = pattern.matcher(input);
        if (matcher.lookingAt()) {
            final String value = matcher.group();
            return new Success<>(value, input.subSequence(value.length()));
        } else {
            return new Error<>(Failure.format(input, description()), input);
        }
    }

}
