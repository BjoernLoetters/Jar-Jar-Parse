package jpc;

import jpc.data.Product;
import org.junit.Test;

import java.util.List;

import static jpc.common.StringParser.character;

public final class UtilityTest extends ParserTest {

    @Test
    public void repeat1SuccessSingleMatchTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertSuccess(parser, List.of('a'), "a");
    }

    @Test
    public void repeat1SuccessMultipleMatchesTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertSuccess(parser, List.of('a', 'a', 'a'), "aaa");
    }

    @Test
    public void repeat1FailureNoMatchTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertFailure(parser, "unexpected character 'b', expected literal 'a'", "b");
    }

    @Test
    public void repeat1SuccessPartialMatchTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertSuccess(parser, List.of('a', 'a'), "aab");
    }

    @Test
    public void repeat1EmptyInputTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertFailure(parser, "unexpected end of input, expected literal 'a'", "");
    }

    @Test
    public void keepRightSuccessTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Character> parser = first.keepRight(second);

        assertSuccess(parser, 'b', "ab");
    }

    @Test
    public void keepRightFailureTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Character> parser = first.keepRight(second);

        assertFailure(parser, "unexpected character 'c', expected literal 'b'", "ac");
    }

    @Test
    public void keepLeftSuccessTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Character> parser = first.keepLeft(second);

        assertSuccess(parser, 'a', "ab");
    }

    @Test
    public void keepLeftFailureTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Character> parser = first.keepLeft(second);

        assertFailure(parser, "unexpected character 'c', expected literal 'b'", "ac");
    }

    @Test
    public void andSuccessTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Product<Character, Character>> parser = first.and(second);

        assertSuccess(parser, new Product<>('a', 'b'), "ab");
    }

    @Test
    public void andFailureTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Product<Character, Character>> parser = first.and(second);

        assertFailure(parser, "unexpected character 'c', expected literal 'b'", "ac");
    }

}
