package jjparse.description;

import jjparse.Parsing.Parser;
import jjparse.StringParsing;

import java.util.Optional;

/**
 * A {@link Description} for {@link Parser}s that expect a {@link String} literal.
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see StringParsing#literal
 * @see Parser#description
 */
public final class Literal extends Description {

    /** The expected {@link String} literal. */
    public final String literal;

    /**
     * Construct a new {@link Literal} {@link Description}.
     * @param literal The expected {@link String} literal.
     */
    public Literal(final String literal) {
        this.literal = literal == null ? "" : literal.trim();
    }

    @Override
    public Optional<String> describe() {
        if (literal.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(String.format("the literal '%s'", literal));
        }
    }

}
