package json;

import jcombinators.StringParsing;
import jcombinators.data.Tuple;
import jcombinators.input.Input;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public final class Example extends StringParsing {

    private Example() {}

    public Parser<JsonValue> jsonValue() {
        return lazy(() -> choice(jsonNull, jsonNumber, jsonBoolean, jsonArray, jsonObject, jsonString));
    }

    private final Parser<String> string = regex("\"([^\"\\\\]|\\\\.)*\"").map(string -> string.substring(1, string.length() - 1));

    public final Parser<JsonNumber> jsonNumber = regex("[+-]?(?:\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")
        .map(Double::parseDouble).map(JsonNumber::new);

    public final Parser<JsonBoolean> jsonBoolean = literal("true").map(ignore -> new JsonBoolean(true))
        .or(literal("false").map(ignore -> new JsonBoolean(false)));

    public final Parser<JsonNull> jsonNull = literal("null").map(ignore -> new JsonNull());

    public final Parser<JsonString> jsonString = string.map(JsonString::new);

    public final Parser<JsonArray> jsonArray = jsonValue().separate(literal(",")).between(literal("["), literal("]"))
        .map(values -> values.toArray(new JsonValue[0])).map(JsonArray::new);

    private final Parser<Tuple<String, JsonValue>> jsonObjectMember = string.keepLeft(literal(":")).and(jsonValue());

    public final Parser<JsonObject> jsonObject = jsonObjectMember.separate(literal(",")).between(literal("{"), literal("}"))
        .map(tuples -> new JsonObject(tuples.stream().collect(Collectors.toMap(Tuple::first, Tuple::second))));

}
