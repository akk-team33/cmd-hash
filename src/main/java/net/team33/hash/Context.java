package net.team33.hash;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Context {

    private static final String HASHES_FILE_NAME = ".net.team33.hashes";

    final Map<Path, Entry> entries = new HashMap<>();
    final Map<BigInteger, List<Path>> pathsMap = new HashMap<>();
    final List<Throwable> problems = new LinkedList<>();
    final List<Path> ignored = new LinkedList<>();

    public final void performDirectory(final Path path) {
        entries.put(path, new Entry(path));
    }

    public final void performRegular(final Path path) {
        System.out.print(path);
        System.out.print(" ... ");
        final Entry entry = entry(path.getParent());
        final Data data = entry.getData(path.getFileName().toString());
        try {
            final long timeStamp = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
            if (data.timeStamp < timeStamp) {
                data.timeStamp = timeStamp;
                data.hash = Hash.create(path);
            }
            paths(data.hash).add(path);
            System.out.println(data.hash);
        } catch (final IOException caught) {
            problems.add(caught);
            System.out.println(caught.getMessage());
        }
    }

    public final void ignore(final Path path) {
        ignored.add(path);
    }

    public final void onProblem(final Throwable caught) {
        problems.add(caught);
    }

    private List<Path> paths(final BigInteger hash) {
        return Optional.ofNullable(pathsMap.get(hash)).orElseGet(() -> {
            final List<Path> result = new LinkedList<>();
            pathsMap.put(hash, result);
            return result;
        });
    }

    private Entry entry(final Path dirPath) {
        return Optional.ofNullable(entries.get(dirPath)).orElseGet(() -> {
            final Entry result = new Entry(dirPath);
            entries.put(dirPath, result);
            return result;
        });
    }

    private static class Data {
        private long timeStamp;
        private BigInteger hash;
    }

    private static class Entry {
        private final Path filename;
        private final Map<String, Data> dataMap;

        private Entry(final Path dirPath) {
            filename = dirPath.resolve(HASHES_FILE_NAME);
            dataMap = new HashMap<>();
        }

        private Data getData(final String name) {
            return Optional.ofNullable(dataMap.get(name)).orElseGet(() -> {
                final Data result = new Data();
                dataMap.put(name, result);
                return result;
            });
        }
    }
}
