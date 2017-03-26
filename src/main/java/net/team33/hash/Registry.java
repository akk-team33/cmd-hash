package net.team33.hash;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

class Registry {

    static final String FILE_NAME = ".net.team33.hashes";
    static final Charset UTF8 = Charset.forName("utf-8");

    private final Map<Path, Entry> entries = new HashMap<>(0);
    private final Consumer<Throwable> onProblem = caught -> {
    };

    Entry entry(final Path path) {
        return Optional.ofNullable(entries.get(path)).orElseGet(() -> {
            final Entry entry = read(path.resolve(FILE_NAME));
            entries.put(path, entry);
            return entry;
        });
    }

    Entry read(final Path path) {
        try (final BufferedReader reader = Files.newBufferedReader(path, UTF8)) {
            return new Entry(new Gson().fromJson(reader, Entry.class.getGenericSuperclass()));
        } catch (IOException e) {
            onProblem.accept(e);
            return new Entry(Collections.emptyMap());
        }
    }

    void write(final Path path) {
        Optional.ofNullable(entries.get(path))
                .ifPresent((entry) -> write(path.resolve(FILE_NAME), entry));
    }

    void remove(final Path path) {
        entries.remove(path);
    }

    private void write(final Path path, final Entry entry) {
        try (final BufferedWriter writer = Files.newBufferedWriter(path, UTF8, CREATE, TRUNCATE_EXISTING)) {
            new Gson().toJson(entry, writer);
        } catch (IOException e) {
            onProblem.accept(e);
        }
    }

    class Entry extends TreeMap<String, Details> {
        Entry(final Map<? extends String, ? extends Details> m) {
            super(m);
        }

        Details details(final String name) {
            return Optional.ofNullable(get(name)).orElseGet(() -> {
                final Details result = new Details();
                put(name, result);
                return result;
            });
        }
    }

    class Details {

        private long update;
        private BigInteger hash;

        long getUpdate() {
            return update;
        }

        void setUpdate(final long timeStamp) {
            update = timeStamp;
        }

        BigInteger getHash() {
            return hash;
        }

        void setHash(final BigInteger value) {
            hash = value;
        }
    }
}
