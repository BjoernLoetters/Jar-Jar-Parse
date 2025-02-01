![Unit Test](https://github.com/BjoernLoetters/Java-Parser-Combinators/actions/workflows/test.yml/badge.svg?branch=main)
![Release](https://img.shields.io/github/v/release/BjoernLoetters/Java-Parser-Combinators?label=Release&logo=github)
![License](https://img.shields.io/github/license/BjoernLoetters/Jar-Jar-Parse?label=License)

# Jar Jar Parse
*"Yousa needin’ a parser? Meesa help!"*

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
- [x] Built-in support for whitespace skipping
- [x] 🌟 Unicode 🦄 support with code point positions
- [x] Abstract parsers and inputs for advanced use cases
- [x] Tested and documented ✅

### 📖 Getting Started

##### Installation 

At the moment, **JJParse** is only available as a `jar`-release via the [release page](https://github.com/BjoernLoetters/Java-Parser-Combinators/releases).
In the near future, it will also be available as a maven package, so stay tuned!

To install **JJParse**, follow these steps:
1. Download the latest `jar` file from the [release page](https://github.com/BjoernLoetters/Java-Parser-Combinators/releases).
2. Add the `jar` file to your project's classpath.
    - In **IntelliJ IDEA**: Right-click the `jar` file → Select **"Add as Library ..."**
    - In **Eclipse**: Right-click your project → **Build Path** → **Add External Archives ...** and select the `jar` file.
    - If using **Gradle** or **Maven**, you can manually place the `jar` in a `libs/` directory and reference it in your configuration.
3. (Optional) Add the JavaDoc `jar` file to your environment.

##### First Steps

To start using **JJParse**, create a class that extends either `Parsing` or `StringParsing`:
- `Parsing` offers the core functionality and allows defining parsers for **arbitrary input types** (e.g., a custom token stream provided by a scanner).
- `StringParsing` is the best choice if you simply want to prototype a language. It provides utilities for **character-based and scanner-less parsing** (including regular expressions) and already skips any white space characters by default. 

In general, a parser in **JJParse** is simply a function that transforms an `Input<T>` into a `Result<U>`. 
Here, an instance of `Input<T>` can be seen as a stream of tokens of type `T`.
In case of `StringParsing`, this type `T` is fixed to be `Character`, while for `Parsing` it is up to you to choose an appropriate token type. 
The `Result<U>` can either be a `Success<U>`, containing the parsed value of type `U`, or a `Failure<U>`, carrying an error message with further details.

To parse an input, instantiate your parser class and call `parse(parser, input)` (to parse the whole input) or `parser.apply(input)` (to parse only a portion of the input).
For a quick start, also have a look at the [Section "Example"](#example).

### 🔍 Example

The example below provides a brief overview of the library. For a more detailed example, check out the subprojects in the [examples directory](examples).

```java
import jjparse.StringParsing;
import jjparse.input.Input;

// Extend 'StringParsing' for convenient character-based parsers.
public class MyParser extends StringParsing {

    // A parser which parses a signed integer.
    public Parser<Integer> number = regex("[+-]?[0-9]+").map(Integer::parseInt);

    // A parser which parses additions and returns their result.
    public Parser<Integer> add = number.keepLeft(character('+')).and(number)
            .map(product -> product.first() + product.second());

    public static void main(final String[] arguments) {
        // Create an input of characters with the name 'My Test Input' (for error reporting).
        Input<Character> input = Input.of("My Test Input", "42 + 0");

        // Use the 'add' parser to parse the above input.
        MyParser parser = new MyParser();
        Result<Integer> result = parser.parse(parser.add, input);

        // The following statement prints 'Success: 42'.
        switch (result) {
            case Success<Integer> success:
                System.out.printf("Success: %s\n", success.value);
                break;
            case Failure<Integer> failure:
                System.err.printf("Failure: %s\n", failure.message);
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
- A character parser takes an `Input<Character>` and produces a `Result<Character>`.
- A string parser takes an `Input<Character>` and produces a `Result<String>`.
- A parser that combines a character parser with a string parser produces a `Result<Tuple<Character, String>>`.

As with any parsing method, things don’t always go smoothly. 
A parsing attempt can fail, which is why a `Result<U>` may either be a `Success<U>` (containing the successful result) or a `Failure<U>` (containing an error message).

This approach, while simple, provides flexibility and expressiveness that traditional parser generators often lack, making it an ideal solution for day-to-day parsing tasks.

### 🤝 Contributing

Contributions of any kind are welcome! 
Whether it’s reporting issues, suggesting features, or submitting pull requests, your help is appreciated. 
If you find this library useful, consider sharing it with others.

### ⚖️ License

This project is licensed under the [MIT](LICENSE) license.