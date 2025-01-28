package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.ParserTest;
import jcombinators.result.Abort;
import org.junit.Test;

import static jcombinators.Parser.abort;
import static jcombinators.Parser.or;
import static jcombinators.common.StringParser.regex;

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
        assertFailure(parser, "syntax error in Test 'failChoiceTest' at line 1 and character 1: unexpected character '@', expected an input that matches '([0-9])|([a-z])'", "@");
    }

    @Test
    public void failChoiceWithoutBacktrackingTest() {
        final Parser<Integer> first = abort("fatal error");
        final ChoiceParser<Integer> parser = new ChoiceParser<>(first, second);

        assertFailure(parser, "fatal error", "a");
    }

    @Test
    public void emptyChoiceTest() {
        assertFailure(parser, "syntax error in Test 'emptyChoiceTest' at line 1 and character 1: unexpected end of input, expected an input that matches '([0-9])|([a-z])'", "");
    }

}