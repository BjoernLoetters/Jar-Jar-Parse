package jjparse.primitive;

import jjparse.ParserTest;
import org.junit.Test;

public final class ChoiceTest extends ParserTest {

    private final Parser<Integer> first = regex("[0-9]").map(Integer::parseInt);
    private final Parser<Integer> second = regex("[a-z]").map(s -> (int) s.charAt(0));
    private final Parser<Integer> parser = choice(first, second);

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
        assertFailure(parser, "syntax error in Test 'failChoiceTest' at line 1 and column 1: unexpected character '@', expected an input that matches '[0-9]' or an input that matches '[a-z]'", "@");
    }

    @Test
    public void failChoiceWithoutBacktrackingTest() {
        final Parser<Integer> first = abort("fatal error");
        final Parser<Integer> parser = choice(first, second);

        assertFailure(parser, "fatal error", "a");
    }

    @Test
    public void emptyChoiceTest() {
        assertFailure(parser, "syntax error in Test 'emptyChoiceTest' at line 1 and column 1: unexpected end of input, expected an input that matches '[0-9]' or an input that matches '[a-z]'", "");
    }

    @Test
    public void abortFirstChoiceTest() {
        assertFailure(choice(first.commit(), second), "syntax error in Test 'abortFirstChoiceTest' at line 1 and column 1: unexpected character 'a', expected an input that matches '[0-9]'", "a");
    }

    @Test
    public void abortSecondChoiceTest() {
        assertFailure(choice(first, second.commit()), "syntax error in Test 'abortSecondChoiceTest' at line 1 and column 1: unexpected character '@', expected an input that matches '[a-z]'", "@");
    }

}