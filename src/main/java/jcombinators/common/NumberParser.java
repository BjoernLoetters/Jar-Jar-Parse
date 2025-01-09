package jcombinators.common;

import jcombinators.Parser;

import java.math.BigInteger;

import static jcombinators.common.StringParser.*;

public final class NumberParser {

    private NumberParser() {

    }

    public static Parser<Integer> digit = regex("[0-9]").map(Integer::parseInt);

    public static Parser<Integer> number = regex("[0-9]+").map(Integer::parseInt);

    public static Parser<Integer> int32 = regex("[+-]?[0-9]+").map(Integer::parseInt);

    public static Parser<Long> int64 = regex("[+-]?[0-9]+").map(Long::parseLong);

    public static Parser<BigInteger> integer = regex("[+-]?[0-9]+").map(BigInteger::new);

    public static Parser<Float> float32 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Float::parseFloat);

    public static Parser<Double> float64 = regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").map(Double::parseDouble);

}
