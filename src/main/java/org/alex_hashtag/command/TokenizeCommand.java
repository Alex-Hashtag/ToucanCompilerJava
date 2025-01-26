package org.alex_hashtag.command;

import org.alex_hashtag.buildSystem.Rainforest;
import org.alex_hashtag.tokenization.TokenStream;
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
public class TokenizeCommand implements Callable<Integer>
{

    @CommandLine.Parameters(
            arity = "0..*",
            description = "Directories or files to parse (default: current directory)."
    )
    private final List<Path> paths = List.of(Paths.get("."));  // default is "."

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
    public Integer call()
    {
        // 1) Load rainforest.toml to get project info (especially 'root')

        String topDir = "test_project";  // e.g. "src" or "mySrcDir"

        try
        {

            // 3) Gather .toucan files
            List<Path> toucanFiles = new ArrayList<>();
            for (Path path : paths)
            {

                path = path.toAbsolutePath().normalize();
                System.out.println(path.toString());
                Rainforest rainforest = new Rainforest(path.toString()+"\\rainforest.toml");
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

            // 4) If dry-run, just list the files found
            if (dryRun)
            {
                System.out.println("Files to parse (dry-run):");
                toucanFiles.forEach(System.out::println);
                return 0;
            }

            // 5) If no files found, inform user and return
            if (toucanFiles.isEmpty())
            {
                System.out.println("No .toucan files found to parse in the specified paths.");
                return 0;
            }

            // 6) If an output file is specified, write tokens there; otherwise print to console
            if (outputFile != null)
            {
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile))
                {
                    for (Path file : toucanFiles)
                    {
                        if (verbose)
                        {
                            System.out.println("---- Tokenizing file: " + file);
                        }
                        String source = Files.readString(file);

                        // The TokenStream internally manages errors and will exit if any
                        TokenStream ts = new TokenStream(file, source);

                        // If we reach here, no errors => we write tokens to the file
                        writer.write("---- Tokenizing file: " + file + "\n");
                        writer.write(ts.getTokensAsString());
                        writer.write("\n");
                    }
                }
            }
            else
            {
                // Print tokens to console
                for (Path file : toucanFiles)
                {
                    if (verbose)
                    {
                        System.out.println("---- Tokenizing file: " + file);
                    }
                    String source = Files.readString(file);

                    // TokenStream creation & error check
                    TokenStream ts = new TokenStream(file, source);

                    // If no errors, print tokens
                    ts.printTokens();
                }
            }

            // 7) If we processed everything without immediate errors, success
            System.out.printf("Parsing complete. %d files tokenized.\n", toucanFiles.size());
            return 0;

        } catch (IOException e)
        {
            System.err.println("Error processing files: " + e.getMessage());
            return 1;
        }
    }
}