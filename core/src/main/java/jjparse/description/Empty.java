package jjparse.description;

import jjparse.Parsing.Parser;

import java.util.Optional;

/**
 * A {@link Description} for {@link Parser}s with no expectation hints (which is the default).
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see Parser#description
 */
public final class Empty extends Description {

    @Override
    public Optional<String> describe() {
        return Optional.empty();
    }

}
