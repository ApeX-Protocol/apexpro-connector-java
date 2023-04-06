package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.Order;

public class GetOrderByClientOrderId {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Order result = syncRequestClient.getOrderByClientOrderId("f162b9a2-2f24-4cfe-bf23-f6e18ff7cbce");
        System.out.println("View order: " +result);
    }
}
