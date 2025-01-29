package jcombinators.input;

import jcombinators.position.Position;
import jcombinators.position.Specific;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CharInput extends Input<Character> implements CharSequence {

    private final char[] underlying;

    private final int offset;

    private final int length;

    private final int line;

    private final int column;

    private final boolean wasHighSurrogate;

    private final Pattern whiteSpacePattern;

    CharInput(final String name, final char[] underlying, final int offset, final int length, final int line, final int column, final boolean wasHighSurrogate, final Pattern whiteSpacePattern) {
        super(name);
        this.underlying = underlying;
        this.offset = offset;
        this.length = length;
        this.line = line;
        this.column = column;
        this.wasHighSurrogate = wasHighSurrogate;
        this.whiteSpacePattern = whiteSpacePattern;
    }

    @Override
    public String describe() {
        if (isEmpty()) {
            return "end of input";
        } else {
            final int codePoint;

            if (wasHighSurrogate) {
                codePoint = Character.codePointAt(underlying, offset - 1);
            } else {
                codePoint = Character.codePointAt(underlying, offset);
            }

            return String.format("character '%s'", Character.toString(codePoint));
        }
    }

    @Override
    public Position position() {
        return new Specific(this, line, column);
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    public CharInput skip() {
        final Matcher matcher = whiteSpacePattern.matcher(this);
        if (matcher.lookingAt()) {
            final int skip = matcher.group().length();
            return subSequence(skip, length());
        } else {
            return this;
        }
    }

    @Override
    public Character head() {
        return underlying[offset];
    }

    @Override
    public CharInput tail() {
        return subSequence(1, length());
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(final int index) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return underlying[offset + index];
        }
    }

    @Override
    public CharInput subSequence(final int start, final int end) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException(start);
        } else if (end < start || end > length()) {
            throw new IndexOutOfBoundsException(end);
        } else {
            int line = this.line;
            int column = this.column;
            boolean wasHighSurrogate = false;

            int i = offset;

            if (this.wasHighSurrogate && start > 1) {
                // The previous character in this input was a high surrogate. The current head() is the corresponding
                // low surrogate. That is, at this point we can finally advance the column counter by one.
                column += 1;
                i += 1;
            }

            while (i < offset + start) {
                final int codePoint = Character.codePointAt(underlying, i);
                final int codePointLength = Character.charCount(codePoint);

                if (codePoint == '\n') {
                    line += 1;
                    column = 1;
                } else if (i + codePointLength <= offset + start) {
                    // This code point is completely contained within the subsequence, so we can safely advance the
                    // column counter.
                    column += 1;
                }

                i += codePointLength;
            }

            if (i > offset + start) {
                // The last code point was not complete (otherwise 'i' would be exactly 'offset + start'), so we
                // remember that we have seen a high surrogate.
                wasHighSurrogate = true;
            }

            return new CharInput(name, underlying, offset + start, end - start, line, column, wasHighSurrogate, whiteSpacePattern);
        }
    }

    public String subString(final int start, final int end) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException(start);
        } else if (end < start || end > length()) {
            throw new IndexOutOfBoundsException(end);
        } else {
            return String.copyValueOf(underlying, offset + start, end - start);
        }
    }

    @Override
    public String toString() {
        return subString(0, length());
    }

}
