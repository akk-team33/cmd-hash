package net.team33.hash;

import java.math.BigInteger;

public class HashInfo {

    public final long timestamp;
    public final BigInteger hash;

    public HashInfo(final long timestamp, final BigInteger hash) {
        this.timestamp = timestamp;
        this.hash = hash;
    }
}
