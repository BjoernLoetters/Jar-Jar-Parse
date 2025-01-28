package jcombinators.input;

public final class InputWrapper implements CharSequence {

    private Input input;

    public InputWrapper(final Input input) {
        this.input = input;
    }

    @Override
    public int length() {
        return input.contents.length() - input.offset;
    }

    @Override
    public char charAt(final int index) {
        return input.contents.charAt(index + input.offset);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return input.contents.substring(start + input.offset, end + input.offset);
    }

}
