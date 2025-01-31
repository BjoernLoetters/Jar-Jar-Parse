package jjparse.description;

import jjparse.Parsing.Parser;
import jjparse.Parsing;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A {@link Description} for {@link Parser}s that expect a {@link Parsing#sequence}.
 *
 * @author Björn Lötters
 *
 * @see Description
 * @see Parsing#sequence
 * @see Parser#description
 */
public final class Sequence extends Description {

    /** The {@link List} of {@link Description}s. */
    public final List<Description> elements;

    /**
     * Constructs a new {@link Sequence} {@link Description}.
     * @param elements The individual {@link Description}s of this {@link Sequence} {@link Description}.
     */
    public Sequence(final List<Description> elements) {
        this.elements = elements;
    }

    @Override
    public Optional<String> describe() {
        final List<String> descriptions = this.elements.stream()
            .map(Description::describe)
            .filter(Predicate.not(Optional::isEmpty))
            .map(Optional::get)
            .toList();

        if (descriptions.isEmpty()) {
            return Optional.empty();
        } else if (descriptions.size() == 1) {
            return Optional.of(descriptions.getFirst());
        } else {
            final StringBuilder result = new StringBuilder();

            for (int i = 0, size = descriptions.size(); i < size; ++i) {
                if (i > 0) {
                    if (i == size - 1) {
                        result.append(" and ");
                    } else {
                        result.append(", ");
                    }
                }
                result.append(descriptions.get(i));
            }

            return Optional.of(result.toString());
        }
    }

}
