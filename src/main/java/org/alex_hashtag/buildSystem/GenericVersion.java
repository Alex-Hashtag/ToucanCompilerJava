package org.alex_hashtag.buildSystem;

public record GenericVersion(int major, int minor, int patch)
{
    public GenericVersion
    {
        if (major < 0 || minor < 0 || patch < 0)
            throw new IllegalArgumentException("Version numbers cannot be negative");
    }

    public static GenericVersion parse(String version)
    {
        String[] parts = version.split("\\.");
        if (parts.length != 3)
            throw new IllegalArgumentException("Version must be in the format of 'major.minor.patch'");

        try
        {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return new GenericVersion(major, minor, patch);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Version components must be integers", e);
        }
    }

    @Override
    public String toString()
    {
        return major + "." + minor + "." + patch;
    }
}
