package net.team33.hash.files;

import net.team33.hash.patterns.Mutable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Walk implements Runnable {

    private static final Predicate<Path> IS_REGULAR = path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    private static final Predicate<Path> IS_DIRECTORY = path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    private static final Consumer<Throwable> IGNORE_PROBLEMS = caught -> {
    };

    private final Path root;
    private final List<Consumer<Path>> consumers;
    private final Consumer<Throwable> problemHandler;

    private Walk(final Builder builder) {
        root = builder.root;
        consumers = Stream.concat(Stream.of(recursive()), builder.consumers.stream()).collect(toList());
        problemHandler = builder.problemHandler.get();
    }

    public static Builder through(final Path path) {
        return new Builder(path);
    }

    private Consumer<Path> recursive() {
        return path -> {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                try (final DirectoryStream<Path> content = Files.newDirectoryStream(path)) {
                    for (final Path entry : content) {
                        run(entry);
                    }
                } catch (IOException e) {
                    problemHandler.accept(e);
                }
            }
        };
    }

    @Override
    public final void run() {
        run(root);
    }

    private void run(final Path path) {
        consumers.forEach(consumer -> consumer.accept(path));
    }

    public static class Builder {
        private final Mutable<Consumer<Throwable>> problemHandler = Mutable.of(IGNORE_PROBLEMS);
        private final List<Consumer<Path>> consumers = new LinkedList<>();
        private final Path root;

        private Builder(final Path root) {
            this.root = root;
        }

        public final Builder whenDirectory(final Consumer<Path> consumer) {
            return when(IS_DIRECTORY).then(consumer);
        }

        public final Builder whenRegular(final Consumer<Path> consumer) {
            return when(IS_REGULAR).then(consumer);
        }

        public final Builder whenSpecial(final Consumer<Path> consumer) {
            return when(IS_DIRECTORY.or(IS_REGULAR).negate()).then(consumer);
        }

        public final When when(final Predicate<Path> predicate) {
            return new When(this, predicate);
        }

        public final Builder onProblems(final Consumer<Throwable> consumer) {
            problemHandler.set(consumer);
            return this;
        }

        public final Walk build() {
            return new Walk(this);
        }

        private Builder add(final Consumer<Path> consumer) {
            consumers.add(consumer);
            return this;
        }
    }

    public static class When {
        private final Builder builder;
        private final Predicate<Path> predicate;

        private When(final Builder builder, final Predicate<Path> predicate) {
            this.builder = builder;
            this.predicate = predicate;
        }

        public Builder then(final Consumer<Path> consumer) {
            return builder.add(path -> {
                if (predicate.test(path)) {
                    consumer.accept(path);
                }
            });
        }
    }
}
