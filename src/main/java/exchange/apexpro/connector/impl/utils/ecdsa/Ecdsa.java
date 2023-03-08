package exchange.apexpro.connector.impl.utils.ecdsa;


import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class Ecdsa {

    public static Signature sign(BigInteger msgHash, PrivateKey privateKey) {
        BigInteger numberMessage = msgHash;
        Curve curve = privateKey.curve;
        BigInteger randNum = new BigInteger(curve.N.toByteArray().length * 8 - 1, new SecureRandom()).abs().add(BigInteger.ONE);
        Point randomSignPoint = EcMath.multiply(curve.G, randNum, curve.N, curve.A, curve.P);
        BigInteger r = randomSignPoint.x.mod(curve.N);
        BigInteger s = ((numberMessage.add(r.multiply(privateKey.secret))).multiply(EcMath.inv(randNum, curve.N))).mod(curve.N);
        return Signature.create(r, s);
    }


    public static Signature sign(String message, PrivateKey privateKey) {
        try {
            byte[] hashMessage = MessageDigest.getInstance("SHA-256").digest(message.getBytes());
            BigInteger numberMessage = new BigInteger(Hex.encodeHexString(hashMessage),16);
            return sign(numberMessage, privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find SHA-256 message digest in provided java environment");
        }
    }

    public static boolean verify(BigInteger msgHash, Signature signature, PublicKey publicKey) {
        BigInteger numberMessage = msgHash;
        Curve curve = publicKey.curve;
        BigInteger r = signature.r;
        BigInteger s = signature.s;

        if (r.compareTo(new BigInteger(String.valueOf(1))) < 0) {
            return false;
        }
        if (r.compareTo(curve.N) >= 0) {
            return false;
        }
        if (s.compareTo(new BigInteger(String.valueOf(1))) < 0) {
            return false;
        }
        if (s.compareTo(curve.N) >= 0) {
            return false;
        }

        BigInteger w = EcMath.inv(s, curve.N);
        Point u1 = EcMath.multiply(curve.G, numberMessage.multiply(w).mod(curve.N), curve.N, curve.A, curve.P);
        Point u2 = EcMath.multiply(publicKey.point, r.multiply(w).mod(curve.N), curve.N, curve.A, curve.P);
        Point v = EcMath.add(u1, u2, curve.A, curve.P);
        if (v.isAtInfinity()) {
            return false;
        }
        return v.x.mod(curve.N).equals(r);
    }


    public static boolean verify(String message, Signature signature, PublicKey publicKey) {
        try {
            byte[] hashMessage = MessageDigest.getInstance("SHA-256").digest(message.getBytes());
            BigInteger numberMessage = new BigInteger(Hex.encodeHexString(hashMessage),16);
            return verify(numberMessage, signature, publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find SHA-256 message digest in provided java environment");
        }
    }

}
