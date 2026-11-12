package jjparse.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jjparse.Parsing.Failure;
import jjparse.Parsing.Parser;
import jjparse.input.Input;

/**
 * Represents a human-readable description of the {@link Expectation} associated with a parsing {@link Failure}.
 *
 * <p>An {@link Expectation} may be the {@link Description} of an expected {@link Input}, provide a {@link Justification}
 * for a {@link Failure}, or merge multiple alternative {@link Expectation}s to a {@link Union}.
 * {@link Unknown} {@link Expectation}s do not contribute to the generated diagnostic message.</p>
 *
 * <p>Expectations are immutable and can be associated with a {@link Parser} using {@link Parser#because(String)}
 * and {@link Parser#expect(String)}. Applying a new justification replaces an existing one, while merging expectations
 * discards their individual justifications.</p>
 *
 * @author Björn Lötters
 */
public sealed abstract class Expectation permits Justification, Description, Union, Unknown {

    /** A constant that represents an {@link Unknown} {@link Expectation}. */
    public static final Expectation unknown = new Unknown();

    /**
     * Creates an {@link Expectation} from a formatted description.
     *
     * @param expectation The format string describing the expectation.
     * @param objects The arguments referenced by the format string.
     * @return An {@link Expectation} containing the formatted description.
     */
    public static Expectation from(final String expectation, final Object ... objects) {
        return new Description(String.format(expectation, objects));
    }

    /** Whether this {@link Expectation} is {@link Unknown} or not. */
    public final boolean isUnknown() {
        return this instanceof Unknown;
    }

    /**
     * Associates an {@link Expectation} with a reason that justifies it.
     *
     * <p>If this expectation is already justified, the existing reason is replaced.</p>
     *
     * @param reason The reason for this {@link Expectation}.
     * @return A new {@link Expectation} that is justified by the provided reason.
     * @see Justification
     */
    public final Expectation because(final String reason) {
        return new Justification(this, reason);
    }

    /**
     * Combines this {@link Expectation} with another alternative {@link Expectation}.
     *
     * <p>The descriptions of both {@link Expectation}s are preserved in their original order, while any
     * associated {@link Justification}s are discarded. {@link Unknown} {@link Expectation}s do not contribute
     * to the resulting description.
     *
     * @param expectation The alternative {@link Expectation} to merge.
     * @return A new {@link Expectation} representing both alternatives.
     */
    public final Expectation merge(final Expectation expectation) {
        return new Union(this, expectation);
    }

    /**
     * Returns a human-readable description of this {@link Expectation}.
     *
     * <p>Starting with {@code "expected"}, alternative {@link Expectation}s are separated by commas, where the last
     * alternative is joined with {@code "or"}. An associated {@link Justification} is appended using {@code "because"}.
     * {@link Unknown} {@link Expectation}s are omitted from the description.
     *
     * @return The formatted {@link Expectation}, or an empty string if the {@link Expectation} is {@link Unknown}
     *         and no {@link Justification} is available.
     */
    @Override
    public final String toString() {
        final List<String> expectations = new ArrayList<>();
        final Optional<String> justification = collect(this, expectations);
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < expectations.size(); ++i) {
            final String expectation = expectations.get(i);

            if (i == expectations.size() - 1 && expectations.size() > 1) {
                result.append(", or ");
            } else if (i == 0) {
                result.append("expected ");
            } else {
                result.append(", ");
            }

            result.append(expectation);
        }

        if (justification.isPresent()) {
            if (!expectations.isEmpty()) {
                result.append(", ");
            }

            result.append("because ").append(justification.get());
        }

        return result.toString();
    }

    private static Optional<String> collect(final Expectation expectation, final List<String> expectations) {
        return switch (expectation) {
            case Union union -> {
                collect(union.left, expectations);
                collect(union.right, expectations);
                yield Optional.empty();
            }

            case Justification justification -> {
                collect(justification.expectation, expectations);
                yield Optional.of(justification.reason);
            }

            case Description description -> {
                expectations.add(description.expectation);
                yield Optional.empty();
            }

            case Unknown ignore -> Optional.empty();

        };
    }

}
