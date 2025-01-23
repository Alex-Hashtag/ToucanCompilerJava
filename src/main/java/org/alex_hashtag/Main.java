package org.alex_hashtag;

import org.alex_hashtag.buildSystem.Rainforest;
import org.alex_hashtag.tokenization.TokenStream;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length < 1) {
            System.out.println("No command provided. Usage:\n  parse <directory>\n  build <...>\n  etc.");
            return;
        }

        String command = args[0];
        switch (command) {
            case "parse" -> handleParseCommand(args);
            // You could add other commands here: e.g. "build", "run", etc.
            default -> System.out.println("Unknown command: " + command);
        }
    }

    private static void handleParseCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: parse <directory>");
            return;
        }
        String directoryStr = args[1];
        Path dir = Paths.get(directoryStr);

        if (!Files.isDirectory(dir)) {
            System.out.println("Not a directory: " + directoryStr);
            return;
        }

        // 1) Find the rainforest.toml (this is up to you. Maybe it's always in dir, or maybe it's in a parent.)
        Path rainforestToml = dir.resolve("rainforest.toml");
        if (!Files.exists(rainforestToml)) {
            System.out.println("No rainforest.toml found at: " + rainforestToml);
            return;
        }

        // Build a Rainforest object
        var rainforest = new Rainforest(rainforestToml.toString());
        System.out.println("Parsed Rainforest data: ");
        // e.g. you could print out the project info, etc.

        // 2) Recursively find all .toucan files
        List<Path> toucanFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(p -> p.toString().endsWith(".toucan"))
                    .forEach(toucanFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 3) Tokenize each .toucan file, store them
        //    If you want to skip duplicates, you might store them in a Set
        //    But here we assume we just do them all
        for (Path p : toucanFiles) {
            System.out.println("---- Parsing file: " + p);
            try {
                String source = Files.readString(p);
                TokenStream ts = new TokenStream(source);
                // Maybe store it in a map from Path->TokenStream or so
                // For demonstration, let's just print them:
                ts.printTokens();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
