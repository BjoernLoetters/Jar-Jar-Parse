package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

import static jcombinators.common.StringParser.literal;

public final class FlatMapTest extends ParserTest {

    private final Parser<String> wordParser = literal("hello");
    private final Parser<Integer> lengthParser = wordParser.flatMap(word -> literal(word).map(String::length));

    @Test
    public void flatMapSuccessTest() {
        assertSuccess(lengthParser, 5, "hellohello");
    }

    @Test
    public void flatMapFailureOuterTest() {
        assertFailure(lengthParser, "unexpected character '1', expected literal 'hello'", "123");
    }

    @Test
    public void flatMapFailureInnerTest() {
        assertFailure(lengthParser, "unexpected character ' ', expected literal 'hello'", "hello 123");
    }

    @Test
    public void flatMapEmptyInputTest() {
        assertFailure(lengthParser, "unexpected end of input, expected literal 'hello'", "");
    }

}
