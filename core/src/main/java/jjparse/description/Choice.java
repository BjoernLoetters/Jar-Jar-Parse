package jjparse.description;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import jjparse.Parsing.Parser;
import jjparse.Parsing;

/**
 * A {@link Description} for {@link Parser}s that expect a {@link Parsing#choice}.
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see Parsing#choice
 * @see Parser#description
 */
public final class Choice extends Description {

    /** The {@link List} of alternative {@link Description}s. */
    public final List<Description> alternatives;

    /**
     * Constructs a new {@link Choice} {@link Description}.
     * @param alternatives The list of alternative {@link Description}s.
     */
    public Choice(final List<Description> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public Optional<String> describe() {
        final List<String> alternatives = this.alternatives.stream()
            .map(Description::describe)
            .filter(Predicate.not(Optional::isEmpty))
            .map(Optional::get)
            .toList();

        if (alternatives.isEmpty()) {
            return Optional.empty();
        } else if (alternatives.size() == 1) {
            return Optional.of(alternatives.getFirst());
        } else {
            final StringBuilder result = new StringBuilder();

            for (int i = 0, size = alternatives.size(); i < size; ++i) {
                if (i > 0) {
                    if (i == size - 1) {
                        result.append(" or ");
                    } else {
                        result.append(", ");
                    }
                }
                result.append(alternatives.get(i));
            }

            return Optional.of(result.toString());
        }
    }

}
