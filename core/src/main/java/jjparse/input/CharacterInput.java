package jjparse.input;

import java.util.NoSuchElementException;

/**
 * A specific implementation of an {@link Input} for {@link Character}s that also implements the {@link CharSequence}
 * interface. {@link Position}s returned by this {@link Input} are aware of unicode code points such that calling
 * {@link Position#toString()} shows the line and column numbers as they are perceived by the user. Instances of this
 * class can be obtained using one of the variants of {@link Input#of}.
 *
 * @see Input
 * @see Input#of
 * @see Character
 * @see CharSequence
 *
 * @author Björn Lötters
 */
public final class CharacterInput extends Input<Character> implements CharSequence {

    /** A cache of line offsets to efficiently compute the line and column numbers of a {@link Position}. */
    private final int[] lines;

    /** The underlying {@link CharSequence} of this {@link CharacterInput}. */
    private final CharSequence sequence;

    /** The offset in characters that denotes the start of the subsequence in the underlying {@link CharSequence}. */
    private final int offset;

    /** The length of the subsequence that is denoted by the offset. */
    private final int length;

    /** The current {@link Position} of this {@link CharacterInput}. */
    private final CodePointPosition position;

    /**
     * Constructs a new {@link CharacterInput}.
     * @param name A human-readable name for this {@link CharacterInput}.
     * @param sequence The underlying {@link CharSequence} for this {@link CharacterInput}.
     * @param offset An offset in characters that denotes the start of a subsequence in the provided {@link CharSequence}.
     * @param length The length of the subsequence that is denoted by the offset.
     * @param lines A cache of line offsets, which is used to compute the line and column numbers in unicode code points
     *              on basis of the character offset.
     */
    CharacterInput(final String name, final CharSequence sequence, final int offset, final int length, final int[] lines) {
        super(name);
        this.sequence = sequence;
        this.offset = offset;
        this.length = length;
        this.lines = lines;
        this.position = new CodePointPosition(offset);
    }

    @Override
    public boolean isEmpty() {
        return length < 1;
    }

    @Override
    public Character head() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return charAt(0);
        }
    }

    @Override
    public Input<Character> tail() {
        return subSequence(1, length);
    }

    @Override
    public CodePointPosition position() {
        return position;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(final int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return sequence.charAt(offset + index);
        }
    }

    @Override
    public CharacterInput subSequence(final int start, final int end) {
        if (start < 0 || start > end) {
            throw new IndexOutOfBoundsException(start);
        } else if (end > length) {
            throw new IndexOutOfBoundsException(end);
        } else {
            return new CharacterInput(name, sequence, offset + start, end - start, lines);
        }
    }

    @Override
    public String toString() {
        return sequence.subSequence(offset, offset + length).toString();
    }

    /**
     * Represents a {@link Position} in this {@link CharacterInput} that is aware of the underlying unicode code points.
     *
     * @see Position
     * @see Character
     *
     * @author Björn Lötters
     */
    public final class CodePointPosition extends Position {

        /** A cached version of this {@link Position}'s line number. */
        private int lineNumber = -1;

        /** A cached version of this {@link Position}'s column number. */
        private int columnNumber = -1;

        /** A cached version of this {@link Position}'s code point. */
        private int codePoint = -1;

        /**
         * Creates a new {@link CodePointPosition} on basis of the provided offset.
         * @param offset The offset of this {@link CodePointPosition}. An offset is the number of characters that must
         *               be skipped in the underlying {@link CharacterInput} in order to reach this {@link CodePointPosition}.
         */
        private CodePointPosition(final int offset) {
            super(offset);
        }

        /**
         * Returns the code point that occurs in the associated {@link CharacterInput} at this {@link CodePointPosition}.
         * @return The unicode code point at this {@link CodePointPosition}.
         */
        public int getCodePoint() {
            if (codePoint == -1) {
                if (Character.isLowSurrogate(sequence.charAt(offset)) && offset > 0 && Character.isHighSurrogate(sequence.charAt(offset - 1))) {
                    // The offset of this position is odd and points in between a surrogate pair (which should not happen
                    // normally). In order to still return a meaningful code point, we jump back over the leading high
                    // surrogate.
                    codePoint = Character.codePointAt(sequence, offset - 1);
                } else {
                    codePoint = Character.codePointAt(sequence, offset);
                }
            }

            return codePoint;
        }

        /**
         * Computes the line number that corresponds to this {@link CodePointPosition} as it is perceived by the user.
         * That is to say, this is not necessarily the character offset in this {@link CodePointPosition} but the
         * unicode code point offset.
         * @return The line number of this {@link CodePointPosition}.
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
         * Computes the column number that corresponds to this {@link CodePointPosition} as it is perceived by the user.
         * That is to say, this is not necessarily the character offset in this {@link CodePointPosition} but the
         * unicode code point offset relative to the corresponding line offset.
         * @return The column number of this {@link CodePointPosition}.
         */
        public int getColumnNumber() {
            if (columnNumber == -1) {
                // We start at 1 here, since column numbers usually do not start at 0.
                columnNumber = 1;

                // Next, we count every code point of the line as a single column.
                int offset = lines[getLineNumber() - 1];
                while (offset < this.offset) {
                    final int codePoint = Character.codePointAt(sequence, offset);
                    offset += Character.charCount(codePoint);
                    columnNumber += 1;
                }

                if (offset > this.offset) {
                    // This position is not correctly aligned with the unicode code points in the underlying source.
                    // That is to say, the offset of this position points to a low surrogate (which should not happen
                    // normally) and the computed column number already points to the next unicode code point. For this
                    // reason, we decrement the column number again, to point to the code point to which the low
                    // surrogate belongs.
                    columnNumber -= 1;
                }
            }

            return columnNumber;
        }

        @Override
        public String describe() {
            if (offset >= CharacterInput.this.sequence.length()) {
                return "end of input";
            } else {
                return String.format("character '%s'", Character.toString(getCodePoint()));
            }
        }

        @Override
        public String toString() {
            return String.format("%s at line %d and column %d", name, getLineNumber(), getColumnNumber());
        }

    }

}
