package jjparse.description;

import jjparse.Parsing.Parser;
import jjparse.StringParsing;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A {@link Description} for {@link Parser}s that expect a {@link Pattern}.
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see StringParsing#regex
 * @see Parser#description
 * @see Pattern
 */
public final class RegExp extends Description {

    /** The expected {@link Pattern} of this {@link Description}. */
    public final Pattern pattern;

    /**
     * Constructs a new {@link RegExp} {@link Description}.
     * @param pattern The expected {@link Pattern} of this {@link Description}.
     */
    public RegExp(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public Optional<String> describe() {
        return Optional.of(String.format("an input that matches '%s'", pattern.pattern()));
    }

}
