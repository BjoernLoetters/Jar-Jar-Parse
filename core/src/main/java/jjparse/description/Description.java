package jjparse.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jjparse.Parsing.Parser;
import jjparse.Parsing.Failure;

/**
 * The base class for all kinds of {@link Parser} {@link Description}s.
 * <br/><br/>
 * A {@link Description} describes the expectation of a {@link Parser} and can be used to generate more informative
 * error messages in case of a {@link Failure}. The following kinds of {@link Description}s are supported:
 * <ul>
 *     <li>{@link Literal}: A {@link Description} describing that a specific {@link String} literal was expected.</li>
 *     <li>{@link RegExp}: A {@link Description} describing that a specific {@link Pattern} was expected.</li>
 *     <li>{@link Choice}: A {@link Description} that represents a choice between other {@link Description}s.</li>
 *     <li>{@link Sequence}: A {@link Description} that represents a sequence of other {@link Description}s.</li>
 *     <li>{@link Negation}: A {@link Description} that negates another {@link Description}.</li>
 *     <li>{@link Empty}: An empty {@link Description} that does not provide further information.</li>
 * </ul>
 *
 * @see Choice
 * @see Literal
 * @see Negation
 * @see RegExp
 * @see Sequence
 * @see Empty
 * @see Parser
 * @see Parser#description
 *
 * @author Björn Lötters
 */
public sealed abstract class Description permits CharacterClass, CharacterRange, Choice, Empty, Literal, Negation, RegExp, Sequence {

    /**
     * Construct a new {@link Description}.
     */
    public Description() { }

    /**
     * Produces a {@link String} that explains what a corresponding {@link Parser} would expect according to this
     * {@link Description}.
     * @return An {@link Optional} {@link String} where {@link Optional#empty()} is returned in case this
     *         {@link Description} is {@link Empty}.
     */
    public abstract Optional<String> describe();

    /**
     * Normalizes this {@link Description} such that {@link Negation}s are propagated downwards to the individual
     * {@link Literal} and {@link RegExp} {@link Description}s. Moreover, {@link Choice}s of {@link RegExp}s are
     * summarized into a single {@link RegExp}.
     * @return The normalized {@link Description}.
     */
    public final Description normalize() {
        return normalize(this, false);
    }

    /**
     * Normalizes the provided {@link Description} such that {@link Negation}s are propagated downwards to the individual
     * {@link Literal} and {@link RegExp} {@link Description}s. Moreover, {@link Choice}s of {@link RegExp}s are
     * summarized into a single {@link RegExp}.
     * @param description The {@link Description} that shall be normalized.
     * @param negate Whether the provided {@link Description} should be negated or not.
     * @return The normalized {@link Description}.
     */
    private static Description normalize(final Description description, final boolean negate) {
        switch (description) {
            case Choice choice: {
                final List<Description> descriptions = collect(choice, new ArrayList<>()).stream().map(element -> normalize(element, negate)).toList();
                if (negate) {
                    return normalize(new Sequence(descriptions), false);
                } else if (descriptions.stream().allMatch(element -> element instanceof RegExp)) {
                    final Pattern composition = Pattern.compile(descriptions.stream()
                        .map(element -> ((RegExp) element).pattern.pattern())
                        .map(pattern -> "(" + pattern + ")")
                        .collect(Collectors.joining("|"))
                    );
                    return new RegExp(composition);
                } else {
                    return new Choice(descriptions);
                }
            }

            case Sequence sequence: {
                final List<Description> descriptions = collect(sequence, new ArrayList<>()).stream().map(element -> normalize(element, negate)).toList();
                if (negate) {
                    return normalize(new Choice(descriptions), false);
                } else {
                    return new Sequence(descriptions);
                }
            }

            case Literal literal:
                return negate ? new Negation(literal) : literal;

            case CharacterRange characterRange:
                return negate ? new Negation(characterRange) : characterRange;

            case CharacterClass characterClass:
                return negate ? new Negation(characterClass) : characterClass;

            case RegExp regExp:
                return negate ? new Negation(regExp) : regExp;

            case Negation negation:
                return normalize(negation.description, !negate);

            case Empty empty:
                return empty;
        }
    }

    /**
     * Collects nested {@link Choice}s into a single {@link List} of {@link Description}s.
     * @param choice The {@link Choice} whose {@link Description}s should be collected.
     * @param alternatives The {@link List} to which the individual {@link Description}s should be added.
     * @return The provided {@link List}.
     */
    private static List<Description> collect(final Choice choice, final List<Description> alternatives) {
        for (final Description alternative : choice.alternatives) {
            if (alternative instanceof Choice other) {
                collect(other, alternatives);
            } else {
                alternatives.add(alternative);
            }
        }

        return alternatives;
    }

    /**
     * Collects nested {@link Sequence}s into a single {@link List} of {@link Description}s.
     * @param sequence The {@link Sequence} whose {@link Description}s should be collected.
     * @param elements The {@link List} to which the individual {@link Description}s should be added.
     * @return The provided {@link List}.
     */
    private static List<Description> collect(final Sequence sequence, final List<Description> elements) {
        for (final Description element : sequence.elements) {
            if (element instanceof Sequence other) {
                collect(other, elements);
            } else {
                elements.add(element);
            }
        }

        return elements;
    }

    /**
     * Creates a new {@link Description} by creating a {@link Sequence} from {@code this} and the provided {@link Description}.
     * @param description The second {@link Description}.
     * @return A {@link Sequence} {@link Description} containing {@code this} and the provided {@link Description} in this order.
     */
    public final Description and(final Description description) {
        return new Sequence(List.of(this, description));
    }

    /**
     * Creates a new {@link Description} by creating a {@link Choice} from {@code this} and the provided {@link Description}.
     * @param alternative The alternative {@link Description}.
     * @return A {@link Choice} {@link Description} containing {@code this} and the provided {@link Description} in this order.
     */
    public final Description or(final Description alternative) {
        return new Choice(List.of(this, alternative));
    }

    /**
     * Creates a new {@link Description} by creating a {@link Negation} from {@code this} {@link Description}.
     * @return A {@link Negation} of {@code this} {@link Description}.
     */
    public final Description negate() {
        return new Negation(this);
    }

}
