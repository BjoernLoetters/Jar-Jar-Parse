package jpc.common;

import jpc.Parser;
import jpc.parsers.LiteralParser;
import jpc.parsers.RegexParser;

public final class StringParser {

    private StringParser() {

    }

    public static Parser<String> literal(final String literal) {
        return new LiteralParser(literal);
    }

    public static Parser<String> regex(final String pattern) {
        return new RegexParser(pattern);
    }

    public static Parser<Character> character(final char character) {
        return literal("" + character).map(c -> c.charAt(0));
    }

    public static final Parser<Character> lowercase = regex("[a-z]").map(character -> character.charAt(0));

    public static final Parser<Character> uppercase = regex("[A-Z]").map(character -> character.charAt(0));

}
