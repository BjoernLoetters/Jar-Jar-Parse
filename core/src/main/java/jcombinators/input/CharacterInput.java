package jcombinators.input;

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

    private final int[] lines;

    private final CharSequence sequence;

    private final int offset;

    private final int length;

    CharacterInput(final String name, final CharSequence sequence, final int offset, final int length, final int[] lines) {
        super(name);
        this.sequence = sequence;
        this.offset = offset;
        this.length = length;
        this.lines = lines;
    }

    @Override
    public boolean isEmpty() {
        return offset >= length;
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
        return new CodePointPosition(offset);
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
     * TODO:
     *  1. Add more JavaDoc documentation
     *  2. Implement RegExpParser for arbitrary Input<Character> and not only CharacterInputs
     *  3. Implement a "skip" method that skips a prefix only (filter is probably too progressive)
     * @author Björn Lötters
     */
    public final class CodePointPosition extends Position {

        /**
         * Creates a new {@link CodePointPosition} on basis of the provided offset.
         * @param offset The offset of this {@link CodePointPosition}. An offset is the number of characters that must
         *               be skipped in the underlying {@link CharacterInput} in order to reach this {@link CodePointPosition}.
         */
        public CodePointPosition(final int offset) {
            super(offset);
        }

        public int getCodePoint() {
            return Character.codePointAt(sequence, offset);
        }

        public int getLineNumber() {
            // Here, we do a binary search to find the index of the line number that corresponds to the offset of this position.
            int lower = 0;
            int upper = lines.length - 1;

            while (lower + 1 < upper) {
                final int middle = lower + ((upper - lower) / 2);
                if (offset < lines[middle]) {
                    // The offset of this position is smaller than the offset of the line `middle`, which means that we
                    // must look in the lower half of the `lines` array.
                    upper = middle;
                } else {
                    // The offset of this position is greater than or equal to the offset of the line `middle`, which means
                    // that we must look in the upper half of the `lines` array
                    lower = middle;
                }
            }

            // We add 1 here, since line numbers usually start at 1 and not 0.
            return lower + 1;
        }

        public int getColumnNumber() {
            final int lineOffset = lines[getLineNumber() - 1];
            // We add 1 here, since column numbers usually start at 1 and not 0.
            return offset - lineOffset + 1;
        }

        @Override
        public String describe() {
            return String.format("character '%s'", Character.toString(getCodePoint()));
        }

        @Override
        public String toString() {
            return String.format("position %d:%d", getLineNumber(), getColumnNumber());
        }

    }

}
