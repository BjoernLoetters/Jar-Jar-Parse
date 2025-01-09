package jpc;

import jpc.data.Tuple;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static jpc.common.StringParser.character;
import static jpc.common.StringParser.regex;

public final class CombinatorTest extends ParserTest {

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
        Parser<Tuple<Character, Character>> parser = first.and(second);

        assertSuccess(parser, new Tuple<>('a', 'b'), "ab");
    }

    @Test
    public void andFailureTest() {
        Parser<Character> first = character('a');
        Parser<Character> second = character('b');
        Parser<Tuple<Character, Character>> parser = first.and(second);

        assertFailure(parser, "unexpected character 'c', expected literal 'b'", "ac");
    }

    @Test
    public void notSuccessTest() {
        final Parser<Void> parser = character('a').not();
        assertSuccess(parser, null, "b");
    }

    @Test
    public void notFailureTest() {
        final Parser<Void> parser = character('a').not();
        assertFailure(parser, "unexpected 'a'", "a");
    }

    @Test
    public void optionalSuccessTest() {
        final Parser<Optional<Character>> parser = character('a').optional();
        assertSuccess(parser, Optional.of('a'), "a");
    }

    @Test
    public void optionalEmptyTest() {
        final Parser<Optional<Character>> parser = character('a').optional();
        assertSuccess(parser, Optional.empty(), "b");
    }

    @Test
    public void separateSuccessTest() {
        final Parser<List<Character>> parser = character('a').separate(character(','));
        assertSuccess(parser, List.of('a'), "a");
        assertSuccess(parser, List.of('a', 'a', 'a'), "a,a,a");
    }

    @Test
    public void separateEmptyTest() {
        final Parser<List<Character>> parser = character('a').separate(character(','));
        assertSuccess(parser, List.of(), "");
    }

    @Test
    public void separateTrailing1Test() {
        final Parser<List<Character>> parser = character('a').separate(character(','));
        assertSuccess(parser, List.of('a', 'a'), "a,a,");
    }

    @Test
    public void separateTrailing2Test() {
        final Parser<Tuple<List<Character>, Character>> parser = character('a').separate(character(',')).and(character(','));
        assertSuccess(parser, new Tuple<>(List.of('a', 'a'), ','), "a,a,");
    }

    @Test
    public void separate1SuccessTest() {
        final Parser<List<Character>> parser = character('a').separate1(character(','));
        assertSuccess(parser, List.of('a', 'a', 'a'), "a,a,a");
    }

    @Test
    public void separate1FailureTest() {
        final Parser<List<Character>> parser = character('a').separate1(character(','));
        assertFailure(parser, "unexpected end of input, expected literal 'a'", "");
        assertFailure(parser, "unexpected character 'b', expected literal 'a'", "b");
    }

    @Test
    public void chainLeftSuccessTest() {
        final Parser<Integer> number = regex("[0-9]").map(Integer::parseInt);
        final Parser<BiFunction<Integer, Integer, Integer>> plus = character('+').map(op -> Integer::sum);
        final Parser<Integer> parser = Parser.chainLeft(number, plus);

        assertSuccess(parser, 6, "1+2+3");
    }

    @Test
    public void chainRightSuccessTest() {
        final Parser<Integer> number = regex("[0-9]").map(Integer::parseInt);
        final Parser<BiFunction<Integer, Integer, Integer>> exponent = character('^').map(op -> (a, b) -> (int) Math.pow(a, b));
        final Parser<Integer> parser = Parser.chainRight(number, exponent);

        assertSuccess(parser, 2, "2^3^0");
    }

    @Test
    public void successParserTest() {
        final Parser<String> parser = Parser.success("ok");
        assertSuccess(parser, "ok", "anything");
    }

    @Test
    public void failParserTest() {
        final Parser<String> parser = Parser.fail("error");
        assertFailure(parser, "error", "anything");
    }

    @Test
    public void abortParserTest() {
        Parser<String> parser = Parser.abort("abort");
        assertFailure(parser, "abort", "anything");
    }

}
