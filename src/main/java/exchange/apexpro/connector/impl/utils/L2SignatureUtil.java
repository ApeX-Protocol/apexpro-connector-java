package exchange.apexpro.connector.impl.utils;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import exchange.apexpro.connector.impl.utils.cryptohash.Keccak256;
import exchange.apexpro.connector.impl.utils.ecdsa.*;
import exchange.apexpro.connector.model.meta.Currency;
import exchange.apexpro.connector.model.meta.ExchangeInfo;


import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;


public class L2SignatureUtil {

    private static final BigInteger[][] PEDERSEN_POINTS;

    static {
        try (Reader r = Resources.asCharSource(
                Resources.getResource(
                        "pedersen_params.json"),
                Charsets.UTF_8).openStream()) {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(r, JsonElement.class);
            JsonArray array = jsonElement.getAsJsonObject().getAsJsonArray("CONSTANT_POINTS");
            BigInteger[][] pedersenPoints = new BigInteger[array.size()][];
            for (int i = 0; i < array.size(); ++i) {
                JsonArray a = array.get(i).getAsJsonArray();
                pedersenPoints[i] = new BigInteger[2];
                pedersenPoints[i][0] = a.get(0).getAsBigInteger();
                pedersenPoints[i][1] = a.get(1).getAsBigInteger();
            }
            PEDERSEN_POINTS = pedersenPoints;
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static BigInteger hashMsgLimitOrder(
            long positionId,
            String assetIdCollateral,
            String assetIdSynthetic,
            long amountCollateral,
            long amountSynthetic,
            long amountFee,
            long expirationTimestamp,
            boolean isBuyingSynthetic,
            long nonce) {
        String assetIdSell;
        String assetIdBuy;
        long amountSell;
        long amountBuy;
        if (isBuyingSynthetic) {
            assetIdSell = assetIdCollateral;
            assetIdBuy = assetIdSynthetic;
            amountSell = amountCollateral;
            amountBuy = amountSynthetic;
        } else {
            assetIdSell = assetIdSynthetic;
            assetIdBuy = assetIdCollateral;
            amountSell = amountSynthetic;
            amountBuy = amountCollateral;
        }
        BigInteger msg = pedersenHash(
                new BigInteger(assetIdSell.substring(2),16),
                new BigInteger(assetIdBuy.substring(2),16));

        msg = pedersenHash(
                msg,
                new BigInteger(assetIdCollateral.substring(2),16));

        BigInteger packedMessage0 = BigInteger.valueOf(amountSell);
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(amountBuy));
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(amountFee));
        packedMessage0 = packedMessage0.shiftLeft(32).add(BigInteger.valueOf(nonce));

        msg = pedersenHash(msg, packedMessage0);
        BigInteger packedMessage1 = BigInteger.valueOf(3);;
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(positionId));
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(positionId));
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(positionId));
        packedMessage1 = packedMessage1.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage1 = packedMessage1.shiftLeft(17);

        return pedersenHash(msg, packedMessage1);
    }


    public static BigInteger hashMsgWithdraw(
            long positionId,
            String assetIdCollateral,
            long amount,
            long expirationTimestamp,
            long nonce) {
        BigInteger packedMessage = BigInteger.valueOf(6);
        packedMessage = packedMessage.shiftLeft(64).add(BigInteger.valueOf(positionId));
        packedMessage = packedMessage.shiftLeft(32).add(BigInteger.valueOf(nonce));
        packedMessage = packedMessage.shiftLeft(64).add(BigInteger.valueOf(amount));
        packedMessage = packedMessage.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage = packedMessage.shiftLeft(49);
        return pedersenHash(
                new BigInteger(assetIdCollateral.substring(2), 16),
                packedMessage);
    }


    public static BigInteger hashMsgWithdrawToAddress(
            long positionId,
            String assetIdCollateral,
            long amount,
            String ethAddress,
            long expirationTimestamp,
            long nonce) {
        BigInteger packedMessage = BigInteger.valueOf(7);
        packedMessage = packedMessage.shiftLeft(64).add(BigInteger.valueOf(positionId));
        packedMessage = packedMessage.shiftLeft(32).add(BigInteger.valueOf(nonce));
        packedMessage = packedMessage.shiftLeft(64).add(BigInteger.valueOf(amount));
        packedMessage = packedMessage.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage = packedMessage.shiftLeft(49);
        BigInteger asset = new BigInteger(assetIdCollateral.substring(2), 16);
        BigInteger ethAddressInt = new BigInteger(ethAddress.substring(2).toLowerCase(), 16);
        return pedersenHash(pedersenHash(asset, ethAddressInt), packedMessage);
    }


    public static BigInteger hashMsgTransfer(
            String assetId,
            String assetIdFee,
            String receiverPublicKey,
            long senderPositionId,
            long receiverPositionId,
            long srcFeePositionId,
            long nonce,
            long amount,
            long maxAmountFee,
            long expirationTimestamp) {

        BigInteger msg = pedersenHash(
                new BigInteger(assetId.substring(2), 16),
                new BigInteger(assetIdFee.substring(2), 16));
        msg = pedersenHash(
                msg,
                new BigInteger(receiverPublicKey.substring(2), 16));

        BigInteger packedMessage0 = BigInteger.valueOf(senderPositionId);
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(receiverPositionId));
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(srcFeePositionId));
        packedMessage0 = packedMessage0.shiftLeft(32).add(BigInteger.valueOf(nonce));
        msg = pedersenHash(msg, packedMessage0);

        BigInteger packedMessage1 = BigInteger.valueOf(4);
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(amount));
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(maxAmountFee));
        packedMessage1 = packedMessage1.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage1 = packedMessage1.shiftLeft(81); //  Padding.
        return pedersenHash(msg, packedMessage1);
    }


    public static BigInteger hashMsgConditionalTransfer(
            String assetId,
            String assetIdFee,
            String receiverPublicKey,
            String factRegistryAddress,
            String fact,
            long senderPositionId,
            long receiverPositionId,
            long srcFeePositionId,
            long nonce,
            long amount,
            long maxAmountFee,
            long expirationTimestamp) {

        BigInteger msg = pedersenHash(
                new BigInteger(assetId.substring(2), 16),
                new BigInteger(assetIdFee.substring(2), 16));
        msg = pedersenHash(
                msg,
                new BigInteger(receiverPublicKey.substring(2), 16));

        msg = pedersenHash(
                msg,
                calcFastWithdrawCondition(factRegistryAddress, hexStringToBytes(fact)));

        BigInteger packedMessage0 = BigInteger.valueOf(senderPositionId);
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(receiverPositionId));
        packedMessage0 = packedMessage0.shiftLeft(64).add(BigInteger.valueOf(srcFeePositionId));
        packedMessage0 = packedMessage0.shiftLeft(32).add(BigInteger.valueOf(nonce));
        msg = pedersenHash(msg, packedMessage0);

        BigInteger packedMessage1 = BigInteger.valueOf(5);
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(amount));
        packedMessage1 = packedMessage1.shiftLeft(64).add(BigInteger.valueOf(maxAmountFee));
        packedMessage1 = packedMessage1.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage1 = packedMessage1.shiftLeft(81); //  Padding.
        return pedersenHash(msg, packedMessage1);
    }

    private static BigInteger buildCondition(String factRegistryAddress, String fact) {
        Keccak256 keccak256 = new Keccak256();
        keccak256.update(BaseEncoding.base16().decode(factRegistryAddress.substring(2).toUpperCase()));
        keccak256.update(BaseEncoding.base16().decode("0".repeat(64 - fact.length()) + fact.toUpperCase()));
        return new BigInteger(keccak256.digest()).and(BigInteger.ONE.shiftLeft(250).subtract(BigInteger.ONE));
    }

    public static BigInteger pedersenHash(BigInteger... input) {
        BigInteger[][] points = PEDERSEN_POINTS;
        Point shiftPoint = new Point(points[0][0], points[0][1]);
        for (int i = 0; i < input.length; i++) {
            BigInteger x = input[i];
            for (int j = 0; j < 252; j++) {
                int pos = 2 + i*252 + j;
                Point pt = new Point(points[pos][0], points[pos][1]);
                if (x.and(BigInteger.ONE).intValue() != 0) {
                    shiftPoint = EcMath.add(shiftPoint,pt, Curve.secp256k1.A,Curve.secp256k1.P);
                }
                x = x.shiftRight(1);
            }
        }
        return shiftPoint.x;
    }


    public static BigInteger calcFastWithdrawCondition(String factRegistryAddress,byte[] fact){
        BigInteger asset = new BigInteger(factRegistryAddress.substring(2), 16);
        Keccak256 keccak256=new Keccak256();
        keccak256.update(asset.toByteArray());
        keccak256.update(LeftPadBytes(fact,32));
        BigInteger signedDataInt=new BigInteger(keccak256.digest());

        BigInteger mask250=BigInteger.ONE.shiftLeft(250).subtract(BigInteger.ONE);
        mask250=mask250.add(BigInteger.ONE);
        signedDataInt=signedDataInt.mod(mask250);
        return signedDataInt;
    }

    public static byte[] calcFastWithdrawFact(String currencyId,String ethAddress, String erc20Address, BigDecimal amount, String salt){
        Currency currency = ExchangeInfo.currency(currencyId,ExchangeInfo.getContractZone(currencyId));
        BigInteger saltBigInteger=new BigInteger(salt);
        Keccak256 keccak256=new Keccak256();
        keccak256.update(hexStringToBytes(ethAddress));
        keccak256.update(LeftPadBytes(BigInteger.valueOf(amount.multiply(currency.getStarkExResolution())
                .longValueExact()).toByteArray(),32));
        keccak256.update(hexStringToBytes(erc20Address));
        keccak256.update(LeftPadBytes(saltBigInteger.toByteArray(),32));
        byte[] bytes=keccak256.digest();
        return bytes;
    }

    public static byte[] hexStringToBytes(String hex) {
        if(hex.startsWith("0x")){
            hex=hex.replaceFirst("0x","");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static byte[] LeftPadBytes(byte[] input,int len){
        byte[] bytes=new byte[len];
        for (int i=0;i<len;i++){
            bytes[i]=0;
        }
        for(int i=0;i<input.length;i++){
            bytes[len-input.length+i]=input[i];
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static BigInteger hashMasFastWithdraw(
            BigInteger assetId,
            BigInteger assetIdFee,
            BigInteger receiverPublicKey,
            BigInteger conditionInt,
            long positionId,
            long receiverPositionId,
            long feePositionId,
            long nonce,
            BigInteger amount,
            BigInteger maxAmountFee,
            long expirationTimestamp
    ){
        BigInteger msg= pedersenHash(assetId, assetIdFee);
        msg = pedersenHash(msg, receiverPublicKey);
        msg = pedersenHash(msg, conditionInt);

        BigInteger packedMessage0=BigInteger.valueOf(positionId);
        packedMessage0=packedMessage0.shiftLeft(64).add(BigInteger.valueOf(receiverPositionId));
        packedMessage0=packedMessage0.shiftLeft(64).add(BigInteger.valueOf(feePositionId));
        packedMessage0=packedMessage0.shiftLeft(32).add(BigInteger.valueOf(nonce));
        msg = pedersenHash(msg, packedMessage0);

        BigInteger packedMessage1=BigInteger.valueOf(5);
        packedMessage1=packedMessage1.shiftLeft(64).add(amount);
        packedMessage1=packedMessage1.shiftLeft(64).add(maxAmountFee);
        packedMessage1=packedMessage1.shiftLeft(32).add(BigInteger.valueOf(expirationTimestamp));
        packedMessage1=packedMessage1.shiftLeft(81);
        msg = pedersenHash(msg, packedMessage1);
        return msg;
    }
}
