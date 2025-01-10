package jcombinators.description;

import java.util.Optional;

public final class Unknown extends Description {

    @Override
    public Optional<String> describe() {
        return Optional.empty();
    }

}
