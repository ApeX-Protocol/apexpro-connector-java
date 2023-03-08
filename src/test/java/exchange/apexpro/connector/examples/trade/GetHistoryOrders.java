package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.HistoryOrders;

public class GetHistoryOrders {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        HistoryOrders result = syncRequestClient.getHistoryOrders("BTC-USDC", null, null, null, 0l, 0l, 100, 0);
        System.out.println(result);
    }
}
