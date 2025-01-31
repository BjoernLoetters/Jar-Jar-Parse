package jjparse.description;

import java.util.Optional;

public final class Negation extends Description {

    public final Description description;

    public Negation(final Description description) {
        this.description = description;
    }

    @Override
    public Optional<String> describe() {
        return description.describe().map(description -> "anything but " + description);
    }

}
