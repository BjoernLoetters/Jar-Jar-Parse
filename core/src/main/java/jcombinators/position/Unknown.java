package jcombinators.position;

import jcombinators.input.Input;

public final class Unknown extends Position {

    public Unknown() {
        super(Input.of("<unknown>", ""), -1, -1);
    }

    @Override
    public String toString() {
        return "unknown input";
    }

}
