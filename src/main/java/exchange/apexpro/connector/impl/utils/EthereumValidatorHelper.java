package exchange.apexpro.connector.impl.utils;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

public class EthereumValidatorHelper {

    /**
     * To checker whether is a valid ethereum address
     * @param ethAddress
     * @return
     */
    public static boolean isValidEthAddress(String ethAddress){

        if (!ethAddress.startsWith("0x"))
            return false;

        String cleanHexInput = Numeric.cleanHexPrefix(ethAddress);
        try{
            Numeric.toBigIntNoPrefix(cleanHexInput);
        }catch(NumberFormatException e){
            return false;
        }
        return cleanHexInput.length() == 40;
    }

    /**
     * To checker whether is a valid EIP721 signature
     */
    public static boolean isValidOnboardingSignature(String onboardingSignature,String originalMessageHash,String ethAddress){


        final byte[] msgHash = BinaryUtil.hexToByte(originalMessageHash);
        final byte[] signatureBytes = Numeric.hexStringToByteArray(onboardingSignature);


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

                if (addressRecovered.equals(ethAddress) || addressRecovered.toLowerCase().equals(ethAddress.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }


}
