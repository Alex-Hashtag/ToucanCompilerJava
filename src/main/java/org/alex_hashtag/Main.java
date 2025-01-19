package org.alex_hashtag;

import org.alex_hashtag.buildSystem.Rainforest;

public class Main
{
    public static void main(String[] args)
    {

        var rf = new Rainforest("test_project/rainforest.toml");
        System.out.println("Main Path: " + rf.project.main());
        System.out.println("Root: " + rf.project.root());
        System.out.println("Compiler Version: " + rf.project.version());

        // Optionally, print metadata and build information
        System.out.println("Project Name: " + rf.metadata.name());
        System.out.println("Build Optimization: " + rf.build.optimization());

        // Print dependencies
        if (!rf.dependencies.isEmpty())
        {
            System.out.println("Dependencies:");
            for (var dep : rf.dependencies)
            {
                System.out.println("  - " + dep);
            }
        }
        else
        {
            System.out.println("No dependencies found.");
        }
    }
}