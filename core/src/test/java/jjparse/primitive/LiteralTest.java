package jjparse.primitive;

import jjparse.ParserTest;
import org.junit.Test;

public final class LiteralTest extends ParserTest {

    private final Parser<String> parser = literal("hello");

    @Test
    public void literalSuccessTest() {
        assertSuccess(parser, "hello", "hello");
    }

    @Test
    public void literalFailureUnexpectedCharacterTest() {
        assertFailure(parser, "syntax error in Test 'literalFailureUnexpectedCharacterTest' at line 1 and column 1: unexpected token 'w', expected the literal 'hello'", "world");
    }

    @Test
    public void literalFailureEndOfInputTest() {
        assertFailure(parser, "syntax error in Test 'literalFailureEndOfInputTest' at line 1 and column 1: unexpected end of input, expected the literal 'hello'", "");
    }

    @Test
    public void literalPartialMatchFailureTest() {
        assertFailure(parser, "syntax error in Test 'literalPartialMatchFailureTest' at line 1 and column 4: unexpected token ' ', expected the literal 'hello'", "hel world");
    }

    @Test
    public void literalLongerInputTest() {
        assertSuccess(parser, "hello", "hello world");
    }

    @Test
    public void literalCaseSensitiveTest() {
        assertFailure(parser, "syntax error in Test 'literalCaseSensitiveTest' at line 1 and column 1: unexpected token 'H', expected the literal 'hello'", "Hello");
    }

    @Test
    public void literalUnicodeTest() {
        final String unicodeLiteral = "😀";
        final Parser<String> unicodeParser = literal(unicodeLiteral);

        assertSuccess(unicodeParser, "😀", "😀");
        assertFailure(unicodeParser, "syntax error in Test 'literalUnicodeTest' at line 1 and column 1: unexpected token '🙂', expected the literal '😀'", "🙂");
        assertFailure(unicodeParser, "syntax error in Test 'literalUnicodeTest' at line 1 and column 1: unexpected end of input, expected the literal '😀'", "");
    }

}
