package jjparse.description;

import java.util.Optional;

public final class CharacterClass extends Description {

    private final char[] characters;

    public CharacterClass(final char[] characters) {
        this.characters = characters;
    }

    @Override
    public Optional<String> describe() {
        final StringBuilder builder = new StringBuilder(characters.length + 16);
        for (int i = 0; i < characters.length; ++i) {
            if (i > 0) {
                if (i + 1 == characters.length) {
                    builder.append(" or ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append("the character '");
            builder.append(characters[i]);
            builder.append("'");
        }

        return Optional.of(builder.toString());
    }

}
