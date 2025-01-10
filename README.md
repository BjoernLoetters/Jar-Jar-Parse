![Test Status](https://github.com/BjoernLoetters/Java-Parser-Combinators/actions/workflows/test.yml/badge.svg?branch=main)
![Test Status](https://img.shields.io/github/v/release/BjoernLoetters/Java-Parser-Combinators?label=Release&logo=github)

# Java Parser Combinators

The Java Parser Combinators library provides parser combinators as they are known from functional languages like Scala or Haskell.

More information will follow in the near future.

### Example

```java
import static jcombinators.common.StringParser.*;

public class MyParser {

    // A parser which parses an integer.
    public static Parser<Integer> number = regex("[+-]?[0-9]+").map(Integer::parseInt);

    // A parser which parses additions.
    public static Parser<Integer> add = number.keepLeft(character('+')).and(number)
            .map(tuple -> tuple.first() + tuple.second());

    public static void main(final String[] arguments) {
        // Create an input with the name 'My Test Input'.
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

This example outputs `Success: 42`. It makes use of the `keepLeft` and `and` combinators to concatenate a number with the `+` sign and another number. 
While `keepLeft` and `and` behave very similar, `keepLeft` only returns the result of the first parser and drops the result of the second one upon success.
