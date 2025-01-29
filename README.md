![Test Status](https://github.com/BjoernLoetters/Java-Parser-Combinators/actions/workflows/test.yml/badge.svg?branch=main)
![Test Status](https://img.shields.io/github/v/release/BjoernLoetters/Java-Parser-Combinators?label=Release&logo=github)

# Java Parser Combinators

**Java Parser Combinators** is a lightweight library designed for the rapid prototyping of parsers in Java.
It aims to balance ease of use and functionality, deliberately placing less emphasis on performance (even though first tests show that we can parse ~ 130k lines of JSON data in around 1 second on a recent M3 chip).

### Parser Combinators

In general, parser combinators are a powerful technique for constructing complex parsers by composing smaller, reusable parsers. 
Each parser is responsible for recognizing a specific part of the input, and combinators are functions that combine these basic parsers into more complex ones (hence the name). 
This approach allows the rapid development of parsers with all the benefits of ordinary program code.
So, instead of generating a parser using a third party tool and a grammar specification, we "program" the syntax of our language in our primary programming language. 

In Java Parser Combinators, parsers are implemented as functions: 
They take an `Input` (which is basically a `String`) and produce a `Result<T>`.
Here, the type `T` indicates the type of the result. 
For example:
- A character parser takes an `Input` and produces a `Result<Character>`
- A string parser takes an `Input` and produces a `Result<String>`
- A parser which concatenates a character with a string parser produces a `Result<Tuple<Character, String>>` 

Of course, a parse may also fail. 
For this reason, a `Result<T>` may either be a `Success<T>` (containing the desired result) or a `Failure<T>` (containing an error message). 
Since parsers are just functions, we may also implement a primitive parser using the lambda notation:

```
Parser<Void> nothing = input -> new Success<>(null, input);
```

### Getting Started

##### Installation

At the moment, Java Parser Combinators are only available as a `jar`-release via the [release page](https://github.com/BjoernLoetters/Java-Parser-Combinators/releases).
In the near future, they will also be available as a maven package. 
So, to get started just download the latest `jar`-release and add it to the classpath of your project. 
In IntelliJ this can be done by right-clicking on the `jar`-file and selecting `Add as library ...`.

##### Usage Example

```java
import static jcombinators.common.StringParser.*;

public class MyParser {

    // A parser which parses an integer.
    public static Parser<Integer> number = regExp("[+-]?[0-9]+").map(Integer::parseInt);

    // A parser which parses additions.
    public static Parser<Integer> add = number.andl(character('+')).and(number)
            .map(tuple -> tuple.first() + tuple.second());

    public static void main(final String[] arguments) {
        // Create an input with the name 'My Test Input' (for error reporting).
        Input input = Input.of("My Test Input", "42+0");

        // Use the 'add' parser to parse the above input. Note how a parser is
        // just a function that takes an input and returns a parse result.
        Result<Integer> result = add.apply(input);

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

### Contributing

Contributions of any kind are welcome! 
Whether it’s reporting issues, suggesting features, or submitting pull requests, your help is appreciated. 
If you find this library useful, consider sharing it with others.

### License

This project is licensed under the [MIT](LICENSE) license.