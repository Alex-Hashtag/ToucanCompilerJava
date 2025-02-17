package org.alex_hashtag.command;

import org.alex_hashtag.buildSystem.Rainforest;
import org.alex_hashtag.internal_representation.ast.AbstractSyntaxTree;
import org.alex_hashtag.tokenizationOLD.TokenStream;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


@CommandLine.Command(
        name = "parse",
        mixinStandardHelpOptions = true,
        description = "Parses .toucan files in directories or specific files (default: current directory)."
)
public class ParseCommand implements Callable<Integer>
{

    @CommandLine.Parameters(
            arity = "0..*",
            description = "Directories or files to parse (default: current directory)."
    )
    private final List<Path> paths = List.of(Paths.get("."));  // default is "."

    @CommandLine.Option(
            names = {"--dry-run"},
            description = "List files to be parsed without outputting an AST."
    )
    private boolean dryRun;

    @CommandLine.Option(
            names = {"-r", "--recursive"},
            description = "Recursively parse subdirectories (default: true).",
            defaultValue = "true"
    )
    private boolean recursive;


    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "Enable verbose logging."
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"-o", "--output-file"},
            description = "Specify a file to write the parsed output (AST). If not specified, output is printed to the console."
    )
    private Path outputFile;

    @Override
    public Integer call()
    {
        List<TokenStream> tokenStreams = new ArrayList<>();

        try
        {
            // 1) Gather .toucan files
            List<Path> toucanFiles = new ArrayList<>();
            for (Path path : paths)
            {
                path = path.toAbsolutePath().normalize();
                if (verbose)
                {
                    System.out.println("Processing path: " + path);
                }
                Rainforest rainforest = new Rainforest(path + "");
                if (Files.isDirectory(path))
                {
                    try (Stream<Path> fileStream = recursive ? Files.walk(path) : Files.list(path))
                    {
                        fileStream.filter(p -> p.toString().endsWith(".toucan"))
                                .forEach(toucanFiles::add);
                    }
                }
                else if (path.toString().endsWith(".toucan"))
                {
                    toucanFiles.add(path);
                }
            }

            // 2) If dry-run, list files and return
            if (dryRun)
            {
                System.out.println("Files to parse (dry-run):");
                toucanFiles.forEach(System.out::println);
                return 0;
            }

            // 3) If no files found, inform the user and return
            if (toucanFiles.isEmpty())
            {
                System.out.println("No .toucan files found to parse in the specified paths.");
                return 0;
            }

            // 4) Parse files
            for (Path file : toucanFiles)
            {
                if (verbose)
                {
                    System.out.println("---- Parsing file: " + file);
                }
                String source = Files.readString(file);

                // Create TokenStream
                TokenStream ts = new TokenStream(file, source);
                tokenStreams.add(ts);
            }

            // 5) Create AST from token streams
            AbstractSyntaxTree ast = new AbstractSyntaxTree(tokenStreams);

            // 6) Write output to file or console
            if (outputFile != null)
            {
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile))
                {
                    writer.write(ast.toString());
                }
                System.out.printf("Parsing complete. Output written to '%s'.\n", outputFile);
            }
            else
            {
                System.out.println("Parsing complete. Parsed output:");
                System.out.println(ast);
            }

            // 7) Success message
            System.out.printf("Parsing complete. %d files parsed.\n", toucanFiles.size());
            return 0;

        } catch (IOException e)
        {
            System.err.println("Error processing files: " + e.getMessage());
            return 1;
        }
    }
}
