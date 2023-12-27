package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.Order;

public class GetOrderByClientOrderId {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Order result = syncRequestClient.getOrderByClientOrderId("7dc20e21-18af-432d-91c9-71472f98adcc", ApiConstants.CONTRACT_ZONE_USDT);
        System.out.println("View order: " +result);
    }
}
