package org.alex_hashtag.buildSystem;

import java.nio.file.Path;


public record Build(
        Optimization optimization,
        Path executable,
        Path logs,
        Path intermediates,
        String os,
        String architecture,
        boolean debugSymbols
)
{
}
