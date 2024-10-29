package org.alex_hashtag;

import org.alex_hashtag.tokenization.TokenStream;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


public class Main
{
    public static void main(String[] args) throws IOException {
        Path source = Paths.get("test_project/rainforest.toml");
        TomlParseResult result = Toml.parse(source);
        result.errors().forEach(System.err::println);

    }
}
