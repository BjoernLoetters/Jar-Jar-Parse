package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

import static jcombinators.common.StringParser.regex;

public final class MapTest extends ParserTest {

    private final Parser<String> stringParser = regex("[a-z]+");
    private final Parser<Integer> lengthParser = stringParser.map(String::length);

    @Test
    public void mapSuccessTest() {
        assertSuccess(lengthParser, 3, "abc");
    }

    @Test
    public void mapFailureTest() {
        assertFailure(lengthParser, "syntax error in Test 'mapFailureTest' at line 1 and character 1: unexpected character '1', expected an input that matches '[a-z]+'", "123");
    }

    @Test
    public void mapEmptyInputTest() {
        assertFailure(lengthParser, "syntax error in Test 'mapEmptyInputTest' at line 1 and character 1: unexpected end of input, expected an input that matches '[a-z]+'", "");
    }

    @Test
    public void mapUppercaseTest() {
        final Parser<String> upperCaseParser = stringParser.map(String::toUpperCase);
        assertSuccess(upperCaseParser, "ABC", "abc");
    }

    @Test
    public void mapPartialMatchTest() {
        assertSuccess(lengthParser, 2, "ab1");
    }

    @Test
    public void mapChainedParserTest() {
        final Parser<Integer> doubleLengthParser = lengthParser.map(length -> length * 2);
        assertSuccess(doubleLengthParser, 6, "abc");
    }

}
