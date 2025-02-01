# Contributing to Jar Jar Parse
*"Meesa so happy yousa wanna help! Together, weesa makin’ **Jar Jar Parse** bombad great! 🎉"*

First of all, thank you for your interest in contributing to **Jar Jar Parse**! This guide will help you get started with contributing to the project.

### How to Contribute

#### Reporting Issues

If you find a bug or have a feature request, please [open an issue](https://github.com/BjoernLoetters/Jar-Jar-Parse/issues) using one of the predefined templates, if applicable. 
In any case, when reporting a bug, please attach a clear concise description including:
1. Steps to reproduce the issue
2. Expected behavior
3. Actual behavior
4. Any relevant logs or error messages

#### Submitting Code Contributions

We welcome pull requests! To contribute code, follow these steps:

1. Fork the repository and create a new branch:
   ```
   git clone https://github.com/BjoernLoetters/Jar-Jar-Parse.git
   cd Jar-Jar-Parse
   git checkout -b feature-or-fix
   ```
2. Make sure your changes follow the [Coding Guidelines](#coding-guidelines).
3. Ensure all tests pass by running:
   ```
   mvn clean test
   ```
4. Add and commit your changes with a meaningful message:
   ```
   git commit -m "Add feature XYZ to enable users to do ABC"
   ```
5. Push your branch
   ```
   git push origin feature-or-fix
   ```
6. Open a pull request (PR) against the `main` branch and describe your changes

#### Coding Guidelines

- Follow the existing code style, even if this is not your preferred style!
- Keep changes focused and minimal.
- Write meaningful commit messages.
- Ensure new code is covered by tests.
- Document code, even if it is internal!

#### Testing

Before submitting a PR, make sure your changes pass all tests. You can run:

```
mvn clean test
```

If you add a new feature, consider adding unit tests to cover the functionality. 

#### Communication

If you have any questions, feel free to open a dicussion or comment on an issue. We appreciate your contributions and look forward to working together!

--- 

Happy coding! 🚀

