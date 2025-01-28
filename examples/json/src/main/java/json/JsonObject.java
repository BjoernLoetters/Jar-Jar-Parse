package json;

import java.util.Map;

public record JsonObject(Map<String, JsonValue> values) implements JsonValue {
}
