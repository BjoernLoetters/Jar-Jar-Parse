package jjparse;

import org.junit.Test;

import java.math.BigInteger;

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
        assertFailure(wordParser, "syntax error in Test 'regexPrefixMatchTest' at line 1 and column 1: unexpected token 'd', expected anything that matches 'abc'", "dabc");
        assertFailure(wordParser, "syntax error in Test 'regexPrefixMatchTest' at line 1 and column 1: unexpected token 'a', expected anything that matches 'abc'", "aabc");
    }

    @Test
    public void regexWhitespaceTest() {
        Parser<String> spaceParser = regex("a b c");
        assertSuccess(spaceParser, "a b c", "a b c");
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
        assertFailure(lowercase, "syntax error in Test 'lowercaseFailureTest' at line 1 and column 1: unexpected token 'A', expected a Latin lowercase letter", "ABC");
    }

    @Test
    public void uppercaseFailureTest() {
        assertFailure(uppercase, "syntax error in Test 'uppercaseFailureTest' at line 1 and column 1: unexpected token 'a', expected a Latin uppercase letter", "abc");
    }

    @Test
    public void characterSuccessTest() {
        Parser<Character> parser = character('x');
        assertSuccess(parser, 'x', "xyz");
    }

    @Test
    public void characterFailureTest() {
        Parser<Character> parser = character('x');
        assertFailure(parser, "syntax error in Test 'characterFailureTest' at line 1 and column 1: unexpected token 'y', expected the token 'x'", "yza");
    }

    @Test
    public void digitTest() {
        for (char i = '0'; i < '9'; ++i) {
            assertSuccess(digit, i, "" + i);
        }

        assertFailure(digit, "syntax error in Test 'digitTest' at line 1 and column 1: unexpected end of input, expected a digit", "");
        assertFailure(digit, "syntax error in Test 'digitTest' at line 1 and column 1: unexpected token 'a', expected a digit", "a123");
    }

    @Test
    public void nat32Test() {
        assertSuccess(nat32, Integer.MAX_VALUE, Integer.MAX_VALUE + "");
        assertSuccess(nat32, 0, "0");
        assertSuccess(nat32, 123456789, "123456789");

        assertFailure(nat32, "syntax error in Test 'nat32Test' at line 1 and column 1: unexpected end of input, expected a natural number", "");
        assertFailure(nat32, "syntax error in Test 'nat32Test' at line 1 and column 1: unexpected token '-', expected a natural number", "-100");
        assertFailure(nat32, "syntax error in Test 'nat32Test' at line 1 and column 1: unexpected token '+', expected a natural number", "+100");
        assertFailure(nat32, "syntax error in Test 'nat32Test' at line 1 and column 1: unexpected token '-', expected a natural number", Integer.MIN_VALUE + "");
        assertFailure(nat32, "syntax error in Test 'nat32Test' at line 1 and column 1: unexpected token 'a', expected a natural number", "a123");
    }

    @Test
    public void int32Test() {
        assertSuccess(int32, 0, "0");
        assertSuccess(int32, -100, "-100");
        assertSuccess(int32, 100, "+100");
        assertSuccess(int32, Integer.MAX_VALUE, Integer.MAX_VALUE + "");
        assertSuccess(int32, Integer.MIN_VALUE, Integer.MIN_VALUE + "");

        assertFailure(int32, "syntax error in Test 'int32Test' at line 1 and column 1: unexpected token 'a', expected an integer number", "a123");
        assertFailure(int32, "syntax error in Test 'int32Test' at line 1 and column 1: unexpected end of input, expected an integer number", "");
    }

    @Test
    public void int64Test() {
        assertSuccess(int64, 0L, "0");
        assertSuccess(int64, -100L, "-100");
        assertSuccess(int64, 100L, "+100");
        assertSuccess(int64, Long.MAX_VALUE, Long.MAX_VALUE + "");
        assertSuccess(int64, Long.MIN_VALUE, Long.MIN_VALUE + "");

        assertFailure(int64, "syntax error in Test 'int64Test' at line 1 and column 1: unexpected token 'a', expected an integer number", "a123");
        assertFailure(int64, "syntax error in Test 'int64Test' at line 1 and column 1: unexpected end of input, expected an integer number", "");
    }

    @Test
    public void integerTest() {
        assertSuccess(integer, BigInteger.ZERO, "0");
        assertSuccess(integer, new BigInteger("12345678901234567890"), "+12345678901234567890");
        assertSuccess(integer, new BigInteger("-12345678901234567890"), "-12345678901234567890");

        assertFailure(integer, "syntax error in Test 'integerTest' at line 1 and column 1: unexpected token 'a', expected an integer number", "a123");
        assertFailure(integer, "syntax error in Test 'integerTest' at line 1 and column 1: unexpected end of input, expected an integer number", "");
    }

    @Test
    public void float32Test() {
        assertSuccess(float32, 0.0f, "0");
        assertSuccess(float32, -100.5f, "-100.5");
        assertSuccess(float32, 100.5f, "+100.5");
        assertSuccess(float32, 1.23e4f, "1.23e4");
        assertSuccess(float32, -1.23e-4f, "-1.23e-4");

        assertFailure(float32, "syntax error in Test 'float32Test' at line 1 and column 1: unexpected token 'a', expected a floating point number", "a123");
        assertFailure(float32, "syntax error in Test 'float32Test' at line 1 and column 1: unexpected end of input, expected a floating point number", "");
    }

    @Test
    public void float64Test() {
        assertSuccess(float64, 0.0, "0");
        assertSuccess(float64, -100.5, "-100.5");
        assertSuccess(float64, 100.5, "+100.5");
        assertSuccess(float64, 1.23e4, "1.23e4");
        assertSuccess(float64, -1.23e-4, "-1.23e-4");

        assertFailure(float64, "syntax error in Test 'float64Test' at line 1 and column 1: unexpected token 'a', expected a floating point number", "a123");
        assertFailure(float64, "syntax error in Test 'float64Test' at line 1 and column 1: unexpected end of input, expected a floating point number", "");
    }

}
