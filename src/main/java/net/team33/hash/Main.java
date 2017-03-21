package net.team33.hash;

import net.team33.hash.files.Walk;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    public static void main(final String[] args) throws IOException, InterruptedException {
        final long time0 = System.currentTimeMillis();
        final Context context = new Context();
        Stream.of(args)
                .map(arg -> Paths.get(arg).toAbsolutePath().normalize())
                .forEach(path -> Walk.through(path)
                        .whenRegular(context::performRegular)
                        .whenDirectory(context::performDirectory)
                        .whenSpecial(context::ignore)
                        .onProblems(context::onProblem)
                        .build().run());

        System.out.printf("%nRegular files (%d):%n", context.pathsMap.size());
        context.pathsMap.entrySet().stream()
                .filter(e -> 1 < e.getValue().size())
                .forEach(e -> System.out.printf("%s (%d): %s%n", e.getKey(), e.getValue().size(), e.getValue()));

        System.out.printf("%nIgnored (%d):%n", context.ignored.size());
        context.ignored.forEach(System.out::println);

        System.out.printf("%nProblems (%d):%n", context.problems.size());
        context.problems.forEach(problem -> problem.printStackTrace(System.out));

        System.out.printf("%n%s seconds%n", (System.currentTimeMillis() - time0) / 1000);
    }
}
