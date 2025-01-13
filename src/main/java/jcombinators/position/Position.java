package jcombinators.position;

import jcombinators.input.Input;

public sealed abstract class Position permits Specific, Unknown {

    public final Input input;

    public final int line;

    public final int column;

    public Position(final Input input, final int line, final int column) {
        this.input = input;
        this.line = line;
        this.column = column;
    }

}
