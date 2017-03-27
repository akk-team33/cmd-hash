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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    private final Registry registry = new Registry();
    private final Map<BigInteger, List<Path>> pathsMap = new HashMap<>();
    private final List<Throwable> problems = new LinkedList<>();
    private final List<Path> ignored = new LinkedList<>();
    private final List<Path> failed = new LinkedList<>();
    private final List<Path> skipped = new LinkedList<>();

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

        //print(main.ignored);
        print(main.failed);
        print(main.skipped);
        print(main.problems, "%nProblems (%d):%n", problem -> problem.printStackTrace(System.out));

        System.out.printf("%n%s seconds%n", (System.currentTimeMillis() - time0) / 1000);
    }

    private static void print(final List<?> collected) {
        print(collected, "%nIgnored (%d):%n", System.out::println);
    }

    private static <T> void print(final List<T> collected, final String format, final Consumer<T> consumer) {
        if (collected.size() > 0) {
            System.out.printf(format, collected.size());
            collected.forEach(consumer);
        }
    }

    private void process(final Iterator<Path> paths) {
        while (paths.hasNext()) {
            process(paths.next());
        }
    }

    private void process(final Path path) {
        if (Registry.FILE_NAME.equals(path.getFileName().toString())) {
            ignored.add(path);
        } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            onDirectory(path);
        } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            onRegular(path);
        } else {
            skipped.add(path);
        }
    }

    private void onDirectory(final Path path) {
        try (final DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            process(paths.iterator());
            registry.write(path);
            registry.remove(path);
        } catch (final IOException e) {
            problems.add(e);
            failed.add(path);
        }
    }

    private void onRegular(final Path path) {
        System.out.print(path);
        System.out.print(" ... ");
        final Registry.Entry entry = registry.entry(path.getParent());
        final Registry.Details details = entry.details(path.getFileName().toString());
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

    private List<Path> paths(final BigInteger hash) {
        return Optional.ofNullable(pathsMap.get(hash)).orElseGet(() -> {
            final List<Path> paths = new LinkedList<>();
            pathsMap.put(hash, paths);
            return paths;
        });
    }
}
