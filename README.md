![Unit Test](https://img.shields.io/github/actions/workflow/status/BjoernLoetters/Jar-Jar-Parse/test.yml?branch=main&label=Unit%20Test&logo=github&cacheSeconds=3600&color=light-green)
[![License](https://img.shields.io/github/license/BjoernLoetters/Jar-Jar-Parse?label=License&logo=github&color=light-green)](LICENSE)
[![Release](https://img.shields.io/github/v/release/BjoernLoetters/Java-Parser-Combinators?label=Release&logo=git&logoColor=white&cacheSeconds=3600)](https://github.com/BjoernLoetters/Jar-Jar-Parse/releases)
[![Maven](https://img.shields.io/maven-central/v/io.github.bjoernloetters/jjparse-core?label=Maven%20Central&logo=apache-maven&logoColor=white&cacheSeconds=3600)](https://central.sonatype.com/artifact/io.github.bjoernloetters/jjparse-core)
[![Documentation](https://img.shields.io/badge/docs-online-blue?color=teal&label=Documentation)](https://bjoernloetters.github.io/Jar-Jar-Parse/)
[![Javadoc](https://img.shields.io/badge/api-reference-green?color=teal&label=API)](https://bjoernloetters.github.io/Jar-Jar-Parse/javadoc/)

# Jar Jar Parse
*"Yousa needin’ a parser? Meesa help!"*

[📚 **Documentation**](https://bjoernloetters.github.io/Jar-Jar-Parse/)  
[📖 **API Reference**](https://bjoernloetters.github.io/Jar-Jar-Parse/javadoc/)

### 🚀 What is Jar Jar Parse?
**Jar Jar Parse** (or **JJParse** for short) is a lightweight library designed for the rapid prototyping of parsers in Java.
It is highly inspired by the [Scala Parser Combinators](https://github.com/scala/scala-parser-combinators) and aims to balance ease of use and functionality, deliberately placing less emphasis on performance.
If you are new to the idea of parser combinators feel free to check out the [Section "What are Parser Combinators?"](#what-are-parser-combinators).
See [Section "Example"](#example) for a quick overview and [Section "Getting Started"](#getting-started) to install and use this library.
If you find this project interesting, please consider [contributing](#contributing) or sharing it with others.

### 🎯 Features

- [x] Easy to use and fully typed ♥️
- [x] Supports backtracking by default 
- [x] Scanner-less parsing with regular expressions
- [x] Built-in support for skipping irrelevant tokens (e.g., whitespace)
- [x] 🌟 Unicode 🦄 support with code point positions
- [x] Abstract parsers and inputs for advanced use cases
- [x] Tested and documented ✅

### 📖 Getting Started

##### Installation 

**JJParse** is available via the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.bjoernloetters/jjparse-core), so installing it is as easy as pie!

###### Automatic Installation

If you are already using `maven`, just copy and paste the following dependency to your `pom.xml`: 
```xml
<dependency>
    <groupId>io.github.bjoernloetters</groupId>
    <artifactId>jjparse-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

Thanks to the widespread use of the Maven Central Repository, you can also include **JJParse** in other build tools:
<details>
   <summary>Gradle</summary>

   ```
   implementation group: 'io.github.bjoernloetters', name: 'jjparse-core', version: '2.0.0'
   ```
   
</details>
<details>
   <summary>SBT</summary>

   ```scala
    libraryDependencies += "io.github.bjoernloetters" % "jjparse-core" % "2.0.0"
   ```

</details>
<details>
   <summary>Ivy</summary>

   ```xml
   <dependency org="io.github.bjoernloetters" name="jjparse-core" rev="2.0.0"/>
   ```
   
</details>

###### Manual Installation

If the above options do not fit your needs, you can also install **JJParse** manually by downloading the latest `jar`-release via the [release page](https://github.com/BjoernLoetters/Java-Parser-Combinators/releases).
To install **JJParse** in this way, follow these steps:
1. Download the latest `jar`-file from the [release page](https://github.com/BjoernLoetters/Java-Parser-Combinators/releases).
2. Add the `jar`-file to your project's classpath.
    - In **IntelliJ IDEA**: Right-click the `jar`-file → Select **"Add as Library ..."**
    - In **Eclipse**: Right-click your project → **Build Path** → **Add External Archives ...** and select the `jar`-file.
3. (Optional) Add the JavaDoc `jar`-file to your development environment.  

##### First Steps

To start using **JJParse**, create a class that extends either `Parsing` or `TextParsing`:
- `Parsing` offers the core functionality and allows defining parsers for **arbitrary input types** (e.g., a custom token stream provided by a scanner).
- `TextParsing` is the best choice if you simply want to prototype a language. It provides utilities for **codepoint-based and scanner-less parsing** (including regular expressions). 

In general, a parser in **JJParse** is simply a function that transforms an `Input<T>` into a `Result<U>`. 
Here, an instance of `Input<T>` can be seen as a stream of tokens of type `T`.
In case of `TextParsing`, this type `T` is fixed to be `Integer`, while for `Parsing` it is up to you to choose an appropriate token type. 
The `Result<U>` can either be a `Success<U>`, containing the parsed value of type `U`, or a `Failure<U>`, carrying an error message with further details.

To parse an input, instantiate your parser class and call `parse(parser, input)` (to parse the whole input) or `parser.apply(input)` (to parse only a portion of the input).
For a quick start, also have a look at the [Section "Example"](#example).

### 🔍 Example

The example below provides a brief overview of the library. For a more detailed example, check out the subprojects in the [examples directory](examples).

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

### 💡 What are Parser Combinators?

In traditional parsing approaches, commonly taught in university courses, we often rely on parser generators. 
These tools take a grammar specification as input and generate a parser implementation based on it. 
While the generated parsers are typically fast, this method has some downsides.

Firstly, modifying the grammar requires regenerating the entire parser, which can be cumbersome. 
Additionally, to produce meaningful output with your parser, you must embed code from your programming language within the grammar specification itself. 
The tool support for such embedded code can sometimes be difficult to work with and frustrating to debug.

This is where parser combinators come into play. 
A parser combinator is a powerful technique for constructing complex parsers by combining simpler, reusable parsers. 
Each individual parser is responsible for recognizing a specific part of the input, and combinators are functions that combine these basic parsers into more complex ones — hence the name "combinators".

The key advantage of this approach is that it enables the rapid development of parsers within the constraints and benefits of a general-purpose programming language. 
Instead of relying on a third-party tool to generate a parser from a grammar specification, parser combinators allow us to "program" the syntax of a language directly in our favourite programming language.

In the case of **JJParse**, parsers are implemented as functions that take an `Input<T>` and produce a `Result<U>`. 
Here ...
- ... the type `T` represents the element type of the input, which is `Character` in many cases.
- ... the type `U` denotes the type of the result.

For example:
- A parser of Unicode codepoints takes an `Input<Integer>` and produces a `Result<Integer>`.
- A Unicode text parser takes an `Input<Integer>` and produces a `Result<String>`.
- A parser that combines a Unicode codepoint parser with a string parser produces a `Result<Product<Integer, String>>`.

As with any parsing method, things don't always go smoothly. 
A parsing attempt can fail, which is why a `Result<U>` may either be a `Success<U>` (containing the successful result) or a `Failure<U>` (containing an error message).

This approach, while simple, provides flexibility and expressiveness that traditional parser generators often lack, making it an ideal solution for day-to-day parsing tasks.

## Documentation

The full documentation is available at:

- Getting started, user guide & examples: https://bjoernloetters.github.io/Jar-Jar-Parse/
- API reference (Javadoc): https://bjoernloetters.github.io/Jar-Jar-Parse/javadoc/

### 🤝 Contributing

Contributions of any kind are welcome! 
Whether it’s reporting issues, suggesting features, or submitting pull requests, your help is appreciated. 
If you find this library useful, consider sharing it with others.

📚 **Help wanted**: While the project already has its own GitHub Pages documentation site, there is still plenty of work to be done! In particular, we need contributors to help fill the documentation with practical examples, tutorials, and guides that make the library more accessible to new users.
Writing comprehensive documentation is a lot of work, but it is also a great opportunity to contribute without diving into the library's internals too much. Together, we can turn a great project into an even better one 🚀!

### ⚖️ License

This project is licensed under the [MIT](LICENSE) license.