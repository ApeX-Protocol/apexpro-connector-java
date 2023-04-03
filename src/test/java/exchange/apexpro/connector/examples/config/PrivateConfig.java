package exchange.apexpro.connector.examples.config;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.Onboard;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.web3j.crypto.Credentials;

public class PrivateConfig {

    //Your ethereum private key
    private static String ETH_PRIVATE_KEY = "387bfabbf324497354364f7ad3ea47221f93636fe0fbb27c5f38dfab97350d11";
    private static int network = ApiConstants.NETWORKID_TEST;

    public Credentials web3Credentials;
    public ApiCredential apiCredential;
    public L2KeyPair l2KeyPair;

    public static PrivateConfig loadConfig() {
        PrivateConfig privateConfig = new PrivateConfig();
        ApexProCredentials apexProCredentials = ApexProCredentials.create(ETH_PRIVATE_KEY,network);
        apexProCredentials.web3Credentials = Credentials.create(ETH_PRIVATE_KEY);
        privateConfig.web3Credentials = apexProCredentials.web3Credentials;
        privateConfig.l2KeyPair = apexProCredentials.l2KeyPair;
        privateConfig.apiCredential = apexProCredentials.apiCredential;
        return privateConfig;
    }

}
