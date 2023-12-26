package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.HistoryOrders;

public class GetHistoryOrders {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        HistoryOrders result = syncRequestClient.getHistoryOrders("BTC-USDT", null, null, null, 0l, 0l, 100, 0);
        System.out.println(result);
    }
}
