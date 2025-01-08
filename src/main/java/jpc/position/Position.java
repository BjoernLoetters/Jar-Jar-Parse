package jpc.position;

public sealed abstract class Position permits Specific, Unknown {

    public final int line;

    public final int column;

    public Position(final int line, final int column) {
        this.line = line;
        this.column = column;
    }

}
