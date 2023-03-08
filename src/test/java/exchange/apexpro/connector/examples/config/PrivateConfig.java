package exchange.apexpro.connector.examples.config;

import exchange.apexpro.connector.Onboard;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.web3j.crypto.Credentials;

public class PrivateConfig {

    //Your ethereum private key
    private static String ETH_PRIVATE_KEY = "387bfabbf324497354364f7ad3ea47221f93636fe0fbb27c5f38dfab97350d11";


    public Credentials credentials;

    public ApiCredential apiCredential;

    public L2KeyPair l2KeyPair;

    public static PrivateConfig loadConfig() {
        Credentials web3Credentials = Credentials.create(ETH_PRIVATE_KEY);
        L2KeyPair l2KeyPair = Onboard.deriveL2Key(web3Credentials, ApiConstants.NETWORKID_TEST);
        PrivateConfig privateConfig = new PrivateConfig();

        privateConfig.credentials = web3Credentials;

        privateConfig.l2KeyPair = l2KeyPair;
        privateConfig.apiCredential = Onboard.generateApiCredential(web3Credentials, l2KeyPair.getPublicKey(), l2KeyPair.getPublicKeyYCoordinate(),ApiConstants.NETWORKID_TEST);
        return privateConfig;
    }

}
