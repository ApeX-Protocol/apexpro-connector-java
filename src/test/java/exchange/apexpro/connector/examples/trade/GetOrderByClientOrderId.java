package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.Order;

public class GetOrderByClientOrderId {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Order result = syncRequestClient.getOrderByClientOrderId("8ee58196-f4bf-47fd-88a0-417dc0f8ddbf");
        System.out.println("View order: " +result);
    }
}
