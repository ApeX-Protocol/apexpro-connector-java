package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.wallet.WithdrawalList;

public class GetWithdrawList {

    public static void main(String[] args) {

        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        WithdrawalList result = syncRequestClient.getWithdrawList(ApiConstants.COLLATERAL_ASSET_USDC,100, 0l, null, null);
        System.out.println("Withdrawal list:"+result);
    }
}
