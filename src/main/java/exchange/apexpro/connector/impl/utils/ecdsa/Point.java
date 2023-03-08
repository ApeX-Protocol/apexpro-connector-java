package exchange.apexpro.connector.impl.utils.ecdsa;
import java.math.BigInteger;


public class Point {

    public BigInteger x;
    public BigInteger y;
    public BigInteger z;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        this.z = BigInteger.ZERO;
    }

    public Point(BigInteger x, BigInteger y, BigInteger z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isAtInfinity() {
        return this.y.equals(BigInteger.ZERO);
    }
}
