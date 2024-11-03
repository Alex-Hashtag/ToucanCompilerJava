package org.alex_hashtag;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.alex_hashtag.tokenization.TokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;


public class Main
{
    public static void main(String[] args) throws IOException
    {
        Path source = Paths.get("test_project/rainforest.toml");

        try (FileConfig config = FileConfig.of(source) )
        {
            config.load();


            // Now you can access your configuration values here
            String myValue = config.get("project.compiler");
            System.out.println("Config value: " + myValue);
        }

    }
}