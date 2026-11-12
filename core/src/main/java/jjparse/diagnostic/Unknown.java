package jjparse.diagnostic;

/**
 * Represents an {@link Expectation} without a known description.
 *
 * <p>An {@link Unknown} {@link Expectation} does not contribute to the generated
 * diagnostic message unless it is associated with a {@link Justification}.</p>
 *
 * @author Björn Lötters
 */
final class Unknown extends Expectation {

    Unknown() { }

}
