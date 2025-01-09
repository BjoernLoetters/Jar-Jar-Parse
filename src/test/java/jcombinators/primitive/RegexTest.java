package jcombinators.primitive;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

public class RegexTest extends ParserTest {

    @Test
    public void regexExactMatchTest() {
        final Parser<String> parser = new RegexParser("[a-z]+");
        assertSuccess(parser, "hello", "hello");
    }

    @Test
    public void regexPartialMatchTest() {
        final Parser<String> parser = new RegexParser("[a-z]+");
        assertSuccess(parser, "abc", "abc123");
    }

    @Test
    public void regexNoMatchTest() {
        final Parser<String> parser = new RegexParser("[a-z]+");
        assertFailure(parser, "unexpected character '1', expected regular expression '[a-z]+'", "123");
    }

    @Test
    public void regexEmptyInputTest() {
        final Parser<String> parser = new RegexParser("[a-z]+");
        assertFailure(parser, "unexpected end of input, expected regular expression '[a-z]+'", "");
    }

    @Test
    public void regexDigitsTest() {
        final Parser<String> parser = new RegexParser("\\d+");
        assertSuccess(parser, "12345", "12345abc");
    }

    @Test
    public void regexUnicodeTest() {
        final Parser<String> parser = new RegexParser("\\p{L}\\p{L}");
        assertSuccess(parser, "你好", "你好世界");
    }

    @Test
    public void regexSpecialCharactersTest() {
        final Parser<String> parser = new RegexParser("[!@#]+");
        assertSuccess(parser, "!@#", "!@#hello");
    }

}