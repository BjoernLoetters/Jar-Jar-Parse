package jjparse.description;

import jjparse.Parsing.Parser;

import java.util.Optional;

/**
 * A {@link Description} for {@link Parser}s that negate another {@link Parser}'s expectation.
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see Parser#description
 * @see Parser#not
 */
public final class Negation extends Description {

    /** The {@link Description} which should be negated. */
    public final Description description;

    /**
     * Constructs a new {@link Negation} {@link Description}.
     * @param description The {@link Description} which should be negated.
     */
    public Negation(final Description description) {
        this.description = description;
    }

    @Override
    public Optional<String> describe() {
        return description.describe().map(description -> "anything but " + description);
    }

}
