package org.alex_hashtag;

import org.alex_hashtag.tokenization.TokenStream;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


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
            description = "Parses .toucan files in directories or specific files (default: current directory)."
    )
    static class ParseCommand implements Callable<Integer> {

        @CommandLine.Parameters(
                arity = "0..*",
                description = "Directories or files to parse (default: current directory)."
        )
        private List<Path> paths = List.of(Paths.get("."));

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Path to save tokenized output (default: print to console)."
        )
        private Path outputFile;

        @CommandLine.Option(
                names = {"-r", "--recursive"},
                description = "Recursively parse subdirectories (default: true).",
                defaultValue = "true"
        )
        private boolean recursive;

        @CommandLine.Option(
                names = {"--dry-run"},
                description = "List files to be parsed without tokenizing."
        )
        private boolean dryRun;

        @CommandLine.Option(
                names = {"-v", "--verbose"},
                description = "Enable verbose logging."
        )
        private boolean verbose;

        @Override
        public Integer call() {
            //try me bitch, 28 was here
            try {
                List<Path> toucanFiles = new ArrayList<>();
                for (Path path : paths) {
                    path = path.toAbsolutePath().normalize();
                    if (Files.isDirectory(path)) {
                        Stream<Path> fileStream = recursive ? Files.walk(path) : Files.list(path);
                        fileStream.filter(p -> p.toString().endsWith(".toucan"))
                                .forEach(toucanFiles::add);
                    } else if (path.toString().endsWith(".toucan")) {
                        toucanFiles.add(path);
                    }
                }

                if (dryRun) {
                    System.out.println("Files to parse:");
                    toucanFiles.forEach(System.out::println);
                    return 0;
                }

                if (outputFile != null) {
                    try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                        for (Path file : toucanFiles) {
                            String source = Files.readString(file);
                            TokenStream ts = new TokenStream(source);
                            writer.write("---- Tokenizing file: " + file + "\n");
                            writer.write(ts.getTokensAsString());
                            writer.write("\n");
                        }
                    }
                } else {
                    for (Path file : toucanFiles) {
                        if (verbose) System.out.println("---- Tokenizing file: " + file);
                        String source = Files.readString(file);
                        TokenStream ts = new TokenStream(source);
                        ts.printTokens();
                    }
                }
                System.out.printf("Parsing complete. %d files tokenized.\n", toucanFiles.size());
                return 0;
            } catch (IOException e) {
                System.err.println("Error processing files: " + e.getMessage());
                return 1;
            }
        }
    }
}
