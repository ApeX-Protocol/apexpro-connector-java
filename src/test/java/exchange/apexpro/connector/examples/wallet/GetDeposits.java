package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.wallet.DepositList;

import java.util.List;

public class GetDeposits {

    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        DepositList result = syncRequestClient.getDepositList(100, 0L, "", null, null, List.of("1","2"));
        System.out.println(result);
    }
}
