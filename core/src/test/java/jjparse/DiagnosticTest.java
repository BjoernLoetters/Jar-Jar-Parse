package jjparse;

import jjparse.diagnostic.Expectation;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DiagnosticTest extends ParserTest {

    @Test
    public void unknownExpectationTest() {
        assertEquals("", Expectation.unknown.toString());
        assertTrue(Expectation.unknown.isUnknown());
    }

    @Test
    public void primitiveExpectationTest() {
        assertEquals("expected a token", Expectation.from("a token").toString());
        assertFalse(Expectation.from("a token").isUnknown());
    }

    @Test
    public void formattedExpectationTest() {
        assertEquals("expected the token 'x'", Expectation.from("the token '%s'", "x").toString());
        assertEquals("expected a number between 1 and 10", Expectation.from("a number between %d and %d", 1, 10).toString());
    }

    @Test
    public void justifiedExpectationTest() {
        assertEquals("expected a token, because a token is required", Expectation.from("a token").because("a token is required").toString());
    }

    @Test
    public void justifiedUnknownExpectationTest() {
        assertEquals("because the input is invalid", Expectation.unknown.because("the input is invalid").toString());
    }

    @Test
    public void multipleJustificationsTest() {
        assertEquals("expected a token, because third reason", Expectation.from("a token").because("first reason").because("second reason").because("third reason").toString());
    }

    @Test
    public void multipleUnknownJustificationsTest() {
        assertEquals("because third reason", Expectation.unknown.because("first reason").because("second reason").because("third reason").toString());
    }

    @Test
    public void mergedExpectationsTest() {
        assertEquals("expected A, or B", Expectation.from("A").merge(Expectation.from("B")).toString());
    }

    @Test
    public void multipleMergedExpectationsTest() {
        assertEquals("expected A, B, or C", Expectation.from("A").merge(Expectation.from("B")).merge(Expectation.from("C")).toString());
        assertEquals("expected A, B, C, or D", Expectation.from("A").merge(Expectation.from("B")).merge(Expectation.from("C")).merge(Expectation.from("D")).toString());
    }

    @Test
    public void nestedMergedExpectationsTest() {
        assertEquals("expected A, B, C, or D", Expectation.from("A").merge(Expectation.from("B")).merge(Expectation.from("C").merge(Expectation.from("D"))).toString());
    }

    @Test
    public void mergedUnknownExpectationsTest() {
        assertEquals("", Expectation.unknown.merge(Expectation.unknown).toString());
        assertEquals("expected A", Expectation.unknown.merge(Expectation.from("A")).toString());
        assertEquals("expected A", Expectation.from("A").merge(Expectation.unknown).toString());
        assertEquals("expected A, or B", Expectation.unknown.merge(Expectation.from("A")).merge(Expectation.unknown).merge(Expectation.from("B")).merge(Expectation.unknown).toString());
    }

    @Test
    public void mergedDuplicateExpectationsTest() {
        assertEquals("expected A, or A", Expectation.from("A").merge(Expectation.from("A")).toString());
        assertEquals("expected A, A, or A", Expectation.from("A").merge(Expectation.from("A")).merge(Expectation.from("A")).toString());
    }

    @Test
    public void mergedJustifiedExpectationsTest() {
        assertEquals("expected A, or B", Expectation.from("A").because("reason A").merge(Expectation.from("B")).toString());
        assertEquals("expected A, or B", Expectation.from("A").merge(Expectation.from("B").because("reason B")).toString());
        assertEquals("expected A, or B", Expectation.from("A").because("reason A").merge(Expectation.from("B").because("reason B")).toString());
    }

    @Test
    public void mergedJustifiedUnknownExpectationsTest() {
        assertEquals("", Expectation.unknown.because("reason A").merge(Expectation.unknown.because("reason B")).toString());
        assertEquals("expected A", Expectation.unknown.because("reason A").merge(Expectation.from("A")).toString());
        assertEquals("expected A", Expectation.from("A").merge(Expectation.unknown.because("reason B")).toString());
    }

    @Test
    public void justifiedMergedExpectationsTest() {
        assertEquals("expected A, or B, because a common reason", Expectation.from("A").merge(Expectation.from("B")).because("a common reason").toString());
    }

    @Test
    public void multipleJustificationsOfMergedExpectationsTest() {
        assertEquals("expected A, or B, because second reason", Expectation.from("A").merge(Expectation.from("B")).because("first reason").because("second reason").toString());
    }

    @Test
    public void mergedJustifiedUnionTest() {
        assertEquals("expected A, B, or C", Expectation.from("A").merge(Expectation.from("B")).because("reason AB").merge(Expectation.from("C")).toString());
        assertEquals("expected A, B, or C", Expectation.from("A").merge(Expectation.from("B").merge(Expectation.from("C")).because("reason BC")).toString());
    }

    @Test
    public void justifiedNestedUnionTest() {
        assertEquals("expected A, B, C, or D, because a common reason", Expectation.from("A").merge(Expectation.from("B")).because("reason AB").merge(Expectation.from("C").merge(Expectation.from("D")).because("reason CD")).because("a common reason").toString());
    }

    @Test
    public void unknownFailureTest() {
        assertFailure(error(), "syntax error in Test 'unknownFailureTest' at line 1 and column 1: unexpected token 'x'", "xyz");
    }

    @Test
    public void unknownFailureAtEndOfInputTest() {
        assertFailure(error(), "syntax error in Test 'unknownFailureAtEndOfInputTest' at line 1 and column 1: unexpected end of input", "");
    }

    @Test
    public void expectedFailureTest() {
        assertFailure(character('a').expect("a letter"), "syntax error in Test 'expectedFailureTest' at line 1 and column 1: unexpected token 'x', expected a letter", "xyz");
    }

    @Test
    public void expectedFailureAtEndOfInputTest() {
        assertFailure(character('a').expect("a letter"), "syntax error in Test 'expectedFailureAtEndOfInputTest' at line 1 and column 1: unexpected end of input, expected a letter", "");
    }

    @Test
    public void multipleExpectsTest() {
        assertFailure(character('a').expect("first expectation").expect("second expectation").expect("third expectation"), "syntax error in Test 'multipleExpectsTest' at line 1 and column 1: unexpected token 'x', expected third expectation", "xyz");
    }

    @Test
    public void expectUnknownTest() {
        assertFailure(this.<Character>error().expect("a letter"), "syntax error in Test 'expectUnknownTest' at line 1 and column 1: unexpected token 'x', expected a letter", "xyz");
    }

    @Test
    public void justifiedFailureTest() {
        assertFailure(character('a').because("a letter is required"), "syntax error in Test 'justifiedFailureTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', because a letter is required", "xyz");
    }

    @Test
    public void justifiedFailureAtEndOfInputTest() {
        assertFailure(character('a').because("a letter is required"), "syntax error in Test 'justifiedFailureAtEndOfInputTest' at line 1 and column 1: unexpected end of input, expected the token 'a', because a letter is required", "");
    }

    @Test
    public void justifiedUnknownFailureTest() {
        assertFailure(this.<Character>error().because("the input is invalid"), "syntax error in Test 'justifiedUnknownFailureTest' at line 1 and column 1: unexpected token 'x', because the input is invalid", "xyz");
    }

    @Test
    public void multipleBecauseTest() {
        assertFailure(character('a').because("first reason").because("second reason").because("third reason"), "syntax error in Test 'multipleBecauseTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', because third reason", "xyz");
    }

    @Test
    public void multipleBecauseOnUnknownTest() {
        assertFailure(this.<Character>error().because("first reason").because("second reason"), "syntax error in Test 'multipleBecauseOnUnknownTest' at line 1 and column 1: unexpected token 'x', because second reason", "xyz");
    }

    @Test
    public void expectBeforeBecauseTest() {
        assertFailure(character('a').expect("a letter").because("a letter is required"), "syntax error in Test 'expectBeforeBecauseTest' at line 1 and column 1: unexpected token 'x', expected a letter, because a letter is required", "xyz");
    }

    @Test
    public void expectAfterBecauseTest() {
        assertFailure(character('a').because("a letter is required").expect("a character"), "syntax error in Test 'expectAfterBecauseTest' at line 1 and column 1: unexpected token 'x', expected a character", "xyz");
    }

    @Test
    public void expectBetweenBecauseTest() {
        assertFailure(character('a').because("first reason").expect("a letter").because("second reason"), "syntax error in Test 'expectBetweenBecauseTest' at line 1 and column 1: unexpected token 'x', expected a letter, because second reason", "xyz");
    }

    @Test
    public void multipleExpectAndBecauseTest() {
        assertFailure(character('a').expect("first expectation").because("first reason").expect("second expectation").because("second reason").expect("third expectation").because("third reason"), "syntax error in Test 'multipleExpectAndBecauseTest' at line 1 and column 1: unexpected token 'x', expected third expectation, because third reason", "xyz");
    }

    @Test
    public void choiceFailureTest() {
        assertFailure(character('a').or(character('b')), "syntax error in Test 'choiceFailureTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b'", "xyz");
    }

    @Test
    public void multipleChoiceFailureTest() {
        assertFailure(choice(character('a'), character('b'), character('c'), character('d')), "syntax error in Test 'multipleChoiceFailureTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', the token 'b', the token 'c', or the token 'd'", "xyz");
    }

    @Test
    public void nestedChoiceFailureTest() {
        assertFailure(character('a').or(character('b').or(character('c'))), "syntax error in Test 'nestedChoiceFailureTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', the token 'b', or the token 'c'", "xyz");
    }

    @Test
    public void choiceFailureAtEndOfInputTest() {
        assertFailure(character('a').or(character('b')).or(character('c')), "syntax error in Test 'choiceFailureAtEndOfInputTest' at line 1 and column 1: unexpected end of input, expected the token 'a', the token 'b', or the token 'c'", "");
    }

    @Test
    public void choiceWithUnknownFirstTest() {
        assertFailure(this.<Character>error().or(character('a')), "syntax error in Test 'choiceWithUnknownFirstTest' at line 1 and column 1: unexpected token 'x', expected the token 'a'", "xyz");
    }

    @Test
    public void choiceWithUnknownLastTest() {
        assertFailure(character('a').or(this.<Character>error()), "syntax error in Test 'choiceWithUnknownLastTest' at line 1 and column 1: unexpected token 'x', expected the token 'a'", "xyz");
    }

    @Test
    public void choiceWithUnknownBetweenTest() {
        assertFailure(character('a').or(this.<Character>error()).or(character('b')), "syntax error in Test 'choiceWithUnknownBetweenTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b'", "xyz");
    }

    @Test
    public void choiceWithOnlyUnknownsTest() {
        assertFailure(choice(this.<Character>error(), this.<Character>error(), this.<Character>error()), "syntax error in Test 'choiceWithOnlyUnknownsTest' at line 1 and column 1: unexpected token 'x'", "xyz");
    }

    @Test
    public void choiceWithDuplicateExpectationsTest() {
        assertFailure(character('a').expect("A").or(character('b').expect("A")), "syntax error in Test 'choiceWithDuplicateExpectationsTest' at line 1 and column 1: unexpected token 'x', expected A, or A", "xyz");
    }

    @Test
    public void choiceWithJustificationOnFirstAlternativeTest() {
        assertFailure(character('a').because("reason A").or(character('b')), "syntax error in Test 'choiceWithJustificationOnFirstAlternativeTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b'", "xyz");
    }

    @Test
    public void choiceWithJustificationOnSecondAlternativeTest() {
        assertFailure(character('a').or(character('b').because("reason B")), "syntax error in Test 'choiceWithJustificationOnSecondAlternativeTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b'", "xyz");
    }

    @Test
    public void choiceWithMultipleJustifiedAlternativesTest() {
        assertFailure(character('a').because("reason A").or(character('b').because("reason B")).or(character('c').because("reason C")), "syntax error in Test 'choiceWithMultipleJustifiedAlternativesTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', the token 'b', or the token 'c'", "xyz");
    }

    @Test
    public void choiceWithJustifiedUnknownTest() {
        assertFailure(character('a').or(this.<Character>error().because("an unknown reason")), "syntax error in Test 'choiceWithJustifiedUnknownTest' at line 1 and column 1: unexpected token 'x', expected the token 'a'", "xyz");
    }

    @Test
    public void justifiedChoiceTest() {
        assertFailure(character('a').or(character('b')).because("one of these alternatives is required"), "syntax error in Test 'justifiedChoiceTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b', because one of these alternatives is required", "xyz");
    }

    @Test
    public void multipleJustificationsOnChoiceTest() {
        assertFailure(character('a').or(character('b')).because("first reason").because("second reason"), "syntax error in Test 'multipleJustificationsOnChoiceTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b', because second reason", "xyz");
    }

    @Test
    public void justificationAfterJustifiedChoiceTest() {
        assertFailure(character('a').because("reason A").or(character('b').because("reason B")).because("common reason"), "syntax error in Test 'justificationAfterJustifiedChoiceTest' at line 1 and column 1: unexpected token 'x', expected the token 'a', or the token 'b', because common reason", "xyz");
    }

    @Test
    public void expectAfterChoiceTest() {
        assertFailure(character('a').or(character('b')).expect("a letter"), "syntax error in Test 'expectAfterChoiceTest' at line 1 and column 1: unexpected token 'x', expected a letter", "xyz");
    }

    @Test
    public void expectAfterJustifiedChoiceTest() {
        assertFailure(character('a').or(character('b')).because("a letter is required").expect("a letter"), "syntax error in Test 'expectAfterJustifiedChoiceTest' at line 1 and column 1: unexpected token 'x', expected a letter", "xyz");
    }

    @Test
    public void expectBeforeJustifiedChoiceTest() {
        assertFailure(character('a').or(character('b')).expect("a letter").because("a letter is required"), "syntax error in Test 'expectBeforeJustifiedChoiceTest' at line 1 and column 1: unexpected token 'x', expected a letter, because a letter is required", "xyz");
    }

    @Test
    public void choicePrefersFirstFarthestFailureTest() {
        assertFailure(literal("abc").expect("ABC").or(literal("ax").expect("AX")), "syntax error in Test 'choicePrefersFirstFarthestFailureTest' at line 1 and column 3: unexpected token 'x', expected ABC", "abx");
    }

    @Test
    public void choicePrefersSecondFarthestFailureTest() {
        assertFailure(literal("ax").expect("AX").or(literal("abc").expect("ABC")), "syntax error in Test 'choicePrefersSecondFarthestFailureTest' at line 1 and column 3: unexpected token 'x', expected ABC", "abx");
    }

    @Test
    public void choiceMergesFarthestFailuresTest() {
        assertFailure(choice(literal("abc").expect("ABC"), literal("abd").expect("ABD"), literal("ax").expect("AX")), "syntax error in Test 'choiceMergesFarthestFailuresTest' at line 1 and column 3: unexpected token 'x', expected ABC, or ABD", "abx");
    }

    @Test
    public void choiceMergesFarthestFailuresWithJustificationsTest() {
        assertFailure(choice(literal("abc").expect("ABC").because("reason ABC"), literal("abd").expect("ABD").because("reason ABD"), literal("ax").expect("AX").because("reason AX")), "syntax error in Test 'choiceMergesFarthestFailuresWithJustificationsTest' at line 1 and column 3: unexpected token 'x', expected ABC, or ABD", "abx");
    }

    @Test
    public void choicePreservesFarthestJustificationTest() {
        assertFailure(literal("abc").expect("ABC").because("reason ABC").or(literal("ax").expect("AX")), "syntax error in Test 'choicePreservesFarthestJustificationTest' at line 1 and column 3: unexpected token 'x', expected ABC, because reason ABC", "abx");
    }

    @Test
    public void choicePreservesSecondFarthestJustificationTest() {
        assertFailure(literal("ax").expect("AX").or(literal("abc").expect("ABC").because("reason ABC")), "syntax error in Test 'choicePreservesSecondFarthestJustificationTest' at line 1 and column 3: unexpected token 'x', expected ABC, because reason ABC", "abx");
    }

    @Test
    public void choicePrefersFarthestFailureAtEndOfInputTest() {
        assertFailure(literal("abc").expect("ABC").or(literal("ax").expect("AX")), "syntax error in Test 'choicePrefersFarthestFailureAtEndOfInputTest' at line 1 and column 3: unexpected end of input, expected ABC", "ab");
    }

    @Test
    public void choiceWithRightUnknownAtEarlierPositionTest() {
        assertFailure(literal("abc").expect("ABC").or(character('a').andr(this.<String>error())), "syntax error in Test 'choiceWithRightUnknownAtEarlierPositionTest' at line 1 and column 3: unexpected token 'x', expected ABC", "abx");
    }

    @Test
    public void choiceWithLeftUnknownAtEarlierPositionTest() {
        assertFailure(character('a').andr(this.<String>error()).or(literal("abc").expect("ABC")), "syntax error in Test 'choiceWithLeftUnknownAtEarlierPositionTest' at line 1 and column 3: unexpected token 'x', expected ABC", "abx");
    }

    @Test
    public void choiceWithLeftUnknownAtFarthestPositionTest() {
        assertFailure(literal("ab").andr(this.<String>error()).or(literal("ax").expect("AX")), "syntax error in Test 'choiceWithLeftUnknownAtFarthestPositionTest' at line 1 and column 3: unexpected token 'x'", "abx");
    }

    @Test
    public void choiceWithRightUnknownAtFarthestPositionTest() {
        assertFailure(literal("ax").expect("AX").or(literal("ab").andr(this.<String>error())), "syntax error in Test 'choiceWithRightUnknownAtFarthestPositionTest' at line 1 and column 3: unexpected token 'x'", "abx");
    }

    @Test
    public void sequenceFirstFailureTest() {
        assertFailure(character('a').expect("A").and(character('b').expect("B")), "syntax error in Test 'sequenceFirstFailureTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void sequenceSecondFailureTest() {
        assertFailure(character('a').expect("A").and(character('b').expect("B")), "syntax error in Test 'sequenceSecondFailureTest' at line 1 and column 2: unexpected token 'x', expected B", "ax");
    }

    @Test
    public void sequenceSecondJustifiedFailureTest() {
        assertFailure(character('a').expect("A").and(character('b').expect("B").because("B is required")), "syntax error in Test 'sequenceSecondJustifiedFailureTest' at line 1 and column 2: unexpected token 'x', expected B, because B is required", "ax");
    }

    @Test
    public void sequenceOuterExpectationTest() {
        assertFailure(character('a').and(character('b')).expect("the sequence AB"), "syntax error in Test 'sequenceOuterExpectationTest' at line 1 and column 2: unexpected token 'x', expected the sequence AB", "ax");
    }

    @Test
    public void sequenceOuterJustificationTest() {
        assertFailure(character('a').and(character('b')).because("the sequence must be complete"), "syntax error in Test 'sequenceOuterJustificationTest' at line 1 and column 2: unexpected token 'x', expected the token 'b', because the sequence must be complete", "ax");
    }

    @Test
    public void mapPreservesExpectationTest() {
        assertFailure(character('a').expect("A").map(character -> Character.toString(character)), "syntax error in Test 'mapPreservesExpectationTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void flatMapPreservesExpectationTest() {
        assertFailure(character('a').flatMap(ignore -> character('b').expect("B")), "syntax error in Test 'flatMapPreservesExpectationTest' at line 1 and column 2: unexpected token 'x', expected B", "ax");
    }

    @Test
    public void successfulChoiceDiscardsFailureTest() {
        assertSuccess(character('a').or(character('b')), 'b', "b");
    }

    @Test
    public void optionalDiscardsFailureTest() {
        assertSuccess(character('a').optional(), Optional.empty(), "xyz");
    }

    @Test
    public void repeatDiscardsFailureTest() {
        assertSuccess(character('a').repeat(), List.of(), "xyz");
    }

    @Test
    public void abortUnknownFailureTest() {
        assertFailure(abort(), "syntax error in Test 'abortUnknownFailureTest' at line 1 and column 1: unexpected token 'x'", "xyz");
    }

    @Test
    public void abortExpectedFailureTest() {
        assertFailure(this.<Character>abort().expect("A"), "syntax error in Test 'abortExpectedFailureTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void abortJustifiedFailureTest() {
        assertFailure(this.<Character>abort().expect("A").because("A is required"), "syntax error in Test 'abortJustifiedFailureTest' at line 1 and column 1: unexpected token 'x', expected A, because A is required", "xyz");
    }

    @Test
    public void commitPreservesExpectationTest() {
        assertFailure(character('a').expect("A").commit(), "syntax error in Test 'commitPreservesExpectationTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void commitPreservesJustificationTest() {
        assertFailure(character('a').expect("A").because("A is required").commit(), "syntax error in Test 'commitPreservesJustificationTest' at line 1 and column 1: unexpected token 'x', expected A, because A is required", "xyz");
    }

    @Test
    public void abortPreventsChoiceTest() {
        assertFailure(character('a').expect("A").commit().or(character('b').expect("B")), "syntax error in Test 'abortPreventsChoiceTest' at line 1 and column 1: unexpected token 'b', expected A", "b");
    }

    @Test
    public void abortPreventsOptionalTest() {
        assertFailure(character('a').expect("A").commit().optional(), "syntax error in Test 'abortPreventsOptionalTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void abortPreventsRepeatTest() {
        assertFailure(character('a').expect("A").commit().repeat(), "syntax error in Test 'abortPreventsRepeatTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void guardPreservesExpectationTest() {
        assertFailure(character('a').expect("A").guard(), "syntax error in Test 'guardPreservesExpectationTest' at line 1 and column 1: unexpected token 'x', expected A", "xyz");
    }

    @Test
    public void guardPreservesJustificationTest() {
        assertFailure(character('a').expect("A").because("A is required").guard(), "syntax error in Test 'guardPreservesJustificationTest' at line 1 and column 1: unexpected token 'x', expected A, because A is required", "xyz");
    }

    @Test
    public void negationUnknownFailureTest() {
        assertFailure(character('a').not(), "syntax error in Test 'negationUnknownFailureTest' at line 1 and column 1: unexpected token 'a'", "abc");
    }

    @Test
    public void negationExpectedFailureTest() {
        assertFailure(character('a').not().expect("anything except A"), "syntax error in Test 'negationExpectedFailureTest' at line 1 and column 1: unexpected token 'a', expected anything except A", "abc");
    }

    @Test
    public void negationJustifiedFailureTest() {
        assertFailure(character('a').not().because("A is forbidden"), "syntax error in Test 'negationJustifiedFailureTest' at line 1 and column 1: unexpected token 'a', because A is forbidden", "abc");
    }

    @Test
    public void negationSuccessTest() {
        assertSuccess(character('a').not(), null, "xyz");
    }

}
