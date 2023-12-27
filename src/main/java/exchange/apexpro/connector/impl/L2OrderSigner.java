package exchange.apexpro.connector.impl;

import com.google.common.hash.Hashing;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.L2SignatureUtil;
import exchange.apexpro.connector.impl.utils.cryptohash.Keccak256;
import exchange.apexpro.connector.impl.utils.ecdsa.Ecdsa;
import exchange.apexpro.connector.impl.utils.ecdsa.PrivateKey;
import exchange.apexpro.connector.impl.utils.ecdsa.Signature;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.meta.Currency;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.meta.MultiChain;
import exchange.apexpro.connector.model.meta.PerpetualContract;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static exchange.apexpro.connector.exception.ApexProApiException.RUNTIME_ERROR;

public class L2OrderSigner {


    public static String signOrder(L2KeyPair l2KeyPair, long accountId, String symbol, BigDecimal size, BigDecimal price, BigDecimal limitFee, long expireTime, String clientOrderId, OrderSide orderSide) throws IOException {

        PerpetualContract perpetualContract = ExchangeInfo.perpetualContract(symbol);
        Currency currency = perpetualContract.getSettleCurrency();
        BigInteger msgHash = L2SignatureUtil.hashMsgLimitOrder(
                accountId,
                currency.getStarkExAssetId(),
                perpetualContract.getStarkExSyntheticAssetId(),
                size.multiply(price)
                        .multiply(currency.getStarkExResolution())
                        .longValueExact(),
                size.multiply(perpetualContract.getStarkExResolution())
                        .longValueExact(),
                limitFee.multiply(currency.getStarkExResolution()).longValueExact(),
                expireTime / (60 * 60 * 1000L),
                orderSide == OrderSide.BUY,
                Long.parseLong(Hashing.sha256()
                        .hashString(clientOrderId, StandardCharsets.UTF_8)
                        .toString()
                        .substring(0, 8), 16));
        PrivateKey privateKey = PrivateKey.fromKey(l2KeyPair.getPrivateKey());
        Signature sign = Ecdsa.sign(msgHash, privateKey);

        byte[] rbytes = sign.r.toByteArray();
        byte[] sbytes = sign.s.toByteArray();
        for (int i = rbytes.length; i < 32; i++) {
            rbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, rbytes);
        }
        for (int i = sbytes.length; i < 32; i++) {
            sbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, sbytes);
        }
        byte[] bytesValue = ArrayUtils.addAll(rbytes, sbytes);
        return Hex.encodeHexString(bytesValue);
    }

    public static String signWithdrawalOrder(L2KeyPair l2KeyPair, Long accountId,String address, BigDecimal amount, String clientId, Long expireTimeInHour, String currencyId) {

        Currency currency = ExchangeInfo.currency(currencyId,ExchangeInfo.getContractZone(currencyId));

        BigInteger msgHash = L2SignatureUtil.hashMsgWithdrawToAddress(
                accountId,
                currency.getStarkExAssetId(),
                amount.multiply(currency.getStarkExResolution()).longValueExact(),
                address,
                expireTimeInHour,
                Long.parseLong(Hashing.sha256()
                        .hashString(clientId, StandardCharsets.UTF_8)
                        .toString()
                        .substring(0, 8), 16));
        PrivateKey privateKey = PrivateKey.fromKey(l2KeyPair.getPrivateKey());
        Signature sign = Ecdsa.sign(msgHash, privateKey);
        Boolean T = Ecdsa.verify(msgHash,sign,privateKey.publicKey());

        byte[] rbytes = sign.r.toByteArray();
        byte[] sbytes = sign.s.toByteArray();
        for (int i = rbytes.length; i < 32; i++) {
            rbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, rbytes);
        }
        for (int i = sbytes.length; i < 32; i++) {
            sbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, sbytes);
        }
        byte[] bytesValue = ArrayUtils.addAll(rbytes, sbytes);
        return Hex.encodeHexString(bytesValue);
    }

    public static String signCrossChainWithdraw(L2KeyPair l2KeyPair, Long accountId, BigDecimal amount, String clientId, Long expireTimeInHour, String currencyId, String address, BigDecimal fee, Long chainId){
        Currency currency = null;
        String contractZone = ExchangeInfo.getContractZone(currencyId);
        if (currencyId.toLowerCase().equals(ApiConstants.COLLATERAL_ASSET_USDC.toLowerCase()))
            currency = ExchangeInfo.currency(currencyId,ApiConstants.CONTRACT_ZONE_USDC);
        else if (currencyId.toLowerCase().equals(ApiConstants.COLLATERAL_ASSET_USDT.toLowerCase()))
            currency = ExchangeInfo.currency(currencyId,ApiConstants.CONTRACT_ZONE_USDT);

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain(contractZone).getChains().stream().filter(f->f.getChainId() == chainId).findAny();
        if (!chain.isPresent())
            throw new ApexProApiException(RUNTIME_ERROR,"In valid chainId:"+chainId);

        long lpAccountId  = ExchangeInfo.global(contractZone).getCrossChainAccountId();

        Long nonce = Long.parseLong(Hashing.sha256()
                .hashString(clientId, StandardCharsets.UTF_8)
                .toString()
                .substring(0, 8), 16);


        BigInteger msgHash = L2SignatureUtil.hashMsgTransfer(
                currency.getStarkExAssetId(),
                "0x0",
                ExchangeInfo.global(contractZone).getCrossChainL2Key(),
                accountId,
                lpAccountId,
                accountId,
                nonce,
                amount.add(fee).multiply(currency.getStarkExResolution()).longValueExact(),
                0l,
                expireTimeInHour);


        PrivateKey privateKey = PrivateKey.fromKey(l2KeyPair.getPrivateKey());
        Signature sign = Ecdsa.sign(msgHash, privateKey);
        Boolean T = Ecdsa.verify(msgHash,sign,privateKey.publicKey());

        byte[] rbytes = sign.r.toByteArray();
        byte[] sbytes = sign.s.toByteArray();
        for (int i = rbytes.length; i < 32; i++) {
            rbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, rbytes);
        }
        for (int i = sbytes.length; i < 32; i++) {
            sbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, sbytes);
        }
        byte[] bytesValue = ArrayUtils.addAll(rbytes, sbytes);
        return Hex.encodeHexString(bytesValue);
    }

    public static String signFastWithdraw(L2KeyPair l2KeyPair, Long accountId, BigDecimal amount, String clientId, Long expireTimeInHour, String currencyId, String address, BigDecimal fee, Long chainId) {
        String contractZone = ExchangeInfo.getContractZone(currencyId);
        Currency currency = ExchangeInfo.currency(currencyId,ExchangeInfo.getContractZone(contractZone));

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain(contractZone).getChains().stream().filter(f->f.getChainId() == chainId).findAny();
        if (!chain.isPresent())
            throw new ApexProApiException(RUNTIME_ERROR,"In valid chainId:"+chainId);

        MultiChain.MultiChainToken multiChainToken = chain.get().getTokens().stream().filter(t -> t.getToken().equals(currencyId)).findAny().get();


        long lpAccountId  = ExchangeInfo.global(contractZone).getFastWithdrawAccountId();

        Long nonce = Long.parseLong(Hashing.sha256()
                .hashString(clientId, StandardCharsets.UTF_8)
                .toString()
                .substring(0, 8), 16);

        Keccak256 keccak256 = new Keccak256();
        keccak256.update(L2SignatureUtil.hexStringToBytes(address));
        long amountLong =  amount.multiply(new BigDecimal("10").pow(multiChainToken.getDecimals().intValue())).longValueExact();
        keccak256.update(L2SignatureUtil.LeftPadBytes(BigInteger.valueOf( amount.multiply(new BigDecimal("10").pow(multiChainToken.getDecimals().intValue())).longValueExact() ).toByteArray(), 32));
        keccak256.update(L2SignatureUtil.hexStringToBytes(multiChainToken.getTokenAddress()));
        keccak256.update(L2SignatureUtil.LeftPadBytes(BigInteger.valueOf(nonce).toByteArray(), 32));
        byte[] fact = keccak256.digest();

        BigInteger condition=L2SignatureUtil.calcFastWithdrawCondition(ExchangeInfo.global(contractZone).getFastWithdrawFactRegisterAddress(),fact);


        BigInteger msgHash = L2SignatureUtil.hashMasFastWithdraw(
                new BigInteger(currency.getStarkExAssetId().substring(2), 16),
                BigInteger.valueOf(0),
                new BigInteger(ExchangeInfo.global(contractZone).getFastWithdrawL2Key().substring(2),16),
                condition,
                accountId,
                lpAccountId,
                accountId,
                nonce,
                BigInteger.valueOf((amount.add(fee)).multiply(currency.getStarkExResolution())
                        .longValueExact()),
                BigInteger.valueOf(0),
                expireTimeInHour);


        PrivateKey privateKey = PrivateKey.fromKey(l2KeyPair.getPrivateKey());
        Signature sign = Ecdsa.sign(msgHash, privateKey);
        Boolean T = Ecdsa.verify(msgHash,sign,privateKey.publicKey());

        byte[] rbytes = sign.r.toByteArray();
        byte[] sbytes = sign.s.toByteArray();
        for (int i = rbytes.length; i < 32; i++) {
            rbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, rbytes);
        }
        for (int i = sbytes.length; i < 32; i++) {
            sbytes = ArrayUtils.addAll(new byte[]{(byte) 0}, sbytes);
        }
        byte[] bytesValue = ArrayUtils.addAll(rbytes, sbytes);
        return Hex.encodeHexString(bytesValue);
    }

}
