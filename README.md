# Toucan Compiler

Welcome to the **Toucan Compiler** repository! Toucan is a statically typed, memory-safe programming language designed with a focus on readability, explicitness, and versatility. Its design draws inspiration from various modern programming languages, aiming to combine their best features in a cohesive, developer-friendly manner.

For detailed documentation on Toucan, visit our website: [Toucan Wiki](https://toucan.wiki).

---

## Features of Toucan

### Language Highlights
- **Memory Safety**: Prevents common programming errors like null pointer dereferencing and buffer overflows.
- **Static Typing**: All types are determined at compile time, enhancing performance and reliability.
- **Explicit Mutability**: Variables are immutable by default, promoting safer programming practices.
- **Rich Type System**: Includes primitives, enums, structs, classes, unions, and more.
- **Operator Overloading**: Custom operators for user-defined types.
- **Traits and Generics**: Enables powerful polymorphism and code reuse.
- **Macros and Annotations**: Support for declarative macros and custom compiler behavior extensions.
- **Error Management**: Explicit error handling using enums for safer and more predictable outcomes.
- **Systems Programming**: Unsafe blocks for low-level memory management and system calls.
- **Functional Programming Support**: Lambdas, const functions, and functional-style expressions.
- **Modular Design**: Packages, namespaces, and import/export systems for better code organization.

---

## Compiler Features

This repository contains the Java-based implementation of the Toucan Compiler. Here's what has been implemented so far:

### Compiler Components
- **Tokenization**: A tokenizer that breaks down Toucan source code into tokens for further parsing.
- **Abstract Syntax Tree (AST)**: A comprehensive internal representation of Toucan code, supporting:
  - Arithmetic, bitwise, boolean, and comparison expressions.
  - Control structures (`if`, `while`, `for`, `do-while`, `switch`).
  - Function calls and declarations with generics and annotations.
  - Variable and constant declarations.
  - Literal support for arrays, strings, numbers, and more.
- **Error Management**: Precise location tracking of errors for better feedback and integration with LSPs.
- **Type System**: A robust type registry with support for custom types, generics, and type annotations.
- **Build System Integration**: Reads and processes `rainforest.toml` files for project metadata, dependencies, and build configurations.
- **Macro System**: Declarative macros and annotations for compile-time code generation and metadata manipulation.
- **Unsafe Contexts**: Explicit unsafe expressions and function properties for low-level operations.

### Repository Structure
- **`buildSystem/`**: Manages project metadata and build configurations.
- **`errors/`**: Error management utilities and reporting.
- **`internal_representation/`**: Core compiler logic, including expressions, functions, types, macros, and literals.
- **`tokenization/`**: Lexer and token management.
- **`test_project/`**: Example Toucan projects for integration testing.

---

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-repo>.git
   cd ToucanCompiler
   ```

2. Build the compiler using Maven:
   ```bash
   mvn clean install
   ```

3. Compile a Toucan project:
   ```bash
   java -jar target/ToucanCompiler.jar rainforest.toml
   ```

4. Run the compiled program:
   ```bash
   ./build/output
   ```

---

## Roadmap

- **Parser Implementation**: Transform the token stream into the AST.
- **Semantic Analysis**: Enforce type checking, scoping rules, and function resolution.
- **Code Generation**: Translate the AST into LLVM IR for efficient compilation to native code.
- **Standard Library**: Develop a rich standard library for common tasks.
- **Testing Suite**: Comprehensive unit and integration tests for the compiler.

---

## Contributing

Contributions are welcome! Please submit issues or pull requests if you have ideas or improvements.

---

For more information, visit the [Toucan Wiki](https://toucan.wiki).
