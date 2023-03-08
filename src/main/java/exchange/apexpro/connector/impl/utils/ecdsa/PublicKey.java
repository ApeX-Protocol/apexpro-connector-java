package exchange.apexpro.connector.impl.utils.ecdsa;


import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

public class PublicKey {

    public Point point;
    public Curve curve;

    private PublicKey(Point point, Curve curve) {
        this.point = point;
        this.curve = curve;
    }

    private static PublicKey create(String xs, String ys, Curve curve, boolean validatePoint) {
        Point p = new Point(new BigInteger(xs,16), new BigInteger(ys,16));

        PublicKey publicKey = new PublicKey(p, curve);
        if (!validatePoint) {
            return publicKey;
        }
        if (p.isAtInfinity()) {
            throw new RuntimeException("Public Key point is at infinity");
        }
        if (!curve.contains(p)) {
            throw new RuntimeException(String.format("Point (%s,%s) is not valid for curve %s", p.x, p.y, curve.name));
        }
        if (!EcMath.multiply(p, curve.N, curve.N, curve.A, curve.P).isAtInfinity()) {
            throw new RuntimeException(String.format("Point (%s,%s) * %s.N is not at infinity", p.x, p.y, curve.name));
        }
        return publicKey;
    }

    private static PublicKey fromString(String str, Curve curve, boolean validatePoint) {
        int baselen = curve.length();
        String xs = str.substring(0,baselen);
        String ys = str.substring(baselen);
        return create(xs,ys,curve,validatePoint);
    }

    public static PublicKey fromPointHexKey(String strX, String strY) {
        if (StringUtils.isEmpty(strX) || StringUtils.isEmpty(strY)) {
            throw new RuntimeException("publicKey x,y point is empty");
        }
        strX = strX.startsWith("0x") ? strX.substring(2) : strX;
        strY = strY.startsWith("0x") ? strY.substring(2) : strY;
        return create(strX,strY,Curve.secp256k1,true);
    }

    public static PublicKey fromKey(String key) {
        return fromString(key, Curve.secp256k1,true);
    }

    public static PublicKey fromPoint(Point point) {
        return new PublicKey(point, Curve.secp256k1);
    }
}
