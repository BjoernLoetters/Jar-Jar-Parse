package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

import java.util.List;

import static jcombinators.Parser.sequence;
import static jcombinators.common.StringParser.character;

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
        assertFailure(parser, "unexpected character 'x', expected the literal 'c'", "abx");
    }

    @Test
    public void sequenceEmptyInputTest() {
        assertFailure(parser, "unexpected end of input, expected the literal 'a'", "");
    }

    @Test
    public void emptySequenceTest() {
        Parser<List<Character>> emptySequenceParser = sequence();
        assertSuccess(emptySequenceParser, List.of(), "abc");
        assertSuccess(emptySequenceParser, List.of(), "");
    }

}