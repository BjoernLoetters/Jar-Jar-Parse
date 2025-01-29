package jcombinators.position;

import jcombinators.input.Input;

public final class Specific extends Position {

    public Specific(final Input<?> input, final int line, final int column) {
        super(input, line, column);
    }

    @Override
    public String toString() {
        return String.format("%s at line %d and column %d", input.name, line, column);
    }

}
