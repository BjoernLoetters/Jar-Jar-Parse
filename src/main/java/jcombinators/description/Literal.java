package jcombinators.description;

import java.util.Optional;

public final class Literal extends Description {

    public final String literal;

    public Literal(final String literal) {
        this.literal = literal == null ? "" : literal.trim();
    }

    @Override
    public final Optional<String> describe() {
        if (literal.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(String.format("the literal '%s'", literal));
        }
    }

}
