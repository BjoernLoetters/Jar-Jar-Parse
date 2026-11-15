package json;

import java.util.Random;

public final class JsonGenerator {

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final Random random;

    public JsonGenerator(final long seed) {
        this.random = new Random(seed);
    }

    public String generate(final long bytes) {
        final StringBuilder json = new StringBuilder();
        json.append('[');

        while (json.length() < bytes - 512) {
            final String value = generateValue(0);

            // Reserve space for the final padding string.
            if (json.length() + value.length() + 4 > bytes - 128) {
                break;
            }

            if (json.length() > 1) {
                json.append(',');
            }

            json.append(value);
        }

        // Fill the remaining space with random string data.
        int remaining = Math.toIntExact(bytes - json.length());
        if (remaining >= 4) {
            if (json.length() > 1) {
                json.append(',');
                remaining--;
            }

            json.append('"');
            remaining--;

            while (remaining > 2) {
                json.append(randomCharacter());
                remaining--;
            }

            json.append('"');
        }

        json.append(']');

        // Fill any remaining bytes with whitespace
        while (json.length() < bytes) {
            json.append(' ');
        }

        return json.toString();
    }

    private String generateValue(int depth) {
        if (depth >= 5) {
            return generatePrimitive();
        }

        return switch (random.nextInt(7)) {
            case 0 -> generateObject(depth + 1);
            case 1 -> generateArray(depth + 1);
            default -> generatePrimitive();
        };
    }

    private String generateObject(int depth) {
        StringBuilder result = new StringBuilder("{");
        int count = 1 + random.nextInt(8);

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append(',');
            }

            result.append('"').append(randomString(5 + random.nextInt(20)))
                .append("\":").append(generateValue(depth));
        }

        return result.append('}').toString();
    }

    private String generateArray(int depth) {
        StringBuilder result = new StringBuilder("[");
        int count = 1 + random.nextInt(10);

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append(',');
            }

            result.append(generateValue(depth));
        }

        return result.append(']').toString();
    }

    private String generatePrimitive() {
        return switch (random.nextInt(5)) {
            case 0 -> "\"" + randomString(5 + random.nextInt(80)) + "\"";
            case 1 -> Long.toString(random.nextLong());
            case 2 -> Double.toString(random.nextDouble() * random.nextInt(1_000_000));
            case 3 -> random.nextBoolean() ? "true" : "false";
            default -> "null";
        };
    }

    private String randomString(int length) {
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(randomCharacter());
        }

        return result.toString();
    }

    private char randomCharacter() {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }

}
