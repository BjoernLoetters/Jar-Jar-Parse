package jcombinators.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed abstract class Description permits Choice, Literal, Negation, Regex, Sequence, Unknown {

    public abstract Optional<String> describe();

    public final Description normalize() {
        return normalize(this, false);
    }

    private static Description normalize(final Description description, final boolean negate) {
        switch (description) {
            case Choice choice: {
                final List<Description> descriptions = collect(choice, new ArrayList<>()).stream().map(element -> normalize(element, negate)).toList();
                if (negate) {
                    return normalize(new Sequence(descriptions), false);
                } else if (descriptions.stream().allMatch(element -> element instanceof Regex)) {
                    final Pattern composition = Pattern.compile(descriptions.stream()
                        .map(element -> ((Regex) element).pattern.pattern())
                        .map(pattern -> "(" + pattern + ")")
                        .collect(Collectors.joining("|"))
                    );
                    return new Regex(composition);
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

            case Regex regex:
                return negate ? new Negation(regex) : regex;

            case Negation negation:
                return normalize(negation.description, !negate);

            case Unknown unknown:
                return unknown;
        }
    }

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

    public final Description and(final Description description) {
        return new Sequence(List.of(this, description));
    }

    public final Description or(final Description alternative) {
        return new Choice(List.of(this, alternative));
    }

    public final Description negate() {
        return new Negation(this);
    }

}
