package net.team33.hash;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    private final Cache cache = new Cache();
    private final Map<BigInteger, List<Path>> pathsMap = new HashMap<>();
    private final List<Throwable> problems = new LinkedList<>();
    private final List<Path> ignored = new LinkedList<>();

    public static void main(final String[] args) throws IOException, InterruptedException {
        final long time0 = System.currentTimeMillis();
        final Main main = new Main();
        main.process(Stream.of(args)
                .map(arg -> Paths.get(arg).toAbsolutePath().normalize())
                .collect(Collectors.toList())
                .iterator());

        System.out.printf("%nDuplicate files (total %d):%n", main.pathsMap.size());
        main.pathsMap.entrySet().stream()
                .filter(e -> 1 < e.getValue().size())
                .forEach(e -> System.out.printf("%s (%d): %s%n", e.getKey(), e.getValue().size(), e.getValue()));

        System.out.printf("%nIgnored (%d):%n", main.ignored.size());
        main.ignored.forEach(System.out::println);

        System.out.printf("%nProblems (%d):%n", main.problems.size());
        main.problems.forEach(problem -> problem.printStackTrace(System.out));

        System.out.printf("%n%s seconds%n", (System.currentTimeMillis() - time0) / 1000);
    }

    private void process(final Iterator<Path> paths) {
        while (paths.hasNext()) {
            process(paths.next());
        }
    }

    private void process(final Path path) {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            onDirectory(path);
        } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            onRegular(path);
        } else {
            onOther(path);
        }
    }

    private void onDirectory(final Path path) {
        try (final DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            process(paths.iterator());
        } catch (final IOException e) {
            problems.add(e);
            ignored.add(path);
        }
    }

    private void onRegular(final Path path) {
        System.out.print(path);
        System.out.print(" ... ");
        final Cache.Entry entry = cache.entry(path.getParent());
        final Cache.Details details = entry.details(path.getFileName().toString());
        try {
            final long timeStamp = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
            if (details.getUpdate() < timeStamp) {
                details.setUpdate(timeStamp);
                details.setHash(Hash.create(path));
            }
            paths(details.getHash()).add(path);
            System.out.println(details.getHash());
        } catch (final IOException caught) {
            problems.add(caught);
            System.out.println(caught.getMessage());
        }
    }

    private void onOther(final Path path) {
        ignored.add(path);
    }

    private List<Path> paths(final BigInteger hash) {
        return Optional.ofNullable(pathsMap.get(hash)).orElseGet(() -> {
            final List<Path> paths = new LinkedList<>();
            pathsMap.put(hash, paths);
            return paths;
        });
    }
}
