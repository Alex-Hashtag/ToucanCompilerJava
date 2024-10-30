package org.alex_hashtag.buildSystem;

import jakarta.mail.internet.InternetAddress;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Path;

import java.net.URL;

public class Rainforest
{
    Project project;
    Metadata metadata;
    Build build;

    public Rainforest(Path path) throws IOException
    {
        TomlParseResult result = Toml.parse(path);
        result.errors().forEach(System.err::println);
    }
}

record Project(
        CompilerVersion version,
        String root,
        Path main
){}

record CompilerVersion(int major, int minor, int patch)
{
    public CompilerVersion
    {
        if (major < 0 || minor < 0 || patch < 0)
            throw new IllegalArgumentException("Version numbers cannot be negative");
    }

    @Override
    public String toString()
    {
        return major + "." + minor + "." + patch;
    }

    public static CompilerVersion parse(String version)
    {
        String[] parts = version.split("\\.");
        if (parts.length != 3)
            throw new IllegalArgumentException("Version must be in the format of 'major.minor.patch'");

        try
        {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return new CompilerVersion(major, minor, patch);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Version components must be integers", e);
        }
    }
}

record Metadata(
        String name,
        String description,
        String author,
        InternetAddress email,
        URL website,
        String version,
        URL repository
){}

record Build(
        Optimization optimization,
        Path executable,
        Path logs,
        Path intermediates,
        String os,
        String architecture,
        boolean debugSymbols
){}

enum Optimization
{
    O0,
    O1,
    O2,
    O3,
    O4,
    Os,
    Oz
}

