package jjparse.input;

import java.util.NoSuchElementException;

/**
 * A specific implementation of an {@link Input} for Unicode codepoints.
 * {@link Position}s returned by this {@link Input} are aware of Unicode codepoints such that calling
 * {@link Position#toString()} shows the line and column numbers as they correspond to the codepoints.
 * Instances of this class can be obtained using one of the variants of {@link Input#of}.
 *
 * @see Input
 * @see Input#of
 * @see Character
 *
 * @author Björn Lötters
 */
public final class CodepointInput extends Input<Integer> {

    /** A cache of line offsets to efficiently compute the line and column numbers of a {@link Position}. */
    private final int[] lines;

    /** The underlying sequence of Unicode codepoints of this {@link CodepointInput}. */
    private final int[] codepoints;

    /** The offset in codepoints that denotes the start of the subsequence in the underlying sequence. */
    private final int offset;

    /** The length of the subsequence that is denoted by the offset. */
    private final int length;

    /** The current {@link Position} of this {@link CodepointInput}. */
    private final CodepointPosition position;

    /**
     * Constructs a new {@link CodepointInput}.
     * @param name A human-readable name for this {@link CodepointInput}.
     * @param codepoints The underlying sequence of Unicode codepoints for this {@link CodepointInput}.
     * @param offset An offset in codepoints that denotes the start of a subsequence in the provided sequence.
     * @param length The length of the subsequence that is denoted by the offset.
     * @param lines A cache of line offsets, which is used to compute the line and column numbers on basis of an offset.
     */
    CodepointInput(final String name, final int[] codepoints, final int offset, final int length, final int[] lines) {
        super(name);
        this.codepoints = codepoints;
        this.lines = lines;
        this.offset = offset;
        this.length = length;
        this.position = new CodepointPosition(offset);
    }

    @Override
    public boolean isEmpty() {
        return length < 1;
    }

    @Override
    public Integer head() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return codepoints[offset];
        }
    }

    @Override
    public Input<Integer> tail() {
        if (isEmpty()) {
            return this;
        } else {
            return new CodepointInput(name, codepoints, offset + 1, length - 1, lines);
        }
    }

    @Override
    public CodepointPosition position() {
        return position;
    }

    @Override
    public String toString() {
        return new String(codepoints, offset, length);
    }

    /**
     * Represents a {@link Position} in this {@link CodepointInput} that is aware of line and column numbers.
     *
     * @see Position
     * @see Character
     *
     * @author Björn Lötters
     */
    public final class CodepointPosition extends Position {

        /** A cached version of this {@link Position}'s line number. */
        private int lineNumber = -1;

        /** A cached version of this {@link Position}'s column number. */
        private int columnNumber = -1;

        /**
         * Creates a new {@link CodepointPosition} on basis of the provided offset.
         * @param offset The offset of this {@link CodepointPosition}. An offset is the number of codepoints that must
         *               be skipped in the underlying {@link CodepointInput} in order to reach this {@link CodepointPosition}.
         */
        private CodepointPosition(final int offset) {
            super(offset);
        }

        /**
         * Returns the codepoint that occurs in the associated {@link CodepointInput} at this {@link CodepointPosition}.
         * @return The Unicode codepoint at this {@link CodepointPosition}.
         */
        public int getCodePoint() {
            return codepoints[offset];
        }

        /**
         * Computes the line number that corresponds to this {@link CodepointPosition}.
         * @return The line number of this {@link CodepointPosition}.
         */
        public int getLineNumber() {
            if (lineNumber == -1) {
                // Here, we do a binary search to find the index of the line number that corresponds to the offset of this position.
                int lower = 0;
                int upper = lines.length;

                while (lower < upper) {
                    final int middle = lower + (upper - lower) / 2;
                    if (offset < lines[middle]) {
                        // The offset of this position is smaller than the offset of the line in the center of the
                        // search space, so we have to search in the lower half.
                        upper = middle;
                    } else if (middle + 1 < upper && offset >= lines[middle + 1]) {
                        // The offset of this position is greater than or equal to the offset of the next line after the
                        // line in the center of the search space, so we have to search in the upper half.
                        lower = middle + 1;
                    } else {
                        // The offset of this position is exactly within the line at the center of the search space, so
                        // we break out of this loop here.
                        lower = middle;
                        upper = middle;
                    }
                }

                // We add 1 here, since `lower` is the line index and not the line number.
                lineNumber = lower + 1;
            }

            return lineNumber;
        }

        /**
         * Computes the column number that corresponds to this {@link CodepointPosition}.
         * @return The column number of this {@link CodepointPosition}.
         */
        public int getColumnNumber() {
            if (columnNumber == -1) {
                columnNumber = this.offset - lines[getLineNumber() - 1] + 1;
            }

            return columnNumber;
        }

        @Override
        public String describe() {
            if (offset >= CodepointInput.this.codepoints.length) {
                return "end of input";
            } else {
                return String.format("token '%s'", Character.toString(getCodePoint()));
            }
        }

        @Override
        public String toString() {
            return String.format("%s at line %d and column %d", name, getLineNumber(), getColumnNumber());
        }

    }

}
