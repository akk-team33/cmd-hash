package net.team33.hash;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class BigIntegerConversionTest {

    @Test
    public final void test() {
        final byte[] bytes = new byte[]{-8, -46, -78, -12};
        final BigInteger bigInteger = new BigInteger(1, bytes);
        Assert.assertTrue(BigInteger.ZERO.compareTo(bigInteger) < 0);
        Assert.assertTrue(0 > bigInteger.intValue());
        Assert.assertEquals(bigInteger.toString(16), Integer.toHexString(bigInteger.intValue()));
    }
}
