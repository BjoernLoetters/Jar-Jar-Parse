package jcombinators.primitive;

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
        assertFailure(parser, "unexpected character 'w', expected the literal 'hello'", "world");
    }

    @Test
    public void literalFailureEndOfInputTest() {
        assertFailure(parser, "unexpected end of input, expected the literal 'hello'", "");
    }

    @Test
    public void literalPartialMatchFailureTest() {
        assertFailure(parser, "unexpected character ' ', expected the literal 'hello'", "hel world");
    }

    @Test
    public void literalLongerInputTest() {
        assertSuccess(parser, "hello", "hello world");
    }

    @Test
    public void literalCaseSensitiveTest() {
        assertFailure(parser, "unexpected character 'H', expected the literal 'hello'", "Hello");
    }

    @Test
    public void literalUnicodeTest() {
        final String unicodeLiteral = "😀";
        final Parser<String> unicodeParser = new LiteralParser(unicodeLiteral);

        assertSuccess(unicodeParser, "😀", "😀");
        assertFailure(unicodeParser, "unexpected character '🙂', expected the literal '😀'", "🙂");
        assertFailure(unicodeParser, "unexpected end of input, expected the literal '😀'", "");
    }

}
