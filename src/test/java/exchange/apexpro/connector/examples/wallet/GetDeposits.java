package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import exchange.apexpro.connector.model.wallet.DepositList;

import java.util.List;

public class GetDeposits {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        DepositList result = syncRequestClient.getDepositList(100, 0L, ApiConstants.COLLATERAL_ASSET_USDC, null, null, List.of("1","2"));
        System.out.println(result);

        result = syncRequestClient.getDepositList(100, 0L, ApiConstants.COLLATERAL_ASSET_USDT, null, null, List.of("1","2"));
        System.out.println(result);

    }
}
