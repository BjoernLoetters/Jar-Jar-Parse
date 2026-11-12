package jjparse.primitive;

import jjparse.ParserTest;
import org.junit.Test;

import java.util.List;

public class SequenceTest extends ParserTest {

    private final Parser<List<Character>> parser = sequence(
        character('a'),
        character('b'),
        character('c')
    );

    @Test
    public void sequenceSuccessTest() {
        assertSuccess(parser, List.of('a', 'b', 'c'), "abc");
    }

    @Test
    public void sequencePartialFailureTest() {
        assertFailure(parser, "syntax error in Test 'sequencePartialFailureTest' at line 1 and column 3: unexpected token 'x', expected the token 'c'", "abx");
    }

    @Test
    public void sequenceEmptyInputTest() {
        assertFailure(parser, "syntax error in Test 'sequenceEmptyInputTest' at line 1 and column 1: unexpected end of input, expected the token 'a'", "");
    }

    @Test
    public void emptySequenceTest() {
        Parser<List<Character>> emptySequenceParser = sequence();
        assertSuccess(emptySequenceParser, List.of(), "abc");
        assertSuccess(emptySequenceParser, List.of(), "");
    }

}