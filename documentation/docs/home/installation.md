# Installation

Jar Jar Parse (or **JJParse**) is published on Maven Central and can be used with any modern Java build tool.  

The library targets **Java 21**.

This page shows how to add the dependency to your project and how to verify that everything works.

---

## Requirements

- Java 21 or later
- A build tool such as Maven or Gradle

---

## Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.bjoernloetters</groupId>
    <artifactId>jjparse-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

You can always find the latest version on Maven Central or on the project's GitHub releases page.

---

## Gradle

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.bjoernloetters:jjparse-core:2.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.bjoernloetters:jjparse-core:2.0.0'
}
```

---

## Other Build Tools

Because JJParse is published to Maven Central, it can also be used with other JVM build tools.

### SBT

```scala
libraryDependencies += "io.github.bjoernloetters" % "jjparse-core" % "2.0.0"
```

### Ivy

```xml
<dependency org="io.github.bjoernloetters" name="jjparse-core" rev="2.0.0"/>
```

---

## Manual Installation

If you prefer not to use a dependency manager, you can install JJParse manually:

1. Download the latest `jar` from the GitHub releases page.
2. Add the `jar` file to your project's classpath. For example:
    - **IntelliJ IDEA**: Right-click the `jar` → **Add as Library ...**
    - **Eclipse**: Right-click the project → **Build Path** → **Add External Archives ...** and select the `jar`.
3. (Optional) Add the JavaDoc `jar` to your IDE to enable API documentation in code completion.

---

## Verify the Installation

To verify that JJParse is on the classpath and working, create a small Java class:

```java
import jjparse.TextParsing;
import jjparse.input.Input;

// Extend 'TextParsing' for convenient parsing of Unicode text.
public class MyParser extends TextParsing {

    // A parser which parses a signed integer.
    public Parser<Integer> number = regex("[+-]?[0-9]+").map(Integer::parseInt);

    // A parser which parses additions and returns their result.
    public Parser<Integer> add = number.andl(character('+')).and(number)
            .map(product -> product.first() + product.second());

    public static void main(final String[] arguments) {
        // Create an instance of our parsing context.
        MyParser parser = new MyParser();

        // Create an input of Unicode codepoints with the name 'My Test Input' (for error reporting).
        // Additionally, skip any kind of Unicode whitespace. 
        Input<Integer> input = Input.of("My Test Input", "42 + 0").withFilter(parser.whitespace);

        // Use the 'add' parser to parse the above input.
        Result<Integer> result = parser.parse(parser.add, input);

        // The following statement prints 'Success: 42'.
        switch (result) {
            case Success<Integer> success:
                System.out.printf("Success: %s\n", success.value);
                break;
            case Failure<Integer> failure:
                System.err.printf("Failure: %s\n", failure.format());
                break;
        }
    }

}
```

If this compiles and prints `Success: 42` when you run it, the installation is complete.

---

## Next Steps

Once the library is available in your build:

- Continue with **Getting Started** to build your first real parser.
- Read the **Tutorial** for a conceptual overview and step-by-step guidance.
- Browse the **API Reference** for detailed information about the available parsers and combinators.
