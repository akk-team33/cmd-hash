package net.team33.hash;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Hashes {

    public static List<Map<String, Object>> read(final Path path) throws IOException {
        try (final Reader reader = Files.newBufferedReader(path, Charset.forName("utf8"))) {
            //noinspection unchecked
            return new Gson().fromJson(reader, List.class);
        }
    }

    public static class Entry {
        public String name;
        public long update;
        public long access;
        public BigInteger hash;
    }
}
