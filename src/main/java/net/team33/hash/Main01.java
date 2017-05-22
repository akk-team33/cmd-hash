package net.team33.hash;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main01 implements Runnable {

    private final List<Throwable> problems = new LinkedList<>();
    private final List<Path> ignored = new LinkedList<>();
    private final List<Path> skipped = new LinkedList<>();
    private final List<Path> failed = new LinkedList<>();
    private final Registry registry = new Registry();

    private final Output out;
    private final Function<Path, Path> hashAndTag;
    private final Function<Path, Path> sieve;
    private final Path root;
    private final Path trash;

    private Main01(final Output out, final List<String> args) throws InitialisationException {
        final List<String> upperCaseArgs = args.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        final int indexOfRename = upperCaseArgs.indexOf("RENAME");
        final int indexOfSieve = upperCaseArgs.indexOf("SIEVE");
        final HashSet<Integer> indexes = Stream.iterate(0, i -> i + 1).limit(args.size())
                .collect(Collectors.toCollection(HashSet::new));

        if (0 > indexOfRename) {
            this.hashAndTag = this::hashRegister;
        } else {
            this.hashAndTag = this::hashRename;
            indexes.remove(indexOfRename);
        }

        if (0 > indexOfSieve) {
            this.sieve = path -> path;
        } else {
            this.sieve = this::sieveToTrash;
            indexes.remove(indexOfSieve);
        }

        if (1 != indexes.size()) {
            throw new InitialisationException();
        } else {
            this.root = Paths.get(args.get(indexes.iterator().next())).toAbsolutePath().normalize();
            this.trash = Paths.get(root.toString() + ".trash");
            this.out = out;
        }
    }

    public static void main(final String[] args) {
        final Output out = new Output();
        try {
            new Main01(out, Arrays.asList(args)).run();
        } catch (InitialisationException e) {
            if (args.length > 0) {
                out.println("Given Arguments:")
                        .println()
                        .println("    ", Stream.of(args).collect(Collectors.joining(" ")))
                        .println();
            }
            out.println("Required Arguments:")
                    .println()
                    .println("    [RENAME] [SIEVE] $path")
                    .println()
                    .println("$path:")
                    .println("    Path to a file or directory")
                    .println()
                    .println("RENAME:")
                    .println("    store hash into the name of each file instead of a special hash-file")
                    .println()
                    .println("SIEVE:")
                    .println("    move duplicate files to a directory named {$path}.trash");
        }
    }

    @Override
    public void run() {
        process(root);

        if (0 < skipped.size()) {
            out.println().println("[SKIPPED]");
            skipped.forEach(out::println);
        }

        if (0 < failed.size()) {
            out.println().println("[FAILED]");
            failed.forEach(out::println);
        }

        if (0 < problems.size()) {
            out.println().println("[PROBLEMS]");
            problems.forEach(out::stackTrace);
        }
    }

    private void process(final Path path) {
        out.println(path, " ...");
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

    private void process(final Iterator<Path> paths) {
        while (paths.hasNext()) {
            process(paths.next());
        }
    }

    private void onRegular(final Path path) {
        final Path hashed = hashAndTag.apply(path);
        final Path sieved = sieve.apply(hashed);
        out.println("-> ", sieved);
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
        removeIfEmpty(path);
    }

    private void removeIfEmpty(final Path path) {
        try {
            Files.delete(path);
        } catch (final IOException ignored) {
        }
    }

    private Path sieveToTrash(final Path path) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private Path hashRename(final Path path) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private Path hashRegister(final Path path) {
        final Registry.Entry entry = registry.entry(path.getParent());
        final Registry.Details details = entry.details(path.getFileName().toString());
        try {
            final long timeStamp = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
            if (details.getUpdate() != timeStamp) {
                details.setUpdate(timeStamp);
                details.setHash(Hash.create(path));
            }
            //paths(details.getHash()).add(path);
            out.println("-> ", details.getHash());
        } catch (final IOException caught) {
            problems.add(caught);
            out.println("-> ", caught.getMessage());
        }
        return path;
    }

    static class Output {
        Output print(final Object... args) {
            Stream.of(args).forEach(System.out::print);
            return this;
        }

        Output println(final Object... args) {
            print(args);
            System.out.println();
            return this;
        }

        Output stackTrace(final Throwable throwable) {
            throwable.printStackTrace(System.out);
            System.out.println();
            return this;
        }
    }

    private static class InitialisationException extends Exception {
    }
}
