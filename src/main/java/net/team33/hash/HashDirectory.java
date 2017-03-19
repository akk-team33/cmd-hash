package net.team33.hash;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HashDirectory {

    private Path path;

    public boolean update() throws IOException {
        final Map<String, HashInfo> hashes = new HashMap<>(); // TODO: read from hash file
        try (final DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            for (final Path child : paths) {
                new HashRegular(child).update(hashes);
            }
        }
        // TODO: write to hash file if modified
        return true;
    }
}
