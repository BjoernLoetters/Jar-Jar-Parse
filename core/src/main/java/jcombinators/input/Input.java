package jcombinators.input;

import jcombinators.position.Position;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * An abstract representation of an input.
 * @param <T>
 */
public abstract class Input<T> {

    /** A human-readable name for this input which can be used for error reporting purposes. */
    public final String name;

    /**
     * Constructs a new input on basis of the provided name.
     * @param name
     */
    public Input(final String name) {
        this.name = name;
    }

    public abstract Position position();

    public abstract boolean isEmpty();

    public abstract Input<T> skip();

    public abstract T head();

    public abstract String describe();

    public abstract Input<T> tail();

    public static Input<Character> of(final String name, final java.io.InputStream stream, final Charset charset) throws IOException {
        final byte[] bytes = stream.readAllBytes();
        return of(name, bytes, charset);
    }

    public static Input<Character> of(final Path path, final Charset charset) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        return of(path.toString(), bytes, charset);
    }

    public static Input<Character> of(final String name, final byte[] bytes, final Charset charset) {
        final String contents = new String(bytes, charset);
        return of(name, contents);
    }

    public static Input<Character> of(final String name, final String string) {
        final char[] underlying = string.toCharArray();
        return new CharInput(name, underlying, 0, underlying.length, 1, 1, false, Pattern.compile("\\s+"));
    }

}
