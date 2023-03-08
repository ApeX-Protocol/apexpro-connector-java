package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.YesterdayPnl;

public class GetYesterdayPnl {

    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        YesterdayPnl historyValue = syncRequestClient.getYesterdayPnl();
        System.out.println(historyValue);
    }
}
