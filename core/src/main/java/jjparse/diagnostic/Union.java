package jjparse.diagnostic;

/**

 * Represents the {@link Union} of two alternative {@link Expectation}s.
 *
 * <p>A {@link Union} is created using {@link Expectation#merge(Expectation)}.
 * The descriptions of both {@link Expectation}s are preserved in their original order,
 * while any associated {@link Justification}s are discarded.</p>
 *
 * @author Björn Lötters
 */
final class Union extends Expectation {

    /** The left alternative {@link Expectation} of this {@link Union}. */
    final Expectation left;

    /** The right alternative {@link Expectation} of this {@link Union}. */
    final Expectation right;

    Union(final Expectation left, final Expectation right) {
        this.left = left instanceof Justification justification ? justification.expectation : left;
        this.right = right instanceof Justification justification ? justification.expectation : right;
    }

}
