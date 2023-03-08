package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.wallet.WithdrawalList;

public class GetWithdrawList {

    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        WithdrawalList result = syncRequestClient.getWithdrawList(100, 0l, null, null);
        System.out.println("Withdrawal list:"+result);
    }
}
