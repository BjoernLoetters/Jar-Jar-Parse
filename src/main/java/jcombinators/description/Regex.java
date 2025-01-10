package jcombinators.description;

import java.util.Optional;
import java.util.regex.Pattern;

public final class Regex extends Description {

    public final Pattern pattern;

    public Regex(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public final Optional<String> describe() {
        return Optional.of(String.format("an input that matches '%s'", pattern.pattern()));
    }

}
