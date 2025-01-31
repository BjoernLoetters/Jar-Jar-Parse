package jjparse.description;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Sequence extends Description {

    public final List<Description> elements;

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
