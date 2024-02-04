package exchange.apexpro.connector;

import exchange.apexpro.connector.impl.utils.BinaryUtil;
import exchange.apexpro.connector.impl.utils.OnboardingHashHelper;
import exchange.apexpro.connector.impl.utils.ecdsa.PrivateKey;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;


public class Onboard {

    public static L2KeyPair deriveL2Key(Credentials web3Credentials, int networkId) {

        String message = OnboardingHashHelper.buildL2KeySigningText(networkId);

        Sign.SignatureData ethSignature = Sign.signPrefixedMessage(message.getBytes(StandardCharsets.UTF_8), web3Credentials.getEcKeyPair());

        byte[] signatureValue = new byte[65];
        System.arraycopy(ethSignature.getR(), 0, signatureValue, 0, 32);
        System.arraycopy(ethSignature.getS(), 0, signatureValue, 32, 32);
        System.arraycopy(ethSignature.getV(), 0, signatureValue, 64, 1);

        BigInteger signatureValueToL2Key = Numeric.toBigInt(Numeric.cleanHexPrefix(Numeric.toHexString(signatureValue)+"03"));

        String hashedSignatureToL2Key = Numeric.toHexString(Hash.sha3(BinaryUtil.hexToByte(Numeric.toHexStringNoPrefix(signatureValueToL2Key))));

        BigInteger privateKeyInt =  Numeric.toBigInt(hashedSignatureToL2Key).shiftRight(5);
        PrivateKey privateKey = PrivateKey.fromKey(privateKeyInt);

        BigInteger publicKeyXInt = privateKey.publicKey().point.x;
        BigInteger publicKeyYInt = privateKey.publicKey().point.y;

        L2KeyPair l2KeyPair = new L2KeyPair();
        l2KeyPair.setPrivateKey(Numeric.toHexStringNoPrefixZeroPadded(privateKeyInt,64));
        l2KeyPair.setPublicKey(Numeric.toHexStringNoPrefixZeroPadded(publicKeyXInt,64));
        l2KeyPair.setPublicKeyYCoordinate(Numeric.toHexStringNoPrefixZeroPadded(publicKeyYInt,64));

        return l2KeyPair;
    }


    public static ApiCredential generateApiCredential(Credentials web3Credentials, String l2Key, String l2KeyYCoordinate, int networkId) {
        RequestOptions options = new RequestOptions();
        options.setNetworkId(networkId);

        SyncRequestClient syncRequestClient = SyncRequestClient.create(options);
        String ethAddress = web3Credentials.getAddress();
        // networkId is defined in ApiConstants, main network environment is 1 (chainId = 1 is ethereum main network), test is 5 (chainId = 5 is Goerli test network);
        int chainId = networkId;
        Long nonce = syncRequestClient.generateNonce(ethAddress,chainId,l2Key);

        String messageHash = OnboardingHashHelper.buildOnboardingSigningHash(chainId,nonce);

        Sign.SignatureData ethSignature = Sign.signMessage(hexStringToByteArray(messageHash), web3Credentials.getEcKeyPair(),false);

        byte[] signatureValue = new byte[65];
        System.arraycopy(ethSignature.getR(), 0, signatureValue, 0, 32);
        System.arraycopy(ethSignature.getS(), 0, signatureValue, 32, 32);
        System.arraycopy(ethSignature.getV(), 0, signatureValue, 64, 1);
        String signature = Numeric.toHexString(signatureValue);
        ApiCredential apiCredential = syncRequestClient.onboard(ethAddress,signature,l2Key,l2KeyYCoordinate);
        apiCredential.setNetworkId(networkId);
        apiCredential.setAddress(ethAddress);
        return apiCredential;
    }

}