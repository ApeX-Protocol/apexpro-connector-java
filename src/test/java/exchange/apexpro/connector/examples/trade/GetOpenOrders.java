package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.OpenOrders;

public class GetOpenOrders {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        OpenOrders result = syncRequestClient.getOpenOrders();
        System.out.println("Open Orders: "+result);
    }
}
