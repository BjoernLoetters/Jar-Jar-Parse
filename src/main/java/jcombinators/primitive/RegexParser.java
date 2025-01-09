package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.result.Error;
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
    public final Result<String> apply(final String input, final int offset) {
        final Matcher matcher = pattern.matcher(input);
        if (matcher.region(offset, input.length()).lookingAt()) {
            final String value = matcher.group();
            return new Success<>(value, offset + value.length());
        } else if (offset >= input.length()) {
            return new Error<>("unexpected end of input, expected regular expression '" + pattern.pattern() + "'", offset);
        } else {
            return new Error<>("unexpected character '" + input.charAt(offset) + "', expected regular expression '" + pattern.pattern() + "'", offset);
        }
    }

}
