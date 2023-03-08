package exchange.apexpro.connector.impl.utils.ecdsa;

import java.math.BigInteger;

/**
 * y^2 = x^3 + A*x + B (mod P)
 */
public class Curve {

    public BigInteger A;
    public BigInteger B;
    public BigInteger P;
    public BigInteger N;
    public Point G;
    public String name;

    /**
     *
     * @param A A
     * @param B B
     * @param P P
     * @param N N
     * @param Gx Gx
     * @param Gy Gy
     * @param name name
     */
    public Curve(BigInteger A, BigInteger B, BigInteger P, BigInteger N, BigInteger Gx, BigInteger Gy, String name) {
        this.A = A;
        this.B = B;
        this.P = P;
        this.N = N;
        this.G = new Point(Gx, Gy);
        this.name = name;
    }

    /**
     * Verify if the point `p` is on the curve
     */
    public boolean contains(Point p) {
        if (p.x.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (p.x.compareTo(this.P) >= 0) {
            return false;
        }
        if (p.y.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (p.y.compareTo(this.P) >= 0) {
            return false;
        }
        return p.y.pow(2).subtract(p.x.pow(3).add(A.multiply(p.x)).add(B)).mod(P).intValue() == 0;
    }

    public int length() {
        return (1 + N.toString(16).length()) / 2;
    }

    public static final Curve secp256k1 = new Curve(
        BigInteger.ONE,
        new BigInteger("3141592653589793238462643383279502884197169399375105820974944592307816406665"),
        new BigInteger("3618502788666131213697322783095070105623107215331596699973092056135872020481"),
        new BigInteger("3618502788666131213697322783095070105526743751716087489154079457884512865583"),
        new BigInteger("874739451078007766457464989774322083649278607533249481151382481072868806602"),
        new BigInteger("152666792071518830868575557812948353041420400780739481342941381225525861407"),
        "secp256k1"
    );

}
