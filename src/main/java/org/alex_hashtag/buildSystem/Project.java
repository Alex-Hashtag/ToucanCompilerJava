package org.alex_hashtag.buildSystem;

import java.nio.file.Path;


public record Project(
        GenericVersion version,
        String root,
        Path main
)
{
}
