package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.Order;

public class GetOrder {
    public static void main(String[] args) {

        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        Order result = syncRequestClient.getOrder("415687649278296337");
        System.out.println("View order: " +result);

    }
}
