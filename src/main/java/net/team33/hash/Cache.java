package net.team33.hash;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cache {

    private final Map<Path, Entry> entries = new HashMap<>(0);

    public Entry entry(final Path path) {
        return Optional.ofNullable(entries.get(path)).orElseGet(() -> {
            final Entry entry = new Entry();
            entries.put(path, entry);
            return entry;
        });
    }

    public class Entry {

        private final Map<String, Details> map = new HashMap<>(0);

        public Details details(final String name) {
            return Optional.ofNullable(map.get(name)).orElseGet(() -> {
                final Details result = new Details();
                map.put(name, result);
                return result;
            });
        }
    }

    public class Details {

        private long update;
        private BigInteger hash;

        public long getUpdate() {
            return update;
        }

        public void setUpdate(final long timeStamp) {
            update = timeStamp;
        }

        public BigInteger getHash() {
            return hash;
        }

        public void setHash(final BigInteger value) {
            hash = value;
        }
    }
}
