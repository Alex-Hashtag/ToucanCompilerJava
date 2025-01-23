package org.alex_hashtag.buildSystem;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import jakarta.mail.internet.InternetAddress;
import org.alex_hashtag.errors.TomlErrorManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Rainforest
{
    public Project project;
    public Metadata metadata;
    public Build build;
    public List<Dependency> dependencies;

    /**
     * Constructs a Rainforest instance by parsing the provided TOML configuration file.
     *
     * @param path The path to the rainforest.toml file.
     */
    public Rainforest(String path)
    {
        TomlErrorManager errorManager = new TomlErrorManager();

        try (FileConfig config = FileConfig.of(path))
        {
            config.load();

            // Parse [project] section
            if (config.contains("project"))
            {
                CommentedConfig projectConfig = config.get("project");
                try
                {
                    String compilerStr = projectConfig.getOrElse("compiler", "0.1.0"); // Default compiler version
                    GenericVersion compilerVersion = GenericVersion.parse(compilerStr);

                    String root = projectConfig.getOrElse("root", "default_root");
                    String mainPathStr = projectConfig.getOrElse("main", "src/Main.toucan");
                    Path mainPath = Paths.get(mainPathStr);

                    project = new Project(compilerVersion, root, mainPath);
                } catch (IllegalArgumentException e)
                {
                    errorManager.addError("Project Section", e.getMessage());
                }
            }
            else
            {
                errorManager.addError("Missing [project] section in TOML configuration.");
            }

            // Parse [metadata] section
            if (config.contains("metadata"))
            {
                CommentedConfig metadataConfig = config.get("metadata");
                try
                {
                    String name = metadataConfig.getOrElse("name", "Unnamed Project");
                    String description = metadataConfig.getOrElse("description", "");

                    // Authors
                    List<String> authorsList = metadataConfig.getOrElse("authors", new ArrayList<String>());
                    String[] authors = authorsList.toArray(new String[0]);

                    // Emails
                    List<String> emailsList = metadataConfig.getOrElse("emails", new ArrayList<String>());
                    InternetAddress[] emails = new InternetAddress[emailsList.size()];
                    for (int i = 0; i < emailsList.size(); i++)
                    {
                        try
                        {
                            emails[i] = new InternetAddress(emailsList.get(i));
                        } catch (jakarta.mail.internet.AddressException e)
                        {
                            errorManager.addError("Metadata Section - Email", "Invalid email format: " + emailsList.get(i));
                        }
                    }

                    // Website
                    String websiteStr = metadataConfig.getOrElse("website", "");
                    URL website = null;
                    if (!websiteStr.isEmpty())
                    {
                        try
                        {
                            website = new URL(websiteStr);
                        } catch (MalformedURLException e)
                        {
                            errorManager.addError("Metadata Section - Website", "Invalid URL: " + websiteStr);
                        }
                    }

                    // Version
                    String versionStr = metadataConfig.getOrElse("version", "0.1.0");
                    GenericVersion version = GenericVersion.parse(versionStr);

                    // Repository
                    String repositoryStr = metadataConfig.getOrElse("repository", "");
                    URL repository = null;
                    if (!repositoryStr.isEmpty())
                    {
                        try
                        {
                            repository = new URL(repositoryStr);
                        } catch (MalformedURLException e)
                        {
                            errorManager.addError("Metadata Section - Repository", "Invalid URL: " + repositoryStr);
                        }
                    }

                    metadata = new Metadata(
                            name,
                            description,
                            authors,
                            emails,
                            website,
                            version,
                            repository
                    );
                } catch (IllegalArgumentException e)
                {
                    errorManager.addError("Metadata Section", e.getMessage());
                }
            }
            else
            {
                // If metadata section is missing, use default values
                metadata = new Metadata(
                        "Unnamed Project",
                        "",
                        new String[]{},
                        new InternetAddress[]{},
                        null,
                        new GenericVersion(0, 1, 0),
                        null
                );
            }

            // Parse [build] section
            if (config.contains("build"))
            {
                CommentedConfig buildConfig = config.get("build");
                try
                {
                    // Optimization
                    String optimizationStr = buildConfig.getOrElse("optimization", "-O0");
                    Optimization optimization;
                    try
                    {
                        optimization = Optimization.valueOf(optimizationStr.replace("-", "").toUpperCase());
                    } catch (IllegalArgumentException e)
                    {
                        errorManager.addError("Build Section - Optimization", "Invalid optimization level: " + optimizationStr);
                        optimization = Optimization.O0; // Default optimization
                    }

                    // Executable
                    String executableStr = buildConfig.getOrElse("executable", "build/output");
                    Path executable = Paths.get(executableStr);

                    // Logs
                    String logsStr = buildConfig.getOrElse("logs", "build/logs");
                    Path logs = Paths.get(logsStr);

                    // Intermediates
                    String intermediatesStr = buildConfig.getOrElse("intermediates", "build/intermediate");
                    Path intermediates = Paths.get(intermediatesStr);

                    // OS
                    String os = buildConfig.getOrElse("os", "linux");

                    // Architecture
                    String architecture = buildConfig.getOrElse("architecture", "x86_64");

                    // Debug Symbols
                    boolean debugSymbols = buildConfig.getOrElse("debug_symbols", false);

                    build = new Build(
                            optimization,
                            executable,
                            logs,
                            intermediates,
                            os,
                            architecture,
                            debugSymbols
                    );
                } catch (IllegalArgumentException e)
                {
                    errorManager.addError("Build Section", e.getMessage());
                }
            }
            else
            {
                // If build section is missing, use default values
                build = new Build(
                        Optimization.O0,
                        Paths.get("build/output"),
                        Paths.get("build/logs"),
                        Paths.get("build/intermediate"),
                        "linux",
                        "x86_64",
                        false
                );
            }

            // Parse dependencies
            dependencies = new ArrayList<>();
            if (config.contains("project.dependencies"))
            {
                List<String> depsList = config.getOrElse("project.dependencies", new ArrayList<String>());
                for (String depStr : depsList)
                {
                    try
                    {
                        Dependency dep = Dependency.parse(depStr);
                        dependencies.add(dep);
                    } catch (IllegalArgumentException e)
                    {
                        errorManager.addError("Dependencies Section", "Invalid dependency format: " + depStr);
                    }
                }
            }
            // If dependencies are missing, leave the list empty as per requirements.

            // After parsing, check for any errors
            if (errorManager.hasErrors())
            {
                errorManager.printErrors();
                System.exit(1);
            }

        } catch (IllegalStateException e)
        {
            // Re-throwing after errorManager handled the errors
            throw e;
        }
    }
}
