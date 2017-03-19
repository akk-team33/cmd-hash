package net.team33.hash;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

public final class MainAlt {

    private static final Logger LOGGER
            = Logger.getLogger(MainAlt.class.getCanonicalName());

    private static final WatchEvent.Kind<?>[] KINDS = {
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE};

    public static void main(final String[] args) throws IOException, InterruptedException {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        final Path watchPath = Paths.get(args[0]).toAbsolutePath().normalize();

        LOGGER.info("watching " + watchPath + " ...");
        watchPath.register(watchService, KINDS);
        try {
            boolean loop = true;
            //noinspection InfiniteLoopStatement
            while (loop) {
                final WatchKey taken = watchService.take();
                try {
                    process(taken);
                } finally {
                    loop = taken.reset();
                }
            }
        } catch (final InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private static void process(final WatchKey taken) {
        taken.pollEvents().forEach(event -> {
            event.context();
            LOGGER.info(format("\n" +
                    "\tcontext: {0}\n" +
                    "\tcount: {1}\n" +
                    "\tkind: {2}", event.context(), event.count(), event.kind()));
        });
    }
}
