package json;

import jjparse.TextParsing;
import jjparse.data.Product;

import java.util.stream.Collectors;

public final class Example extends TextParsing {

    private Example() {}

    public Parser<JsonValue> jsonValue() {
        return lazy(() -> choice(jsonNull, jsonNumber, jsonBoolean, jsonArray, jsonObject, jsonString));
    }

    private final Parser<String> string = regex("\"([^\"\\\\]|\\\\.)*\"")
        .expect("a string")
        .map(string -> string.substring(1, string.length() - 1));

    public final Parser<JsonNumber> jsonNumber = regex("[+-]?(?:\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")
        .expect("a number")
        .map(Double::parseDouble).map(JsonNumber::new);

    public final Parser<JsonBoolean> jsonBoolean = choice(
        literal("true").map(ignore -> new JsonBoolean(true)),
        literal("false").map(ignore -> new JsonBoolean(false))
    ).expect("a boolean");

    public final Parser<JsonNull> jsonNull = literal("null")
        .expect("null")
        .map(ignore -> new JsonNull());

    public final Parser<JsonString> jsonString = string.map(JsonString::new);

    public final Parser<JsonArray> jsonArray = jsonValue().separate(literal(",")).between(literal("["), literal("]").commit())
        .expect("an array")
        .map(values -> values.toArray(new JsonValue[0])).map(JsonArray::new);

    private final Parser<Product<String, JsonValue>> jsonObjectMember = string.expect("a property name")
        .andl(literal(":").expect("a property separator"))
        .and(jsonValue());

    public final Parser<JsonObject> jsonObject = jsonObjectMember.separate(literal(",")).between(literal("{"), literal("}").commit())
        .expect("an object")
        .map(tuples -> new JsonObject(tuples.stream().collect(Collectors.toMap(Product::first, Product::second))));

}
