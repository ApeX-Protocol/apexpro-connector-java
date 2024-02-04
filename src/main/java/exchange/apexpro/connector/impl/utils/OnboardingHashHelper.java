package exchange.apexpro.connector.impl.utils;

import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.impl.utils.eip712.EIP712TypedDataStruct;
import exchange.apexpro.connector.impl.utils.eip712.SolidityPackEncoder;
import exchange.apexpro.connector.impl.utils.eip712.TypedData;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static exchange.apexpro.connector.constant.ApiConstants.*;


public class OnboardingHashHelper {

    public static final String VERSION = "1.0";

    /**
     * Returns the HASH for the L2 Key signature body
     * @return
     */
    public static String buildL2KeySigningText(int chainId){

        //eip712_message = '{"name": "apex","version": "1.0","envId": 5,"action": "L2 Key","onlySignOn": "https://trade.apex.exchange"}'
        String message = "name: "+BROKER_NAME+"\n" +
                "version: 1.0\n" +
                "envId: "+chainId+"\n" +
                "action: "+ OFF_CHAIN_KEY_DERIVATION_ACTION+"\n" +
                "onlySignOn: "+OFF_CHAIN_ONBOARDING_SIGN_DOMAIN;

        return message;
    }

    /**
     * Returns the HASH for the onboarding signature body,
     * @param chainId
     * @param nonce
     * @return
     */
    public static String buildOnboardingSigningHash(int chainId, long nonce){

        List<Type> parts = new ArrayList<>();
        TypedData t1 = new TypedData("name", "string", BROKER_NAME);
        TypedData t2 = new TypedData("version", "string", VERSION);
        TypedData t3 = new TypedData("chainId", "uint256", chainId);
        byte[] domainHash = EIP712TypedDataStruct.buildHashStruct("EIP712Domain",Arrays.asList(t1, t2, t3));
        parts.add(new Bytes32(domainHash));

        TypedData t6 = new TypedData("action", "string", OFF_CHAIN_ONBOARDING_ACTION);
        TypedData t7 = new TypedData("onlySignOn", "string", OFF_CHAIN_ONBOARDING_SIGN_DOMAIN);
        TypedData t8 = new TypedData("nonce", "string", nonce);
        byte[] messageHash = EIP712TypedDataStruct.buildHashStruct(BROKER_NAME,Arrays.asList(t6, t7, t8));
        parts.add(new Bytes32(messageHash));

        byte[] bodyHexResult = SolidityPackEncoder.solidityPack(parts);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(BinaryUtil.hexToByte("0x1901"),0,2);
        baos.write(bodyHexResult,0,bodyHexResult.length);
        byte[] eip712Hash = Hash.sha3(baos.toByteArray());

        return BinaryUtil.byteToHex(eip712Hash);
    }


    /**
     * Verify that an EIP721 signature is valid
     */
    public static boolean verifySignature(String ethSignature,String originalMessageHash,String ethAddress){


        final byte[] msgHash = BinaryUtil.hexToByte(originalMessageHash);
        final byte[] signatureBytes = Numeric.hexStringToByteArray(ethSignature);


        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        final Sign.SignatureData sd = new Sign.SignatureData(v,
                Arrays.copyOfRange(signatureBytes, 0, 32),
                Arrays.copyOfRange(signatureBytes, 32, 64));

        String addressRecovered = null;

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            final BigInteger publicKey = Sign.recoverFromSignature((byte) i, new ECDSASignature(
                    new BigInteger(1, sd.getR()),
                    new BigInteger(1, sd.getS())), msgHash);

            if (publicKey != null) {
                addressRecovered = "0x" + Keys.getAddress(publicKey);

                if (addressRecovered.equals(ethAddress) || addressRecovered.equalsIgnoreCase(ethAddress.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

}
