package exchange.apexpro.connector.impl.utils.ecdsa;


import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;


public class Signature {

    public BigInteger r;
    public BigInteger s;

    private Signature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
    }

    public static Signature create(BigInteger r, BigInteger s) {
        return new Signature(r,s);
    }

    public static Signature createWithHex(String rStr, String sStr) {
        if (StringUtils.isEmpty(rStr) || StringUtils.isEmpty(sStr)) {
            throw new RuntimeException("Signature rStr,sStr is empty");
        }
        BigInteger r = new BigInteger(rStr.substring(2),16);
        BigInteger s = new BigInteger(sStr.substring(2),16);
        return new Signature(r,s);
    }

}
