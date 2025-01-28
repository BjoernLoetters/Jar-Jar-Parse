package json;

import jcombinators.Parser;
import jcombinators.data.Tuple;
import jcombinators.input.Input;

import java.util.stream.Collectors;

import static jcombinators.Parser.lazy;
import static jcombinators.Parser.or;
import static jcombinators.common.StringParser.literal;
import static jcombinators.common.StringParser.regex;

public final class Example {

    private Example() {}

    public static Parser<JsonValue> jsonValue() {
        return lazy(() -> or(jsonNull, jsonNumber, jsonBoolean, jsonArray, jsonObject, jsonString));
    }

    private static final Parser<String> string = regex("\"([^\"\\\\]|\\\\.)*\"").map(string -> string.substring(1, string.length() - 1));

    public static final Parser<JsonNumber> jsonNumber = regex("[+-]?(?:\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")
        .map(Double::parseDouble).map(JsonNumber::new);

    public static final Parser<JsonBoolean> jsonBoolean = literal("true").map(ignore -> new JsonBoolean(true))
        .or(literal("false").map(ignore -> new JsonBoolean(false)));

    public static final Parser<JsonNull> jsonNull = literal("null").map(ignore -> new JsonNull());

    public static final Parser<JsonString> jsonString = string.map(JsonString::new);

    public static final Parser<JsonArray> jsonArray = jsonValue().separate(literal(",")).between(literal("["), literal("]"))
        .map(values -> values.toArray(new JsonValue[0])).map(JsonArray::new);

    private static final Parser<Tuple<String, JsonValue>> jsonObjectMember = string.keepLeft(literal(":")).and(jsonValue());

    public static final Parser<JsonObject> jsonObject = jsonObjectMember.separate(literal(",")).between(literal("{"), literal("}"))
        .map(tuples -> new JsonObject(tuples.stream().collect(Collectors.toMap(Tuple::first, Tuple::second))));

}
