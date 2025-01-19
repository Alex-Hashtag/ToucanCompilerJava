package org.alex_hashtag.buildSystem;

import jakarta.mail.internet.InternetAddress;
import java.net.URL;

public record Metadata(
        String name,
        String description,
        String[] authors,
        InternetAddress[] emails,
        URL website,          // Can be null
        GenericVersion version,
        URL repository        // Can be null
) {
}
