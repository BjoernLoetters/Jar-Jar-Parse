package jcombinators.input;

import jcombinators.position.Position;
import jcombinators.position.Specific;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Input {

    public final String name;

    public final Position position;

    final String contents;

    final int offset;

    private Input(final String name, final String contents, final int offset, final int line, final int column) {
        this.name = name;
        this.position = new Specific(this, line, column);
        this.offset = offset;
        this.contents = contents;
    }

    public int getCodePoint() {
        if (Character.isHighSurrogate(contents.charAt(offset)) && offset + 1 < contents.length() && Character.isLowSurrogate(contents.charAt(offset + 1))) {
            return Character.toCodePoint(contents.charAt(offset), contents.charAt(offset + 1));
        } else {
            return contents.charAt(offset);
        }
    }

    public Input next() {
        if (isEmpty()) {
            return this;
        } else {
            final int line;
            final int column;

            if (getCodePoint() == '\n') {
                line = position.line + 1;
                column = 1;
            } else {
                line = position.line;
                column = position.column + 1;
            }

            return new Input(name, contents, offset + Character.charCount(this.getCodePoint()), line, column);
        }
    }

    public Input drop(final int number) {
        Input result = this;
        for (int i = 0; i < number && !result.isEmpty(); ++i) {
            result = result.next();
        }
        return result;
    }

    public boolean isEmpty() {
        return offset >= contents.length();
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
        return new Input(name, contents, 0, 1, 1);
    }

}
