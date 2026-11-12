package jjparse.diagnostic;



/**
 * Represents an {@link Expectation} described by a human-readable {@link String}.
 *
 * <p>A {@link Description} is created using {@link Expectation#from(String, Object...)}
 * and contributes its description to the diagnostic message of an {@link Expectation}.</p>
 *
 * @author Björn Lötters
 */
final class Description extends Expectation {

    /** The human-readable description of this {@link Expectation}. */
    final String expectation;

    Description(final String expectation) {
        this.expectation = expectation;
    }

}
