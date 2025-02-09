package org.alex_hashtag.buildSystem;

import com.moandjiezana.toml.Toml;
import jakarta.mail.internet.InternetAddress;
import org.alex_hashtag.errors.TomlErrorManager;

import java.net.URI;
import java.net.URISyntaxException;
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
        Path configFilePath = Paths.get(path, "rainforest.toml");

        try
        {
            Toml config = new Toml().read(configFilePath.toFile());

            // Parse [project] section
            if (config.containsTable("project"))
            {
                Toml projectConfig = config.getTable("project");
                try
                {
                    String compilerStr = projectConfig.getString("compiler", "0.1.0");
                    GenericVersion compilerVersion = GenericVersion.parse(compilerStr);

                    String root = projectConfig.getString("root", "default_root");
                    String mainPathStr = projectConfig.getString("main", "src/Main.toucan");
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
            if (config.containsTable("metadata"))
            {
                Toml metadataConfig = config.getTable("metadata");
                try
                {
                    String name = metadataConfig.getString("name", "Unnamed Project");
                    String description = metadataConfig.getString("description", "");

                    // Authors
                    List<String> authorsList = metadataConfig.getList("authors", new ArrayList<>());
                    String[] authors = authorsList.toArray(new String[0]);

                    // Emails
                    List<String> emailsList = metadataConfig.getList("emails", new ArrayList<>());
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
                    String websiteStr = metadataConfig.getString("website", "");
                    URI website = null;
                    if (!websiteStr.isEmpty())
                    {
                        try
                        {
                            website = new URI(websiteStr);
                        } catch (URISyntaxException e)
                        {
                            errorManager.addError("Metadata Section - Website", "Invalid URI: " + websiteStr);
                        }
                    }

                    // Version
                    String versionStr = metadataConfig.getString("version", "0.1.0");
                    GenericVersion version = GenericVersion.parse(versionStr);

                    // Repository
                    String repositoryStr = metadataConfig.getString("repository", "");
                    URI repository = null;
                    if (!repositoryStr.isEmpty())
                    {
                        try
                        {
                            repository = new URI(repositoryStr);
                        } catch (URISyntaxException e)
                        {
                            errorManager.addError("Metadata Section - Repository", "Invalid URI: " + repositoryStr);
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
            if (config.containsTable("build"))
            {
                Toml buildConfig = config.getTable("build");
                try
                {
                    String optimizationStr = buildConfig.getString("optimization", "-O0");
                    Optimization optimization = Optimization.valueOf(optimizationStr.replace("-", "").toUpperCase());

                    Path executable = Paths.get(buildConfig.getString("executable", "build/output"));
                    Path logs = Paths.get(buildConfig.getString("logs", "build/logs"));
                    Path intermediates = Paths.get(buildConfig.getString("intermediates", "build/intermediate"));
                    String os = buildConfig.getString("os", "linux");
                    String architecture = buildConfig.getString("architecture", "x86_64");
                    boolean debugSymbols = buildConfig.getBoolean("debug_symbols", false);

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
            List<String> depsList = config.getList("project.dependencies", new ArrayList<>());
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

            if (errorManager.hasErrors())
            {
                errorManager.printErrors();
                System.exit(1);
            }

        } catch (IllegalStateException e)
        {
            throw e;
        }
    }
}
