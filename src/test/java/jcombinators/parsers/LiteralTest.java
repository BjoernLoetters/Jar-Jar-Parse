package jcombinators.parsers;

import jcombinators.Parser;
import jcombinators.ParserTest;
import org.junit.Test;

public final class LiteralTest extends ParserTest {

    private final Parser<String> parser = new LiteralParser("hello");

    @Test
    public void literalSuccessTest() {
        assertSuccess(parser, "hello", "hello");
    }

    @Test
    public void literalFailureUnexpectedCharacterTest() {
        assertFailure(parser, "unexpected character 'w', expected literal 'hello'", "world");
    }

    @Test
    public void literalFailureEndOfInputTest() {
        assertFailure(parser, "unexpected end of input, expected literal 'hello'", "");
    }

    @Test
    public void literalPartialMatchFailureTest() {
        assertFailure(parser, "unexpected character ' ', expected literal 'hello'", "hel world");
    }

    @Test
    public void literalLongerInputTest() {
        assertSuccess(parser, "hello", "hello world");
    }

    @Test
    public void literalCaseSensitiveTest() {
        assertFailure(parser, "unexpected character 'H', expected literal 'hello'", "Hello");
    }

    @Test
    public void literalUnicodeTest() {
        final String unicodeLiteral = "😀";
        final Parser<String> unicodeParser = new LiteralParser(unicodeLiteral);

        assertSuccess(unicodeParser, "😀", "😀");
        assertFailure(unicodeParser, "unexpected character '🙂', expected literal '😀'", "🙂");
        assertFailure(unicodeParser, "unexpected end of input, expected literal '😀'", "");
    }

}
