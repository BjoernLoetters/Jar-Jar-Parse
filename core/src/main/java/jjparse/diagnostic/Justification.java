package jjparse.diagnostic;

/**
 * Represents an {@link Expectation} associated with a reason that justifies it.
 *
 * <p>A {@link Justification} is created using {@link Expectation#because(String)}.
 * If the given {@link Expectation} is already justified, its existing {@link Justification}
 * is replaced by the new one.</p>
 *
 * @author Björn Lötters
 */
final class Justification extends Expectation {

    /** The {@link Expectation} associated with this {@link Justification}. */
    final Expectation expectation;

    /** The human-readable reason that justifies this {@link Expectation}. */
    final String reason;

    Justification(final Expectation expectation, final String reason) {
        this.expectation = expectation instanceof Justification oldJustification ? oldJustification.expectation : expectation;
        this.reason = reason;
    }

}
