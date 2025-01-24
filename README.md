# Toucan Compiler

Welcome to the **Toucan Compiler** repository! Toucan is a statically typed, memory-safe programming language designed to prioritize readability, explicitness, and versatility. Inspired by modern programming languages, Toucan combines their best features into a cohesive and user-friendly design.

This repository hosts the Java-based implementation of the Toucan Compiler.

For detailed documentation and language specifics, visit the official [Toucan Wiki](https://toucan.wiki).

---

## Features of Toucan

### Language Highlights
- **Memory Safety**: Protects against common errors like null pointer dereferencing and buffer overflows.
- **Static Typing**: Types are determined at compile time, enhancing reliability and performance.
- **Explicit Mutability**: Variables are immutable by default, with explicit modifiers for mutable or const behavior.
- **Rich Type System**: Support for primitives, structs, enums, classes, unions, and more.
- **Traits and Generics**: Provides advanced polymorphism and code reuse capabilities.
- **Functional Programming Support**: Includes lambdas, inline functions, and functional expressions.
- **Error Management**: Features explicit, enum-based error handling, ensuring predictable and safer code execution.
- **Macros and Annotations**: Support for declarative macros and annotations for compile-time code transformations.
- **Low-Level Programming**: Unsafe blocks and direct system calls for advanced systems programming.
- **Modular Design**: Packages and namespaces for clean, scalable project structures.

---

## Compiler Features

### Current Progress
The compiler implementation is actively in development, with the following features already completed:

- **Tokenizer**: Breaks down Toucan source code into tokens for parsing.
- **Abstract Syntax Tree (AST)**: Internal representation of the code, supporting:
  - Expressions: Arithmetic, bitwise, boolean, and control structures.
  - Control Statements: `if`, `while`, `for`, `do-while`, and `switch`.
  - Function Definitions: Generics, annotations, and parameterized types.
  - Literals: Support for arrays, strings, integers, floats, and more.
- **Type System**: Robust type registry for built-in and custom types, including support for references and arrays.
- **Error Handling**: Precise tracking of errors for better debugging and IDE support.
- **Build System Integration**: Parses `rainforest.toml` files for project configuration.
- **Macros**: Declarative macros and annotations for metaprogramming.
- **Unsafe Contexts**: Handles unsafe expressions and function properties for low-level operations.

### Repository Structure
- **`buildSystem/`**: Manages project metadata and build configurations.
- **`errors/`**: Error reporting and management utilities.
- **`internal_representation/`**: Core compiler logic:
  - Expressions
  - Functions
  - Types
  - Macros
  - Literals
- **`tokenization/`**: Tokenizer and token management utilities.
- **`test_project/`**: Sample Toucan projects for testing.

---

## Getting Started

### Requirements
- **Java**: Version 11 or higher.
- **Gradle**: For build automation and native compilation.
- **GraalVM**: To enable native image generation.

### Build and Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Alex-Hashtag/ToucanCompilerJava.git
   cd ToucanCompilerJava
   ```

2. Build the compiler using Gradle:
   ```bash
   gradle nativeCompile
   ```

3. Locate the compiled executable:
    - The native executable will be located in the `build/native/nativeCompile/` directory.

4. Run the compiled project:
   ```bash
   ./build/native/nativeCompile/ToucanCompiler path/to/rainforest.toml
   ```

5. Pass arguments directly to the executable:
   ```bash
   ./build/native/nativeCompile/ToucanCompiler --help
   ```

---

## Roadmap

### Immediate Goals
- **Parser Implementation**: Convert tokens into a structured AST.
- **Semantic Analysis**: Implement type checking, scoping rules, and error detection.
- **Code Generation**: Translate AST into LLVM IR for high-performance native code.
- **Standard Library**: Develop a rich standard library for Toucan.

### Long-Term Goals
- **Integrated Development Environment (IDE)**: Add support for Toucan-specific features like syntax highlighting and LSP.
- **Comprehensive Testing**: Expand unit and integration tests across all components.
- **Optimizations**: Implement compiler optimization passes to improve performance.

---

## Contribution

Contributions are welcome! If you have ideas or suggestions, feel free to open an issue or submit a pull request.

For any questions or discussions, please contact the author at **alex_hashtag@toucan.wiki**.

---

Visit the [Toucan Wiki](https://toucan.wiki) for more details about the language and its ecosystem.
