package jpc.common;

import jpc.Parser;
import jpc.ParserTest;
import org.junit.Test;

import static jpc.common.StringParser.*;

public final class StringTest extends ParserTest {

    @Test
    public void regexEmptyInputTest() {
        Parser<String> emptyParser = regex("");
        assertSuccess(emptyParser, "", "");
        assertSuccess(emptyParser, "", "hello world");
    }

    @Test
    public void regexPrefixMatchTest() {
        Parser<String> wordParser = regex("abc");
        assertSuccess(wordParser, "abc", "abc_hello_world");
        assertFailure(wordParser, "unexpected character 'd', expected regular expression 'abc'", "dabc");
        assertFailure(wordParser, "unexpected character 'a', expected regular expression 'abc'", "aabc");
    }

    @Test
    public void regexWhitespaceTest() {
        Parser<String> spaceParser = regex("a b c");
        assertSuccess(spaceParser, "a b c", "a b c");
    }

    @Test
    public void regexEscapeSequenceTest() {
        Parser<String> escapeParser = regex("\\d+");
        assertSuccess(escapeParser, "123", "123");
    }

    @Test
    public void regexExactMatchTest() {
        Parser<String> endParser = regex("abc");
        assertSuccess(endParser, "abc", "abc");
    }

}
