package jcombinators.common;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

import static jcombinators.common.StringParser.*;

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
        assertFailure(wordParser, "unexpected character 'd', expected an input that matches 'abc'", "dabc");
        assertFailure(wordParser, "unexpected character 'a', expected an input that matches 'abc'", "aabc");
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

    @Test
    public void lowercaseSuccessTest() {
        assertSuccess(lowercase, 'a', "abc");
    }

    @Test
    public void uppercaseSuccessTest() {
        assertSuccess(uppercase, 'A', "ABC");
    }

    @Test
    public void lowercaseFailureTest() {
        assertFailure(lowercase, "unexpected character 'A', expected an input that matches '[a-z]'", "ABC");
    }

    @Test
    public void uppercaseFailureTest() {
        assertFailure(uppercase, "unexpected character 'a', expected an input that matches '[A-Z]'", "abc");
    }

    @Test
    public void characterSuccessTest() {
        Parser<Character> parser = character('x');
        assertSuccess(parser, 'x', "xyz");
    }

    @Test
    public void characterFailureTest() {
        Parser<Character> parser = character('x');
        assertFailure(parser, "unexpected character 'y', expected the literal 'x'", "yza");
    }

}
