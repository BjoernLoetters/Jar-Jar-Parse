package jpc.common;

import jpc.Parser;
import jpc.parsers.RegexParser;

public final class StringParser {

    private StringParser() {

    }

    public static Parser<String> regex(final String pattern) {
        return new RegexParser(pattern);
    }

}
