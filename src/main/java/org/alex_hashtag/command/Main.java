package org.alex_hashtag.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;


@CommandLine.Command(
        name = "rainforest",
        version = "Rainforest 0.2",
        mixinStandardHelpOptions = true,
        description = "A command-line interface for the Toucan programming language."
)
public class Main implements Callable<Integer>
{

    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new Main())
                .addSubcommand("tokenize", new TokenizeCommand())
                .addSubcommand("parse", new ParseCommand())
                // Additional subcommands can go here, e.g. "build", "run", etc.
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()
    {
        System.out.println("No command provided. Use --help for usage instructions.");
        return 0;
    }
}
