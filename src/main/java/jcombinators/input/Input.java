package jcombinators.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class Input implements CharSequence {

    public final String name;

    private final String contents;

    private final int offset;

    private final int length;

    private Input(final String name, final String contents, final int offset, final int length) {
        this.name = name;
        this.contents = contents;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public final int length() {
        return length;
    }

    @Override
    public final char charAt(final int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return contents.charAt(offset + index);
        }
    }

    public final int codePointAt(final int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return Character.codePointAt(this, index);
        }
    }

    @Override
    public final Input subSequence(final int start, final int end) {
        if (end < 0 || end > length) {
            throw new IndexOutOfBoundsException(end);
        } else if (start < 0 || start > end) {
            throw new IndexOutOfBoundsException(start);
        } else {
            return new Input(name, contents, offset + start, end - start);
        }
    }

    public final Input subSequence(final int start) {
        return subSequence(start, length);
    }

    @Override
    public final String toString() {
        return contents.substring(offset, offset + length);
    }

    public static Input of(final String name, final InputStream stream, final Charset charset) throws IOException {
        final byte[] bytes = stream.readAllBytes();
        return of(name, bytes, charset);
    }

    public static Input of(final Path path, final Charset charset) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        return of(path.toString(), bytes, charset);
    }

    public static Input of(final String name, final byte[] bytes, final Charset charset) {
        final String contents = new String(bytes, charset);
        return of(name, contents);
    }

    public static Input of(final String name, final CharSequence sequence) {
        final String contents = sequence.toString();
        return new Input(name, contents, 0, contents.length());
    }

}
