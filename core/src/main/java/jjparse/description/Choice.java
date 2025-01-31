package jjparse.description;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Choice extends Description {

    public final List<Description> alternatives;
    
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
