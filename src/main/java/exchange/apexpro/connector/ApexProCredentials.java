package exchange.apexpro.connector;

import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import org.web3j.crypto.Credentials;

public class ApexProCredentials {
    public Credentials web3Credentials;
    public ApiCredential apiCredential;
    public L2KeyPair l2KeyPair;
    private ApexProCredentials(String privateEthereumKey) {
        Credentials web3Credentials = Credentials.create(privateEthereumKey);
        this.web3Credentials = web3Credentials;
    }

    public static ApexProCredentials create(String privateEthereumKey,int networkId) throws ApexProApiException {
        ApexProCredentials apexProCredentials = new ApexProCredentials(privateEthereumKey);
        L2KeyPair l2KeyPair = Onboard.deriveL2Key(apexProCredentials.web3Credentials, ApiConstants.NETWORKID_TEST);

        apexProCredentials.l2KeyPair = l2KeyPair;
        apexProCredentials.apiCredential = Onboard.generateApiCredential(apexProCredentials.web3Credentials, l2KeyPair.getPublicKey(), l2KeyPair.getPublicKeyYCoordinate(),networkId);
        return apexProCredentials;

    }

    public String getAddress() {
        return this.web3Credentials.getAddress();
    }

}
