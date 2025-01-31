package jjparse.primitive;

import jjparse.ParserTest;
import org.junit.Test;

import java.util.List;

public final class RepeatTest extends ParserTest {

    private final Parser<Character> charParser = regex("[a-z]").map(s -> s.charAt(0));
    private final Parser<List<Character>> repeatParser = charParser.repeat();

    @Test
    public void repeatSuccessTest() {
        assertSuccess(repeatParser, List.of('a', 'b', 'c'), "abc");
    }

    @Test
    public void repeatSuccessPartialMatchTest() {
        assertSuccess(repeatParser, List.of('a', 'b'), "ab1");
    }

    @Test
    public void repeatSuccessEmptyMatchTest() {
        assertSuccess(repeatParser, List.of(), "123");
    }

    @Test
    public void repeatEmptyInputTest() {
        assertSuccess(repeatParser, List.of(), "");
    }

    @Test
    public void repeatNoMatchTest() {
        final Parser<List<String>> noMatchParser = regex("[0-9]").repeat();
        assertSuccess(noMatchParser, List.of(), "abc");
    }

}
