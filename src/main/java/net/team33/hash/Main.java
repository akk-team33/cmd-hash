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

    private static final Consumer<Map.Entry<BigInteger, List<Path>>> DEFAULT_SIEVE_JOB =
            e -> System.out.printf("%s (%d): %s%n", e.getKey(), e.getValue().size(), e.getValue());

    private final Registry registry = new Registry();
    private final Map<BigInteger, List<Path>> pathsMap = new HashMap<>();
    private final List<Throwable> problems = new LinkedList<>();
    private final List<Path> ignored = new LinkedList<>();
    private final List<Path> failed = new LinkedList<>();
    private final List<Path> skipped = new LinkedList<>();
    private final Consumer<Map.Entry<BigInteger, List<Path>>> sieveJob;
    private final Path trash;

    private Main(final Path trash) {
        this.trash = trash;
        this.sieveJob = null == trash ? DEFAULT_SIEVE_JOB : this::sieve;
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        final long time0 = System.currentTimeMillis();
        final Path trash;
        final int head;
        if (1 < args.length && "SIEVE".equals(args[0].toUpperCase())) {
            trash = Paths.get(args[1]).toAbsolutePath().normalize();
            head = 2;
        } else {

            trash = null;
            head = 0;
        }
        final Main main = new Main(trash);
        main.process(Stream.of(args).skip(head)
                .map(arg -> Paths.get(arg).toAbsolutePath().normalize())
                .collect(Collectors.toList())
                .iterator());

        System.out.printf("%nDuplicate files (total %d):%n", main.pathsMap.size());
        main.pathsMap.entrySet().stream()
                .filter(e -> 1 < e.getValue().size())
                .forEach(main.sieveJob);

        //print(main.ignored);
        print(main.failed);
        print(main.skipped);
        print(main.problems, "%nProblems (%d):%n", problem -> problem.printStackTrace(System.out));

        System.out.printf("%n%s seconds%n", ((System.currentTimeMillis() - time0) / 1000.0));
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

    private void sieve(final Map.Entry<BigInteger, List<Path>> entry) {
        entry.getValue().stream().skip(1).forEach((path) -> sieve(entry.getKey(), path));
    }

    private void sieve(final BigInteger key, final Path path) {
        final Path parent = trash.getParent();
        final Path target = trash.resolve(parent.relativize(path));
        try {
            Files.createDirectories(target.getParent());
            Files.move(path, target);
            System.out.printf("%s: moved <%s> to <%s>%n", key, path, target);
        } catch (IOException e) {
            problems.add(e);
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
        } else if (path.equals(trash)) {
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
            if (details.getUpdate() != timeStamp) {
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
