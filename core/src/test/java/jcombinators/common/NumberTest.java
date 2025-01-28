package jcombinators.common;

import jcombinators.ParserTest;
import org.junit.Test;

import java.math.BigInteger;

import static jcombinators.common.NumberParser.*;

public final class NumberTest extends ParserTest {

    @Test
    public void digitTest() {
        for (int i = 0; i < 9; ++i) {
            assertSuccess(digit, i, "" + i);
        }

        assertFailure(digit, "syntax error in Test 'digitTest' at line 1 and character 1: unexpected end of input, expected an input that matches '[0-9]'", "");
        assertFailure(digit, "syntax error in Test 'digitTest' at line 1 and character 1: unexpected character 'a', expected an input that matches '[0-9]'", "a123");
    }

    @Test
    public void numberTest() {
        assertSuccess(number, Integer.MAX_VALUE, Integer.MAX_VALUE + "");
        assertSuccess(number, 0, "0");
        assertSuccess(number, 123456789, "123456789");

        assertFailure(number, "syntax error in Test 'numberTest' at line 1 and character 1: unexpected end of input, expected an input that matches '[0-9]+'", "");
        assertFailure(number, "syntax error in Test 'numberTest' at line 1 and character 1: unexpected character '-', expected an input that matches '[0-9]+'", "-100");
        assertFailure(number, "syntax error in Test 'numberTest' at line 1 and character 1: unexpected character '+', expected an input that matches '[0-9]+'", "+100");
        assertFailure(number, "syntax error in Test 'numberTest' at line 1 and character 1: unexpected character '-', expected an input that matches '[0-9]+'", Integer.MIN_VALUE + "");
        assertFailure(number, "syntax error in Test 'numberTest' at line 1 and character 1: unexpected character 'a', expected an input that matches '[0-9]+'", "a123");
    }

    @Test
    public void int32Test() {
        assertSuccess(int32, 0, "0");
        assertSuccess(int32, -100, "-100");
        assertSuccess(int32, 100, "+100");
        assertSuccess(int32, Integer.MAX_VALUE, Integer.MAX_VALUE + "");
        assertSuccess(int32, Integer.MIN_VALUE, Integer.MIN_VALUE + "");

        assertFailure(int32, "syntax error in Test 'int32Test' at line 1 and character 1: unexpected character 'a', expected an input that matches '[+-]?[0-9]+'", "a123");
        assertFailure(int32, "syntax error in Test 'int32Test' at line 1 and character 1: unexpected end of input, expected an input that matches '[+-]?[0-9]+'", "");
    }

    @Test
    public void int64Test() {
        assertSuccess(int64, 0L, "0");
        assertSuccess(int64, -100L, "-100");
        assertSuccess(int64, 100L, "+100");
        assertSuccess(int64, Long.MAX_VALUE, Long.MAX_VALUE + "");
        assertSuccess(int64, Long.MIN_VALUE, Long.MIN_VALUE + "");

        assertFailure(int64, "syntax error in Test 'int64Test' at line 1 and character 1: unexpected character 'a', expected an input that matches '[+-]?[0-9]+'", "a123");
        assertFailure(int64, "syntax error in Test 'int64Test' at line 1 and character 1: unexpected end of input, expected an input that matches '[+-]?[0-9]+'", "");
    }

    @Test
    public void integerTest() {
        assertSuccess(integer, BigInteger.ZERO, "0");
        assertSuccess(integer, new BigInteger("12345678901234567890"), "+12345678901234567890");
        assertSuccess(integer, new BigInteger("-12345678901234567890"), "-12345678901234567890");

        assertFailure(integer, "syntax error in Test 'integerTest' at line 1 and character 1: unexpected character 'a', expected an input that matches '[+-]?[0-9]+'", "a123");
        assertFailure(integer, "syntax error in Test 'integerTest' at line 1 and character 1: unexpected end of input, expected an input that matches '[+-]?[0-9]+'", "");
    }

    @Test
    public void float32Test() {
        assertSuccess(float32, 0.0f, "0");
        assertSuccess(float32, -100.5f, "-100.5");
        assertSuccess(float32, 100.5f, "+100.5");
        assertSuccess(float32, 1.23e4f, "1.23e4");
        assertSuccess(float32, -1.23e-4f, "-1.23e-4");

        assertFailure(float32, "syntax error in Test 'float32Test' at line 1 and character 1: unexpected character 'a', expected an input that matches '[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?'", "a123");
        assertFailure(float32, "syntax error in Test 'float32Test' at line 1 and character 1: unexpected end of input, expected an input that matches '[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?'", "");
    }

    @Test
    public void float64Test() {
        assertSuccess(float64, 0.0, "0");
        assertSuccess(float64, -100.5, "-100.5");
        assertSuccess(float64, 100.5, "+100.5");
        assertSuccess(float64, 1.23e4, "1.23e4");
        assertSuccess(float64, -1.23e-4, "-1.23e-4");

        assertFailure(float64, "syntax error in Test 'float64Test' at line 1 and character 1: unexpected character 'a', expected an input that matches '[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?'", "a123");
        assertFailure(float64, "syntax error in Test 'float64Test' at line 1 and character 1: unexpected end of input, expected an input that matches '[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?'", "");
    }

}
