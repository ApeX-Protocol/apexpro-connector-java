package exchange.apexpro.connector.examples.config;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.Onboard;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.web3j.crypto.Credentials;

public class PrivateConfig {

    //Your ethereum private key
    private static String ETH_PRIVATE_KEY = "5921059e276bae2e61d8e5ade6d6a026cce953344d3b9f0df218ef9ecd90ac58";
    private static int network = ApiConstants.NETWORKID_TEST;


    public ApexProCredentials getApexProCredentials() {
        return apexProCredentials;
    }

    private ApexProCredentials apexProCredentials;

    public static PrivateConfig loadConfig() {
        PrivateConfig privateConfig = new PrivateConfig();
        ApexProCredentials apexProCredentials = ApexProCredentials.create(ETH_PRIVATE_KEY,network);
        privateConfig.apexProCredentials = apexProCredentials;
        return privateConfig;
    }

    public static PrivateConfig createApexProCredentials(L2KeyPair l2KeyPair,ApiCredential apiCredential) {
        PrivateConfig privateConfig = new PrivateConfig();
        ApexProCredentials apexProCredentials = ApexProCredentials.create(apiCredential,l2KeyPair);
        privateConfig.apexProCredentials = apexProCredentials;
        return privateConfig;
    }
}
