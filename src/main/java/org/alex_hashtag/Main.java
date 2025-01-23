package org.alex_hashtag;

import org.alex_hashtag.buildSystem.Rainforest;
import org.alex_hashtag.tokenization.TokenStream;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rainforest",
        version = "Rainforest 0.2",
        mixinStandardHelpOptions = true,
        description = "A command-line interface for the Toucan programming language."
)
public class Main implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main())
                .addSubcommand("parse", new ParseCommand())
                // Add more subcommands here, e.g., "build", "run", etc.
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("No command provided. Use --help for usage instructions.");
        return 0;
    }

    @CommandLine.Command(
            name = "parse",
            description = "Parses a directory (defaults to current directory) and tokenizes all .toucan files."
    )
    static class ParseCommand implements Callable<Integer> {

        @CommandLine.Parameters(
                index = "0",
                description = "The directory to parse (defaults to current directory).",
                defaultValue = "."
        )
        private Path directory;

        @Override
        public Integer call() {
            try {
                // Resolve directory and ensure it's a valid directory
                directory = directory.toAbsolutePath().normalize();
                if (!Files.isDirectory(directory)) {
                    System.out.println("Not a directory: " + directory);
                    return 1;
                }

                Path rainforestToml = directory.resolve("rainforest.toml");
                if (!Files.exists(rainforestToml)) {
                    System.out.println("No rainforest.toml found at: " + rainforestToml);
                    return 1;
                }

                // Build a Rainforest object
                var rainforest = new Rainforest(rainforestToml.toString());
                System.out.println("Parsed Rainforest data: " + rainforest);

                // Recursively find all .toucan files
                List<Path> toucanFiles = new ArrayList<>();
                Files.walk(directory)
                        .filter(p -> p.toString().endsWith(".toucan"))
                        .forEach(toucanFiles::add);

                // Tokenize each .toucan file
                for (Path file : toucanFiles) {
                    System.out.println("---- Parsing file: " + file);
                    try {
                        String source = Files.readString(file);
                        TokenStream ts = new TokenStream(source);
                        ts.printTokens();
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return 0;
            } catch (IOException e) {
                System.err.println("Error while processing the directory: " + e.getMessage());
                e.printStackTrace();
                return 1;
            }
        }
    }
}
