package exchange.apexpro.connector.impl.utils;

import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.ecdsa.PublicKey;


import java.util.regex.Pattern;

import static exchange.apexpro.connector.exception.ApexProApiException.INPUT_ERROR;
import static exchange.apexpro.connector.exception.ApexProApiException.RUNTIME_ERROR;

public class L2ParamUtil {

    private static final Pattern L2_KEY_PATTERN = Pattern.compile("^(0x)?[0-9a-fA-F]{1,64}$");
    private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("^0x[0-9a-fA-F]{40}$");

    public static String checkAndNormalizedL2Key(String l2Key) {
        if (l2Key.isBlank()) {
            throw new ApexProApiException(INPUT_ERROR,"blank l2Key");
        }
        if (!L2_KEY_PATTERN.matcher(l2Key).find()) {
            throw new ApexProApiException(INPUT_ERROR,"invalid l2Key: " + l2Key);
        }
        l2Key = l2Key.startsWith("0x") ? l2Key.substring(2) : l2Key;
        l2Key = l2Key.toLowerCase();
        return "0x" + ("0".repeat(64 - l2Key.length()) + l2Key);
    }

    public static String checkAndNormalizedL2KeyYCoordinate(String l2KeyYCoordinate) {
        if (l2KeyYCoordinate.isBlank()) {
            throw new ApexProApiException(INPUT_ERROR,"blank l2KeyYCoordinate");
        }
        if (!L2_KEY_PATTERN.matcher(l2KeyYCoordinate).find()) {
            throw new ApexProApiException(INPUT_ERROR,"invalid l2KeyYCoordinate: " + l2KeyYCoordinate);
        }
        l2KeyYCoordinate = l2KeyYCoordinate.startsWith("0x") ? l2KeyYCoordinate.substring(2) : l2KeyYCoordinate;
        l2KeyYCoordinate = l2KeyYCoordinate.toLowerCase();
        return "0x" + ("0".repeat(64 - l2KeyYCoordinate.length()) + l2KeyYCoordinate);
    }

    public static void checkL2KeyAndL2KeyYCoordinate(String l2Key, String l2KeyYCoordinate) {
        try {
            PublicKey.fromPointHexKey(l2Key, l2KeyYCoordinate);
        } catch (RuntimeException e) {
            throw new ApexProApiException(RUNTIME_ERROR,"invalid l2Key & l2KeyYCoordinate: " + l2Key + ", " + l2KeyYCoordinate);
        }
    }

    public static String checkAndNormalizedEthAddress(String ethAddress) {
        if (ethAddress.isEmpty()) {
            throw new ApexProApiException(INPUT_ERROR,"empty ethAddress");
        }
        if (!ETH_ADDRESS_PATTERN.matcher(ethAddress).find()) {
            throw new ApexProApiException(INPUT_ERROR,"invalid ethAddress: " + ethAddress);
        }
        return ethAddress.toLowerCase();
    }
}
