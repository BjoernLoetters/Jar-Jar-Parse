package jpc.parsers;

import jpc.Parser;
import jpc.ParserTest;
import jpc.result.Abort;
import org.junit.Test;

import static jpc.Parser.or;
import static jpc.common.StringParser.regex;

public final class ChoiceTest extends ParserTest {

    private final Parser<Integer> first = regex("[0-9]").map(Integer::parseInt);
    private final Parser<Integer> second = regex("[a-z]").map(s -> (int) s.charAt(0));
    private final Parser<Integer> parser = or(first, second);

    @Test
    public void firstChoiceTest() {
        assertSuccess(parser, 1, "1");
    }

    @Test
    public void secondChoiceTest() {
        assertSuccess(parser, (int) 'a', "a");
    }

    @Test
    public void failChoiceTest() {
        assertFailure(parser, "unexpected character '@', expected regular expression '[a-z]'", "@");
    }

    @Test
    public void failChoiceWithoutBacktrackingTest() {
        final Parser<Integer> first = (input, offset) -> new Abort<>("fatal error", 0);
        final ChoiceParser<Integer> parser = new ChoiceParser<>(first, second);

        assertFailure(parser, "fatal error", "a");
    }

    @Test
    public void emptyChoiceTest() {
        assertFailure(parser, "unexpected end of input, expected regular expression '[a-z]'", "");
    }

}