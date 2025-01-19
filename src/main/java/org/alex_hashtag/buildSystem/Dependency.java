package org.alex_hashtag.buildSystem;

public record Dependency(
        String publisher,
        String project,
        GenericVersion version
) {
    /**
     * Parses a dependency string in the format "publisher::project::version".
     *
     * @param dependencyStr The dependency string to parse.
     * @return A Dependency instance.
     * @throws IllegalArgumentException If the format is incorrect or version is invalid.
     */
    public static Dependency parse(String dependencyStr) {
        String[] parts = dependencyStr.split("::");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Dependency must be in the format 'publisher::project::version'");
        }

        String publisher = parts[0].trim();
        String project = parts[1].trim();
        GenericVersion version = GenericVersion.parse(parts[2].trim());

        return new Dependency(publisher, project, version);
    }

    @Override
    public String toString() {
        return publisher + "::" + project + "::" + version;
    }
}
