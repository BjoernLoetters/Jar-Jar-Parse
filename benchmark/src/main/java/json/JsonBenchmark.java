package json;

import jjparse.TextParsing;
import jjparse.data.Product;
import jjparse.input.Input;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(3)
public class JsonBenchmark extends TextParsing {

    private Parser<Object> parser;

    private Input<Integer> input;

    @Setup(Level.Trial)
    public void setup() {
        final Parser<String> jsonString = regex("\"([^\"\\\\]|\\\\.)*\"")
            .expect("a string")
            .map(string -> string.substring(1, string.length() - 1));

        final Parser<Double> jsonNumber = regex("[+-]?(?:\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")
            .expect("a number")
            .map(Double::parseDouble);

        final Parser<Boolean> jsonBoolean = choice(
            literal("true").map(ignore -> true),
            literal("false").map(ignore -> false)
        ).expect("a boolean");

        final Parser<Object> jsonNull = literal("null")
            .expect("null")
            .map(ignore -> null);

        final Parser<Object[]> jsonArray = lazy(() -> parser).separate(literal(",")).between(literal("["), literal("]").commit())
            .expect("an array")
            .map(values -> values.toArray(new Object[0]));

        final Parser<Product<String, Object>> jsonObjectMember = jsonString.expect("a property name")
            .andl(literal(":").expect("a property separator"))
            .and(lazy(() -> parser));

        final Parser<Object> jsonObject = jsonObjectMember.separate(literal(",")).between(literal("{"), literal("}").commit())
            .expect("an object")
            .map(tuples -> {
                Map<String, Object> result = new HashMap<>();
                tuples.forEach(tuple -> result.put(tuple.first(), tuple.second()));
                return result;
            });

        parser = choice(jsonNull, jsonNumber, jsonBoolean, jsonArray, jsonObject, jsonString);

        final long seed = 0xA5F294B01CAA9636L;
        input = Input.of("json benchmark", new JsonGenerator(seed).generate(100_000_000L));
    }

    @Benchmark
    public Object benchmark() {
        return parse(parser, input);
    }

}
