package exchange.apexpro.connector.impl.utils.ecdsa;


import java.math.BigInteger;
import java.security.SecureRandom;


public class PrivateKey {

    public Curve curve;
    public BigInteger secret;

    public PrivateKey() {
        this(Curve.secp256k1, null);
        secret = new BigInteger(curve.N.toByteArray().length * 8 - 1, new SecureRandom()).abs().add(BigInteger.ONE);
    }

    private PrivateKey(Curve curve, BigInteger secret) {
        this.curve = curve;
        this.secret = secret;
    }

    public PublicKey publicKey() {
        Curve curve = this.curve;
        Point publicPoint = EcMath.multiply(curve.G, this.secret, curve.N, curve.A, curve.P);
        return PublicKey.fromPoint(publicPoint);
    }

    public static PrivateKey fromKey(String key) {
        return new PrivateKey(Curve.secp256k1, new BigInteger(key,16));
    }

    public static PrivateKey fromKey(BigInteger key) {
        return new PrivateKey(Curve.secp256k1, key);
    }

}
