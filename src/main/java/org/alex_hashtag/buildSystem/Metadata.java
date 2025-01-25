package org.alex_hashtag.buildSystem;

import jakarta.mail.internet.InternetAddress;

import java.net.URI;


public record Metadata(
        String name,
        String description,
        String[] authors,
        InternetAddress[] emails,
        URI website,          // Can be null
        GenericVersion version,
        URI repository        // Can be null
)
{
}
