package net.team33.hash;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

class Hash {

    private static final int BUFFER_SIZE = 17;

    static BigInteger create(final Path path) throws IOException {
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
