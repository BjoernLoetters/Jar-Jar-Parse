package jjparse.description;

import java.util.Optional;

public final class CharacterRange extends Description {

    public final char min;
    public final char max;

    public CharacterRange(final char min, final char max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Optional<String> describe() {
        return Optional.of(String.format("a character that matches '[%c-%c]'", min, max));
    }

}
