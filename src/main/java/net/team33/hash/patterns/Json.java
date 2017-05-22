package net.team33.hash.patterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Json {

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder().setPrettyPrinting();
    private static final Charset CHARSET = Charset.forName("utf-8");
    private static final OpenOption[] OPTIONS = {
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
    };

    private Gson gson = GSON_BUILDER.create();

    public void write(final Object src, final Appendable out) {
        gson.toJson(src, out);
    }

    public void write(final Object src, final Path path) {
        try (final BufferedWriter out = Files.newBufferedWriter(path, CHARSET, OPTIONS)) {
            write(src, out);
        } catch (IOException e) {
            throw new IllegalArgumentException("can not write to <" + path + ">", e);
        }
    }

    public <T> T read(final Class<T> tClass, final Path path) {
        try (final BufferedReader in = Files.newBufferedReader(path, CHARSET)) {
            return gson.fromJson(in, tClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("can not read from <" + path + ">", e);
        }
    }
}
