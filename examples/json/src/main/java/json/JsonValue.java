package json;

public sealed interface JsonValue permits JsonObject, JsonString, JsonArray, JsonNull, JsonNumber, JsonBoolean {


}
