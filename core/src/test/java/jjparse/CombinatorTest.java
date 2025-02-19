package jjparse;

import jjparse.data.Product;
import jjparse.input.Input;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertFailure(parser, "syntax error in Test 'repeat1FailureNoMatchTest' at line 1 and column 1: unexpected character 'b', expected the literal 'a'", "b");
    }

    @Test
    public void repeat1SuccessPartialMatchTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertSuccess(parser, List.of('a', 'a'), "aab");
    }

    @Test
    public void repeat1EmptyInputTest() {
        Parser<List<Character>> parser = character('a').repeat1();
        assertFailure(parser, "syntax error in Test 'repeat1EmptyInputTest' at line 1 and column 1: unexpected end of input, expected the literal 'a'", "");
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

        assertFailure(parser, "syntax error in Test 'keepRightFailureTest' at line 1 and column 2: unexpected character 'c', expected the literal 'b'", "ac");
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

        assertFailure(parser, "syntax error in Test 'keepLeftFailureTest' at line 1 and column 2: unexpected character 'c', expected the literal 'b'", "ac");
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

        assertFailure(parser, "syntax error in Test 'andFailureTest' at line 1 and column 2: unexpected character 'c', expected the literal 'b'", "ac");
    }

    @Test
    public void notSuccessTest() {
        final Parser<Void> parser = character('a').not();
        assertSuccess(parser, null, "b");
    }

    @Test
    public void notFailureTest() {
        final Parser<Void> parser = character('a').not();
        assertFailure(parser, "syntax error in Test 'notFailureTest' at line 1 and column 1: unexpected character 'a', expected anything but the literal 'a'", "a");
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
        final Parser<Product<List<Character>, Character>> parser = character('a').separate(character(',')).and(character(','));
        assertSuccess(parser, new Product<>(List.of('a', 'a'), ','), "a,a,");
    }

    @Test
    public void separate1SuccessTest() {
        final Parser<List<Character>> parser = character('a').separate1(character(','));
        assertSuccess(parser, List.of('a', 'a', 'a'), "a,a,a");
    }

    @Test
    public void separate1FailureTest() {
        final Parser<List<Character>> parser = character('a').separate1(character(','));
        assertFailure(parser, "syntax error in Test 'separate1FailureTest' at line 1 and column 1: unexpected end of input, expected the literal 'a'", "");
        assertFailure(parser, "syntax error in Test 'separate1FailureTest' at line 1 and column 1: unexpected character 'b', expected the literal 'a'", "b");
    }

    @Test
    public void chainLeftSuccessTest() {
        final Parser<Integer> number = regex("[0-9]").map(Integer::parseInt);
        final Parser<BiFunction<Integer, Integer, Integer>> plus = character('+').map(op -> Integer::sum);
        final Parser<Integer> parser = chainLeft1(number, plus);

        assertSuccess(parser, 6, "1+2+3");
    }

    @Test
    public void chainRightSuccessTest() {
        final Parser<Integer> number = regex("[0-9]").map(Integer::parseInt);
        final Parser<BiFunction<Integer, Integer, Integer>> exponent = character('^').map(op -> (a, b) -> (int) Math.pow(a, b));
        final Parser<Integer> parser = chainRight1(number, exponent);

        assertSuccess(parser, 2, "2^3^0");
    }

    @Test
    public void successParserTest() {
        final Parser<String> parser = success(() -> "ok");
        assertSuccess(parser, "ok", "anything");
    }

    @Test
    public void errorParserTest() {
        final Parser<String> parser = error("error");
        assertFailure(parser, "error", "anything");
    }

    @Test
    public void abortParserTest() {
        Parser<String> parser = abort("abort");
        assertFailure(parser, "abort", "anything");
    }

    @Test
    public void commitSuccessTest() {
        Parser<Character> parser = character('a').commit();
        assertSuccess(parser, 'a', "a");
    }

    @Test
    public void commitFailureTest() {
        Parser<Character> parser = character('a').commit();
        assertFailure(parser, "syntax error in Test 'commitFailureTest' at line 1 and column 1: unexpected character 'b', expected the literal 'a'", "b");
    }

    @Test
    public void commitChainedParserTest() {
        Parser<Product<Character, Character>> parser = character('a').commit().and(character('b'));
        assertSuccess(parser, new Product<>('a', 'b'), "ab");
    }

    @Test
    public void commitChoiceNoBacktrackingTest() {
        Parser<Character> parser = character('a').commit().or(character('b'));
        assertFailure(parser, "syntax error in Test 'commitChoiceNoBacktrackingTest' at line 1 and column 1: unexpected character 'b', expected the literal 'a'", "b");
    }

    @Test
    public void positionParserCorrectPositionTest() {
        final String contents = "line1 column1\n line2\n    column3  line3\n";
        final Input<Character> input = Input.of("Test '" + getTestName() + "'", contents);

        final Parser<Function<Input<Character>.Position, String>> parser = regex("line[0-9]|column[0-9]").map(ignore -> position -> position.toString());

        final Parser<String> positionParser = position(parser);
        final Result<String> firstResult = positionParser.apply(input);

        assertTrue(firstResult.isSuccess());
        assertEquals("Test 'positionParserCorrectPositionTest' at line 1 and column 1", firstResult.get().get());

        final Result<String> secondResult = positionParser.apply(firstResult.rest);
        assertTrue(secondResult.isSuccess());
        assertEquals("Test 'positionParserCorrectPositionTest' at line 1 and column 7", secondResult.get().get());

        final Result<String> thirdResult = positionParser.apply(secondResult.rest);
        assertTrue(thirdResult.isSuccess());
        assertEquals("Test 'positionParserCorrectPositionTest' at line 2 and column 2", thirdResult.get().get());

        final Result<String> fourthResult = positionParser.apply(thirdResult.rest);
        assertTrue(fourthResult.isSuccess());
        assertEquals("Test 'positionParserCorrectPositionTest' at line 3 and column 5", fourthResult.get().get());

        final Result<String> fifthResult = positionParser.apply(fourthResult.rest);
        assertTrue(fifthResult.isSuccess());
        assertEquals("Test 'positionParserCorrectPositionTest' at line 3 and column 14", fifthResult.get().get());
    }

    @Test
    public void positionParserCorrectCodePointPositionTest() {
        final String contents = "😀🙂\n  🙂😀";
        final Input<Character> input = Input.of("Test '" + getTestName() + "'", contents);

        final Parser<Function<Input<Character>.Position, String>> parser = regex("😀|🙂").map(ignore -> position -> position.toString());

        final Parser<String> positionParser = position(parser);
        final Result<String> firstResult = positionParser.apply(input);

        assertTrue(firstResult.isSuccess());
        assertEquals("Test 'positionParserCorrectCodePointPositionTest' at line 1 and column 1", firstResult.get().get());

        final Result<String> secondResult = positionParser.apply(firstResult.rest);
        assertTrue(secondResult.isSuccess());
        assertEquals("Test 'positionParserCorrectCodePointPositionTest' at line 1 and column 2", secondResult.get().get());

        final Result<String> thirdResult = positionParser.apply(secondResult.rest);
        assertTrue(thirdResult.isSuccess());
        assertEquals("Test 'positionParserCorrectCodePointPositionTest' at line 2 and column 3", thirdResult.get().get());

        final Result<String> fourthResult = positionParser.apply(thirdResult.rest);
        assertTrue(fourthResult.isSuccess());
        assertEquals("Test 'positionParserCorrectCodePointPositionTest' at line 2 and column 4", fourthResult.get().get());
    }

}
