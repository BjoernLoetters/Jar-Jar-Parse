package jjparse.description;

import java.util.Optional;

public final class Empty extends Description {

    @Override
    public Optional<String> describe() {
        return Optional.empty();
    }

}
