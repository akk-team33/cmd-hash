package net.team33.hash;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

public class HashRegular {

    private static final int BUFFER_SIZE = 17;

    private final Path path;
    private final String name;
    private long timestamp;

    public HashRegular(final Path path) throws IOException {
        this.path = path;
        this.name = path.getFileName().toString();
        this.timestamp = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
    }

    public final boolean update(final Map<String, HashInfo> hashes) throws IOException {
        if (hashes.containsKey(name) && hashes.get(name).timestamp > timestamp) {
            return false;
        }
        hashes.put(name, new HashInfo(timestamp, hash()));
        return true;
    }

    private BigInteger hash() throws IOException {
        final byte[] values = new byte[BUFFER_SIZE];
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (final InputStream in = Files.newInputStream(path)) {
            long k = 0;
            int read = in.read(buffer);
            while (0 < read) {
                for (int i = 0; i < read; ++i, ++k) {
                    final int m = (int) (k % values.length);
                    values[m] += buffer[i];
                }
                read = in.read(buffer);
            }
        }
        return new BigInteger(1, values);
    }
}
