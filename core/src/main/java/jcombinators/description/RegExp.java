package jcombinators.description;

import java.util.Optional;
import java.util.regex.Pattern;

public final class RegExp extends Description {

    public final Pattern pattern;

    public RegExp(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public Optional<String> describe() {
        return Optional.of(String.format("an input that matches '%s'", pattern.pattern()));
    }

}
